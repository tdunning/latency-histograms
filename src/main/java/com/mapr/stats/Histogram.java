package com.mapr.stats;

/**
 * Abstract class that describes how a Histogram should work.
 */
public abstract class Histogram {
    public Histogram(double min, double max, double binsPerDecade) {
        if (max <= 2 * min) {
            throw new IllegalArgumentException(String.format("Illegal/nonsensical min,max (%.2f, %.2g)", min, max));
        }
        if (min <= 0 || max <= 0) {
            throw new IllegalArgumentException("Min and max must be positive");
        }
        if (binsPerDecade < 5 || binsPerDecade > 500) {
            throw new IllegalArgumentException(
                    String.format("Unreasonable number of bins per decade %.2g. Expected value in range [5,500]",
                            binsPerDecade));
        }
    }

    abstract void add(double v);

    abstract double[] getCenters();

    abstract long[] getCounts();

    abstract long[] getCompressedCounts();
}
