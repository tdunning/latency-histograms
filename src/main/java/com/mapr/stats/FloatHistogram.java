package com.mapr.stats;

import java.nio.LongBuffer;

/**
 * Maintains histogram buckets that are constant width
 * in base-2 floating point representation space. This is close
 * to exponential binning, but should be faster.
 */
public class FloatHistogram extends Histogram {
    private final long[] counts;
    private final double min;
    private final double max;
    private final double fuzz;
    private final int bitsOfPrecision;

    public FloatHistogram(double min, double max) {
        this(min, max, 50);
    }

    public FloatHistogram(double min, double max, double binsPerDecade) {
        super(min, max, binsPerDecade);
        this.min = min;
        this.max = max;

        // convert binsPerDecade into bins per octave, then figure out how many bits that takes
        bitsOfPrecision = (int) Math.ceil(Math.log(binsPerDecade * Math.log10(2)) / Math.log(2));
        int binCount = (int) Math.ceil(Math.log(max / min) / Math.log(2) * Math.pow(2, bitsOfPrecision));

        if (binCount > 10000) {
            throw new IllegalArgumentException(
                    String.format("Excessive number of bins %d resulting from min,max,binsPerDecade = %.2g, %.2g, %.2g",
                            binCount, min, max, binsPerDecade));

        }
        counts = new long[binCount];
        fuzz = (1 + Math.pow(2, -(bitsOfPrecision + 1))) / min;
    }

    private int bucket(double x) {
        if (x <= min) {
            return 0;
        } else if (x >= max) {
            return counts.length - 1;
        } else {
            x *= fuzz;
            int shift1 = 52 - bitsOfPrecision;
            int shift2 = 0x3ff << bitsOfPrecision;
            long floatBits = Double.doubleToLongBits(x);
            return (int) (floatBits >>> shift1) - shift2;
        }
    }

    private double center(int k) {
        return Double.longBitsToDouble((k + (0x3ffL << bitsOfPrecision)) << (52 - bitsOfPrecision)) / fuzz;
    }

    @Override
    public void add(double v) {
        counts[bucket(v)]++;
    }

    @Override
    public double[] getCenters() {
        double[] r = new double[counts.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = center(i);
        }
        return r;
    }

    @Override
    public long[] getCounts() {
        return counts;
    }

    @Override
    public long[] getCompressedCounts() {
        LongBuffer buf = LongBuffer.allocate(counts.length);
        Simple64.compress(buf, counts, 0, counts.length);
        long[] r = new long[buf.position()];
        buf.flip();
        buf.get(r);
        return r;
    }
}
