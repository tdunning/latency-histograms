package com.mapr.stats;

import java.nio.LongBuffer;
import java.util.Arrays;

/**
 * Records a histogram of values based on exponentially sized bins.
 * This allows values to be recorded with constant relative error.
 * Commonly, 50 bins per decade are captured which allows error to
 * be kept below roughly ±2%.
 */
public class ExpHistogram extends Histogram {
    private final double min;
    private final double max;
    private final double[] cuts;
    private final double binFactor;

    private final long[] counts;

    public ExpHistogram(double min, double max) {
        this(min, max, 50);
    }

    public ExpHistogram(double min, double max, double binsPerDecade) {
        super(min, max, binsPerDecade);

        this.min = min;
        this.max = max;
        double logWidth = Math.log(10) / binsPerDecade;
        binFactor = Math.exp(logWidth);
        int binCount = (int) (Math.log(max / min) / logWidth + 1);
        if (binsPerDecade < 5 || binsPerDecade > 500) {
            throw new IllegalArgumentException(
                    String.format("Unreasonable number of bins per decade %.2g. Expected value in range [5,500]",
                            binsPerDecade));
        }
        if (binCount > 10000) {
            throw new IllegalArgumentException(
                    String.format("Excessive number of bins %d resulting from min,max,binsPerDecade = %.2g, %.2g, %.2g",
                            binCount, min, max, binsPerDecade));

        }
        counts = new long[binCount];
        cuts = new double[binCount];
        double x = min / Math.exp(logWidth / 2);
        for (int i = 0; i < cuts.length; i++) {
            cuts[i] = x;
            x *= binFactor;
        }
        assert cuts.length < 1000;
        assert cuts[0] < min;
        assert min / cuts[0] < binFactor;
        assert x > max;
        assert max / cuts[cuts.length - 1] < binFactor;
    }

    @Override
    public void add(double v) {
        if (v < min) {
            counts[0]++;
        } else if (v > max) {
            counts[cuts.length]++;
        } else {
            int i = Arrays.binarySearch(cuts, v);
            if (i >= 0) {
                counts[i]++;
            } else {
                counts[-i-2]++;
            }
        }
    }

    @Override
    public double[] getCenters() {
        double[] r = new double[cuts.length];
        double x = min;
        for (int i = 0; i < r.length; i++) {
            r[i] = x;
            x *= binFactor;
        }
        return r;
    }

    @Override
    public long[] getCounts() {
        return counts;
    }

    @Override
    public long[] getCompressedCounts() {
        LongBuffer buf = LongBuffer.allocate(200);
        Simple64.compress(buf, counts, 0, counts.length);
        long[] r = new long[buf.position()];
        buf.flip();
        buf.get(r);
        return r;
    }
}
