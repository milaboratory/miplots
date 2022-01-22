package com.milaboratory.miplots.stat.util;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Adapted from MOEAFramework
 */
public class KruskalWallis {
    public KruskalWallis() {}

    private static double H(int nGroups, List<Observation> data) {
        int[] n = new int[nGroups];
        double[] rBar = new double[nGroups];

        for (Observation observation : data) {
            n[observation.group]++;
            rBar[observation.group] += observation.rank;
        }

        double H = 0.0;
        for (int i = 0; i < nGroups; i++) {
            H += Math.pow(rBar[i], 2.0) / n[i];
        }

        int N = data.size();
        return 12.0 / (N * (N + 1)) * H - 3.0 * (N + 1);
    }

    private static double C(List<Observation> data) {
        int N = data.size();
        double C = 0.0;

        int i = 0;
        while (i < N) {
            int j = i + 1;

            while ((j < N) && (data.get(i).value == data.get(j).value))
                j++;

            C += Math.pow(j - i, 3.0) - (j - i);
            i = j;
        }

        return 1 - C / (Math.pow(N, 3.0) - N);
    }

    private static List<Observation> prepareData(double[]... datasets) {
        ArrayList<Observation> data = new ArrayList<>();
        for (int group = 0; group < datasets.length; group++) {
            data.ensureCapacity(datasets[group].length);
            for (double value : datasets[group]) {
                data.add(new Observation(value, group));
            }
        }
        data.sort(Comparator.comparingDouble(o -> o.value));

        int i = 0;
        while (i < data.size()) {
            int j = i + 1;
            double rank = i + 1;

            while ((j < data.size())
                    && (data.get(i).value == data.get(j).value)) {
                rank += j + 1;
                j++;
            }

            rank /= j - i;

            for (int k = i; k < j; k++) {
                data.get(k).rank = rank;
            }

            i = j;
        }

        return data;
    }

    /** Computes Kruskal-Wallis p-value */
    public double kruskalWallisTest(Collection<double[]> datasets) {
        return kruskalWallisTest(datasets.toArray(double[][]::new));
    }

    /** Computes Kruskal-Wallis p-value */
    public double kruskalWallisTest(double[]... datasets) {
        if (datasets.length <= 1)
            throw new IllegalArgumentException();

        int nGroups = datasets.length;
        List<Observation> data = prepareData(datasets);

        ChiSquaredDistribution chiDist = new ChiSquaredDistribution(nGroups - 1);
        double C = C(data);
        if (C == 0.0)
            return 1.0;

        double H = H(nGroups, data);
        return 1.0 - chiDist.cumulativeProbability(H / C);
    }

    /** Tests null hypothesis */
    public boolean kruskalWallisTest(double alpha, double[]... datasets) {
        return kruskalWallisTest(datasets) < alpha;
    }

    static class Observation {
        private double rank;
        private final double value;
        private final int group;

        Observation(double value, int group) {
            this.value = value;
            this.group = group;
        }
    }
}
