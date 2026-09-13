package com.quantlab.monitoring.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class FeatureDriftCalculator {

    public record DriftResult(
            String metricType,
            double observedValue,
            double threshold,
            String driftStatus, // NORMAL, WATCH, WARNING, CRITICAL
            int sampleSize
    ) {}

    /**
     * Calculates Population Stability Index (PSI) between baseline and target distributions.
     * PSI < 0.10 -> NORMAL
     * 0.10 <= PSI < 0.25 -> WARNING
     * PSI >= 0.25 -> CRITICAL
     */
    public DriftResult calculatePsi(double[] baseline, double[] current, int numBins, double threshold) {
        if (baseline == null || baseline.length == 0 || current == null || current.length == 0) {
            return new DriftResult("PSI", 0.0, threshold, "NORMAL", 0);
        }

        double[] sortedBaseline = Arrays.copyOf(baseline, baseline.length);
        Arrays.sort(sortedBaseline);

        double[] binEdges = new double[numBins + 1];
        binEdges[0] = Double.NEGATIVE_INFINITY;
        binEdges[numBins] = Double.POSITIVE_INFINITY;

        for (int i = 1; i < numBins; i++) {
            int idx = (int) Math.round((double) i / numBins * sortedBaseline.length) - 1;
            idx = Math.max(0, Math.min(idx, sortedBaseline.length - 1));
            binEdges[i] = sortedBaseline[idx];
        }

        double[] baselineCounts = countBins(baseline, binEdges);
        double[] currentCounts = countBins(current, binEdges);

        double totalBaseline = baseline.length;
        double totalCurrent = current.length;

        double psi = 0.0;
        double epsilon = 1e-4; // prevent division by zero / log(0)

        for (int i = 0; i < numBins; i++) {
            double actualPct = (baselineCounts[i] + epsilon) / (totalBaseline + epsilon * numBins);
            double targetPct = (currentCounts[i] + epsilon) / (totalCurrent + epsilon * numBins);

            psi += (targetPct - actualPct) * Math.log(targetPct / actualPct);
        }

        psi = Math.max(0.0, psi);

        String status = "NORMAL";
        if (psi >= threshold) {
            status = "CRITICAL";
        } else if (psi >= 0.10) {
            status = "WARNING";
        }

        return new DriftResult("PSI", Math.round(psi * 10000.0) / 10000.0, threshold, status, current.length);
    }

    /**
     * Calculates two-sample Kolmogorov-Smirnov (KS) statistic: maximum vertical distance between empirical CDFs.
     */
    public DriftResult calculateKsTest(double[] baseline, double[] current, double threshold) {
        if (baseline == null || baseline.length == 0 || current == null || current.length == 0) {
            return new DriftResult("KS_TEST", 0.0, threshold, "NORMAL", 0);
        }

        double[] b = Arrays.copyOf(baseline, baseline.length);
        double[] c = Arrays.copyOf(current, current.length);
        Arrays.sort(b);
        Arrays.sort(c);

        int n1 = b.length;
        int n2 = c.length;
        int i = 0;
        int j = 0;
        double dMax = 0.0;

        while (i < n1 && j < n2) {
            double v1 = b[i];
            double v2 = c[j];
            double val = Math.min(v1, v2);

            while (i < n1 && b[i] <= val) i++;
            while (j < n2 && c[j] <= val) j++;

            double cdf1 = (double) i / n1;
            double cdf2 = (double) j / n2;
            dMax = Math.max(dMax, Math.abs(cdf1 - cdf2));
        }

        String status = dMax >= threshold ? "CRITICAL" : (dMax >= threshold * 0.7 ? "WARNING" : "NORMAL");
        return new DriftResult("KS_TEST", Math.round(dMax * 10000.0) / 10000.0, threshold, status, current.length);
    }

    /**
     * Calculates 1-Wasserstein Distance (Earth Mover's Distance) for 1D distributions.
     */
    public DriftResult calculateWassersteinDistance(double[] baseline, double[] current, double threshold) {
        if (baseline == null || baseline.length == 0 || current == null || current.length == 0) {
            return new DriftResult("WASSERSTEIN", 0.0, threshold, "NORMAL", 0);
        }

        double[] b = Arrays.copyOf(baseline, baseline.length);
        double[] c = Arrays.copyOf(current, current.length);
        Arrays.sort(b);
        Arrays.sort(c);

        int n = Math.min(b.length, c.length);
        double sumDiff = 0.0;

        for (int i = 0; i < n; i++) {
            int idxB = (int) ((double) i / n * b.length);
            int idxC = (int) ((double) i / n * c.length);
            sumDiff += Math.abs(b[idxB] - c[idxC]);
        }

        double distance = sumDiff / n;
        String status = distance >= threshold ? "CRITICAL" : (distance >= threshold * 0.7 ? "WARNING" : "NORMAL");

        return new DriftResult("WASSERSTEIN", Math.round(distance * 10000.0) / 10000.0, threshold, status, current.length);
    }

    /**
     * Normalized Difference in Mean and Standard Deviation.
     */
    public DriftResult calculateMeanDifference(double[] baseline, double[] current, double threshold) {
        if (baseline == null || baseline.length == 0 || current == null || current.length == 0) {
            return new DriftResult("MEAN_DIFF", 0.0, threshold, "NORMAL", 0);
        }

        double meanB = Arrays.stream(baseline).average().orElse(0.0);
        double meanC = Arrays.stream(current).average().orElse(0.0);
        double stdB = calculateStd(baseline, meanB);

        double stdDiff = stdB > 1e-6 ? Math.abs(meanC - meanB) / stdB : Math.abs(meanC - meanB);
        String status = stdDiff >= threshold ? "CRITICAL" : (stdDiff >= threshold * 0.5 ? "WARNING" : "NORMAL");

        return new DriftResult("MEAN_DIFF", Math.round(stdDiff * 10000.0) / 10000.0, threshold, status, current.length);
    }

    private double[] countBins(double[] data, double[] binEdges) {
        int numBins = binEdges.length - 1;
        double[] counts = new double[numBins];
        for (double val : data) {
            for (int b = 0; b < numBins; b++) {
                if (val >= binEdges[b] && (b == numBins - 1 ? val <= binEdges[b + 1] : val < binEdges[b + 1])) {
                    counts[b]++;
                    break;
                }
            }
        }
        return counts;
    }

    private double calculateStd(double[] data, double mean) {
        if (data.length <= 1) return 0.0;
        double sumSq = 0.0;
        for (double v : data) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (data.length - 1));
    }
}
