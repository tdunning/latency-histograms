package com.mapr.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads samples from standard input, ignoring lines that start with #
 * Prints out the histogram
 */
public class Analyze {
    public static void main(String[] args) throws IOException {
        Histogram histo = new FloatHistogram(1, 10000);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = in.readLine();
        long t = 0;
        int n = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                double x = Double.parseDouble(line);
                long t0 = System.nanoTime();
                histo.add(x);
                t += (System.nanoTime() - t0);
                n++;
            }
            line = in.readLine();
        }
        System.err.printf("Average time = %.3f us\n", t * 1e-3 / n);
        System.err.printf("Compressed size = %d bytes\n", histo.getCompressedCounts().length * 8);
        double[] cuts = histo.getBounds();
        long[] counts = histo.getCounts();
        for (int i = 0; i < cuts.length; i++) {
            System.out.printf("%.2f,%d\n", cuts[i], counts[i]);
        }
    }
}
