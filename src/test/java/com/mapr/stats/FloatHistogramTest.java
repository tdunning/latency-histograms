package com.mapr.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FloatHistogramTest {
    @Test
    public void testEmpty() {
        Histogram x = new FloatHistogram(1, 100);
        double[] bins = x.getCenters();
        assertEquals(1, bins[0], 1e-5);
        assertTrue(bins[bins.length - 1] >= 100.0);
        assertTrue(bins.length >= 100);
    }

    @Test
    public void testLinear() {
        int n = 10000;
        Histogram x = new FloatHistogram(1e-3, 10);
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            double v = rand.nextDouble();
            x.add(v);
        }
        double fudge = 1 + 1.5 / 32;
        long[] counts = x.getCounts();
        double[] cuts = x.getCenters();
        long sum = 0;
        for (int i = 0; i < cuts.length - 1; i++) {
            sum += counts[i];
            double below = Math.min(10000, cuts[i] * fudge * n);
            assertEquals(0, Math.abs(sum - below), 250);
        }
    }

    @Test
    public void testFitToLog() throws Exception {
        double scale = Math.pow(2, 52) / 0.997592709;
        double x = 0.01;
        System.out.printf("x,y1,y1\n");
        double sxx = 0;
        double sxy = 0;
        double sx = 0;
        double sy = 0;
        double n = 0;

        while (x < 10) {
            long xz = Double.doubleToLongBits(x);
            double v1 = xz / scale - 1005.478676052;
            double v2 = 15 + Math.log(x) / Math.log(2);
            sxx += v1 * v1;
            sxy += v1 * v2;
            sx += v1;
            sy += v2;
            n += 1;
            System.out.printf("%.6f,%.6f,%.6f\n", x, v1, v2);
            x += 0.01;
        }
        double denom = sxx * n - sx * sx;
        double a = (sxy * n - sx * sy) / denom;
        double b = (sxx * sy - sxy * sx) / denom;
        System.out.printf("%.9f, %.9f\n", a, b);
        System.out.printf("%.4f, %.4f, %.4f, %.4f, %.4f\n", sxx, sxy, sx, sy, n);
    }
}