package com.milaboratory.miplots.stat.util;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of Kendall's Tau-b rank correlation</a>.
 * <p>
 * A pair of observations (x<sub>1</sub>, y<sub>1</sub>) and (x<sub>2</sub>, y<sub>2</sub>) are considered
 * <i>concordant</i> if x<sub>1</sub> &lt; x<sub>2</sub> and y<sub>1</sub> &lt; y<sub>2</sub> or x<sub>2</sub> &lt;
 * x<sub>1</sub> and y<sub>2</sub> &lt; y<sub>1</sub>. The pair is <i>discordant</i> if x<sub>1</sub> &lt; x<sub>2</sub>
 * and y<sub>2</sub> &lt; y<sub>1</sub> or x<sub>2</sub> &lt; x<sub>1</sub> and y<sub>1</sub> &lt; y<sub>2</sub>.  If
 * either x<sub>1</sub> = x<sub>2</sub> or y<sub>1</sub> = y<sub>2</sub>, the pair is neither concordant nor
 * discordant.
 * <p>
 * Kendall's Tau-b is defined as:
 * <pre>
 * tau<sub>b</sub> = (n<sub>c</sub> - n<sub>d</sub>) / sqrt((n<sub>0</sub> - n<sub>1</sub>) * (n<sub>0</sub> - n<sub>2</sub>))
 * </pre>
 * <p>
 * where:
 * <ul>
 *     <li>n<sub>0</sub> = n * (n - 1) / 2</li>
 *     <li>n<sub>c</sub> = Number of concordant pairs</li>
 *     <li>n<sub>d</sub> = Number of discordant pairs</li>
 *     <li>n<sub>1</sub> = sum of t<sub>i</sub> * (t<sub>i</sub> - 1) / 2 for all i</li>
 *     <li>n<sub>2</sub> = sum of u<sub>j</sub> * (u<sub>j</sub> - 1) / 2 for all j</li>
 *     <li>t<sub>i</sub> = Number of tied values in the i<sup>th</sup> group of ties in x</li>
 *     <li>u<sub>j</sub> = Number of tied values in the j<sup>th</sup> group of ties in y</li>
 * </ul>
 * <p>
 * This implementation uses the O(n log n) algorithm described in
 * William R. Knight's 1966 paper "A Computer Method for Calculating
 * Kendall's Tau with Ungrouped Data" in the Journal of the American
 * Statistical Association.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Kendall_tau_rank_correlation_coefficient">
 *         Kendall tau rank correlation coefficient (Wikipedia)</a>
 * @see <a href="http://www.jstor.org/stable/2282833">A Computer
 *         Method for Calculating Kendall's Tau with Ungrouped Data</a>
 * @since 3.3
 */
public class KendallsCorrelation {
    /** tau_b */
    public final double tau;
    /** z-score */
    public final double significance;
    /** two-tailed p-value */
    public final double pValue;

    /**
     * Computes the Kendall's Tau rank correlation coefficient between the two arrays.
     *
     * @param xArray first data array
     * @param yArray second data array
     * @throws DimensionMismatchException if the arrays lengths do not match
     */
    public KendallsCorrelation(final double[] xArray, final double[] yArray)
            throws DimensionMismatchException {

        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
        }

        final int n = xArray.length;
        final long numPairs = sum(n - 1);

        @SuppressWarnings("unchecked")
        Pair<Double, Double>[] pairs = new Pair[n];
        for (int i = 0; i < n; i++) {
            pairs[i] = new Pair<>(xArray[i], yArray[i]);
        }

        Arrays.sort(pairs, new Comparator<Pair<Double, Double>>() {
            /** {@inheritDoc} */
            public int compare(Pair<Double, Double> pair1, Pair<Double, Double> pair2) {
                int compareFirst = pair1.getFirst().compareTo(pair2.getFirst());
                return compareFirst != 0 ? compareFirst : pair1.getSecond().compareTo(pair2.getSecond());
            }
        });

        long v1_part_1 = 0;
        long v2_part_1 = 0;
        long vt = 0;

        long tiedXPairs = 0;
        long tiedXYPairs = 0;
        long consecutiveXTies = 1;
        long consecutiveXYTies = 1;
        Pair<Double, Double> prev = pairs[0];
        for (int i = 1; i < n; i++) {
            final Pair<Double, Double> curr = pairs[i];
            if (curr.getFirst().equals(prev.getFirst())) {
                consecutiveXTies++;
                if (curr.getSecond().equals(prev.getSecond())) {
                    consecutiveXYTies++;
                } else {
                    tiedXYPairs += sum(consecutiveXYTies - 1);
                    consecutiveXYTies = 1;
                }
            } else {

                vt += consecutiveXTies * (consecutiveXTies - 1) * (2 * consecutiveXTies + 5);
                v1_part_1 += consecutiveXTies * (consecutiveXTies - 1);
                v2_part_1 += consecutiveXTies * (consecutiveXTies - 1) * (consecutiveXTies - 2);

                tiedXPairs += sum(consecutiveXTies - 1);
                consecutiveXTies = 1;
                tiedXYPairs += sum(consecutiveXYTies - 1);
                consecutiveXYTies = 1;
            }
            prev = curr;
        }
        vt += consecutiveXTies * (consecutiveXTies - 1) * (2 * consecutiveXTies + 5);
        v1_part_1 += consecutiveXTies * (consecutiveXTies - 1);
        v2_part_1 += consecutiveXTies * (consecutiveXTies - 1) * (consecutiveXTies - 2);

