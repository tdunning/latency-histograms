package com.mapr.stats;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ExpHistogramTest {
    @Test
    public void testEmpty() {
        Histogram x = new ExpHistogram(1, 100);
        double[] bins = x.getBounds();
        assertEquals(1, bins[0], 1e-5);
        assertEquals(100, bins[bins.length - 1], 1e-5);
        assertEquals(101, bins.length);
    }

    @Test
    public void testLinear() {
        int n = 10000;
        Histogram x = new ExpHistogram(1e-3, 10);
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            double v = rand.nextDouble();
            x.add(v);
        }
        double fudge = Math.exp(Math.log(10) / 100);
        long[] counts = x.getCounts();
        double[] cuts = x.getBounds();
        long sum = 0;
        for (int i = 0; i < cuts.length - 1; i++) {
            sum += counts[i];
            double below = Math.min(10000, cuts[i] * fudge * n);
            assertEquals(0, Math.abs(sum - below), 150);
        }
    }
}