        tiedXPairs += sum(consecutiveXTies - 1);
        tiedXYPairs += sum(consecutiveXYTies - 1);

        long swaps = 0;
        @SuppressWarnings("unchecked")
        Pair<Double, Double>[] pairsDestination = new Pair[n];
        for (int segmentSize = 1; segmentSize < n; segmentSize <<= 1) {
            for (int offset = 0; offset < n; offset += 2 * segmentSize) {
                int i = offset;
                final int iEnd = FastMath.min(i + segmentSize, n);
                int j = iEnd;
                final int jEnd = FastMath.min(j + segmentSize, n);

                int copyLocation = offset;
                while (i < iEnd || j < jEnd) {
                    if (i < iEnd) {
                        if (j < jEnd) {
                            if (pairs[i].getSecond().compareTo(pairs[j].getSecond()) <= 0) {
                                pairsDestination[copyLocation] = pairs[i];
                                i++;
                            } else {
                                pairsDestination[copyLocation] = pairs[j];
                                j++;
                                swaps += iEnd - i;
                            }
                        } else {
                            pairsDestination[copyLocation] = pairs[i];
                            i++;
                        }
                    } else {
                        pairsDestination[copyLocation] = pairs[j];
                        j++;
                    }
                    copyLocation++;
                }
            }
            final Pair<Double, Double>[] pairsTemp = pairs;
            pairs = pairsDestination;
            pairsDestination = pairsTemp;
        }

        long v1_part_2 = 0;
        long v2_part_2 = 0;
        long vu = 0;

        long tiedYPairs = 0;
        long consecutiveYTies = 1;
        prev = pairs[0];
        for (int i = 1; i < n; i++) {
            final Pair<Double, Double> curr = pairs[i];
            if (curr.getSecond().equals(prev.getSecond())) {
                consecutiveYTies++;
            } else {
                vu += consecutiveYTies * (consecutiveYTies - 1) * (2 * consecutiveYTies + 5);
                v1_part_2 += consecutiveYTies * (consecutiveYTies - 1);
                v2_part_2 += consecutiveYTies * (consecutiveYTies - 1) * (consecutiveYTies - 2);

                tiedYPairs += sum(consecutiveYTies - 1);
                consecutiveYTies = 1;
            }
            prev = curr;
        }
        vu += consecutiveYTies * (consecutiveYTies - 1) * (2 * consecutiveYTies + 5);
        v1_part_2 += consecutiveYTies * (consecutiveYTies - 1);
        v2_part_2 += consecutiveYTies * (consecutiveYTies - 1) * (consecutiveYTies - 2);

        tiedYPairs += sum(consecutiveYTies - 1);

        final long concordantMinusDiscordant = numPairs - tiedXPairs - tiedYPairs + tiedXYPairs - 2 * swaps;
        final double nonTiedPairsMultiplied = (numPairs - tiedXPairs) * (double) (numPairs - tiedYPairs);
        double tau_b = concordantMinusDiscordant / FastMath.sqrt(nonTiedPairsMultiplied);


        // Significance
        double v0 = (n * (n - 1)) * (2 * n + 5);
        double n_f = 1.0 * n;
        double v1 = (v1_part_1 * v1_part_2);
        double v2 = (v2_part_1 * v2_part_2);
        double var_s = (v0 - vt - vu) / 18.0 + v1 / (2.0 * n_f * (n_f - 1.0)) + v2 / (9.0 * n_f * (n_f - 1.0) * (n_f - 2.0));

        double s = tau_b * Math.sqrt(nonTiedPairsMultiplied);
        double z = s / Math.sqrt(var_s);

        this.tau = tau_b;
        this.significance = z;
        NormalDistribution dist = new NormalDistribution();
        this.pValue = 2 * dist.cumulativeProbability(-Math.abs(significance));
    }


    /**
     * Returns the sum of the number from 1 .. n according to Gauss' summation formula: \[ \sum\limits_{k=1}^n k =
     * \frac{n(n + 1)}{2} \]
     *
     * @param n the summation end
     * @return the sum of the number from 1 to n
     */
    private static long sum(long n) {
        return n * (n + 1) / 2L;
    }
}
