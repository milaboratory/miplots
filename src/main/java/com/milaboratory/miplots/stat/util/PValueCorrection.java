/*
 *
 * Copyright (c) 2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/miplots/blob/main/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.miplots.stat.util;

import java.util.Arrays;
import java.util.Comparator;

public final class PValueCorrection {
    private PValueCorrection() {
    }

    private static int[] seqLen(int start, int end) {
        int[] result;
        if (start == end) {
            result = new int[end + 1];
            for (int i = 0; i < result.length; ++i) {
                result[i] = i + 1;
            }
        } else if (start < end) {
            result = new int[end - start + 1];
            for (int i = 0; i < result.length; ++i) {
                result[i] = start + i;
            }
        } else {
            result = new int[start - end + 1];
            for (int i = 0; i < result.length; ++i) {
                result[i] = start - i;
            }
        }
        return result;
    }

    private static int[] order(double[] array, boolean decreasing) {
        int size = array.length;
        int[] idx = new int[size];
        double[] baseArr = new double[size];
        for (int i = 0; i < size; ++i) {
            baseArr[i] = array[i];
            idx[i] = i;
        }

        Comparator<Integer> cmp;
        if (!decreasing) {
            cmp = Comparator.comparingDouble(a -> baseArr[a]);
        } else {
            cmp = (a, b) -> Double.compare(baseArr[b], baseArr[a]);
        }

        return Arrays.stream(idx)
                .boxed()
                .sorted(cmp)
                .mapToInt(a -> a)
                .toArray();
    }

    private static double[] cummin(double[] array) {
        if (array.length < 1)
            throw new IllegalArgumentException("cummin requires at least one element");
        double[] output = new double[array.length];
        double cumulativeMin = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] < cumulativeMin)
                cumulativeMin = array[i];
            output[i] = cumulativeMin;
        }
        return output;
    }

    private static double[] cummax(double[] array) {
        if (array.length < 1)
            throw new IllegalArgumentException("cummax requires at least one element");
        double[] output = new double[array.length];
        double cumulativeMax = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] > cumulativeMax)
                cumulativeMax = array[i];
            output[i] = cumulativeMax;
        }
        return output;
    }

    private static double[] pminx(double[] array, double x) {
        if (array.length < 1)
            throw new IllegalArgumentException("pmin requires at least one element");
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = Math.min(array[i], x);
        }
        return result;
    }

    private static double[] int2double(int[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private static double doubleArrayMin(double[] array) {
        if (array.length < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");
        return Arrays.stream(array).min().orElse(Double.NaN);
    }

    public static double[] Bonferoni(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");

        double[] result = new double[size];
        for (int i = 0; i < size; ++i) {
            double b = pvalues[i] * size;
            if (b >= 1) {
                result[i] = 1;
            } else if (0 <= b) {
                result[i] = b;
            } else {
                throw new RuntimeException("" + b + " is outside [0, 1)");
            }
        }
        return result;
    }

    public static double[] Holm(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");

        int[] o = order(pvalues, false);
        double[] o2Double = int2double(o);
        double[] cummaxInput = new double[size];
        for (int i = 0; i < size; ++i) {
            cummaxInput[i] = (size - i) * pvalues[o[i]];
        }
        int[] ro = order(o2Double, false);
        double[] cummaxOutput = cummax(cummaxInput);
        double[] pmin = pminx(cummaxOutput, 1.0);
        double[] result = new double[size];
        for (int i = 0; i < size; ++i) {
            result[i] = pmin[ro[i]];
        }
        return result;
    }

    public static double[] Hommel(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");
        int[] indices = seqLen(size, size);
        int[] o = order(pvalues, false);
        double[] p = new double[size];
        for (int i = 0; i < size; ++i) {
            p[i] = pvalues[o[i]];
        }
        double[] o2Double = int2double(o);
        int[] ro = order(o2Double, false);
        double[] q = new double[size];
        double[] pa = new double[size];
        double[] npi = new double[size];
        for (int i = 0; i < size; ++i) {
            npi[i] = p[i] * size / indices[i];
        }
        double min = doubleArrayMin(npi);
        Arrays.fill(q, min);
        Arrays.fill(pa, min);
        for (int j = size; j >= 2; --j) {
            int[] ij = seqLen(1, size - j + 1);
            for (int i = 0; i < size - j + 1; ++i) {
                ij[i]--;
            }
            int i2Length = j - 1;
            int[] i2 = new int[i2Length];
            for (int i = 0; i < i2Length; ++i) {
                i2[i] = size - j + 2 + i - 1;
            }
            double q1 = j * p[i2[0]] / 2.0;
            for (int i = 1; i < i2Length; ++i) {
                double temp_q1 = p[i2[i]] * j / (2.0 + i);
                if (temp_q1 < q1) q1 = temp_q1;
            }
            for (int i = 0; i < size - j + 1; ++i) {
                q[ij[i]] = Math.min(p[ij[i]] * j, q1);
            }
            for (int i = 0; i < i2Length; ++i) {
                q[i2[i]] = q[size - j];
            }
            for (int i = 0; i < size; ++i) {
                if (pa[i] < q[i]) {
                    pa[i] = q[i];
                }
            }
        }
        for (int i = 0; i < size; ++i) {
            q[i] = pa[ro[i]];
        }
        return q;
    }

    private static final class IntermediateData {
        final double[] ni;
        final int[] o, ro;

        public IntermediateData(double[] ni, int[] o, int[] ro) {
            this.ni = ni;
            this.o = o;
            this.ro = ro;
        }

        double[] result(double[] cumminInput) {
            double[] cumminArray = cummin(cumminInput);
            double[] pmin = pminx(cumminArray, 1.0);
            double[] result = new double[cumminInput.length];
            for (int i = 0; i < cumminInput.length; ++i)
                result[i] = pmin[ro[i]];
            return result;
        }
    }

    private static IntermediateData computeIntermediateData(double[] pvalues) {
        int size = pvalues.length;
        double[] ni = new double[size];
        int[] o = order(pvalues, true);
        double[] oDouble = int2double(o);
        for (int i = 0; i < size; ++i) {
            if (pvalues[i] < 0 || pvalues[i] > 1) {
                throw new RuntimeException("array[" + i + "] = " + pvalues[i] + " is outside [0, 1]");
            }
            ni[i] = (double) size / (size - i);
        }
        int[] ro = order(oDouble, false);
        return new IntermediateData(ni, o, ro);
    }

    public static double[] BenjaminiHochberg(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");

        IntermediateData data = computeIntermediateData(pvalues);
        double[] cumminInput = new double[size];
        for (int i = 0; i < size; ++i)
            cumminInput[i] = data.ni[i] * pvalues[data.o[i]];
        return data.result(cumminInput);
    }


    public static double[] BenjaminiYekutieli(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");

        IntermediateData data = computeIntermediateData(pvalues);
        double[] cumminInput = new double[size];
        double q = 0;
        for (int i = 1; i < size + 1; ++i)
            q += 1.0 / i;
        for (int i = 0; i < size; ++i)
            cumminInput[i] = q * data.ni[i] * pvalues[data.o[i]];
        return data.result(cumminInput);
    }

    public static double[] Hochberg(double[] pvalues) {
        int size = pvalues.length;
        if (size < 1)
            throw new IllegalArgumentException("pAdjust requires at least one element");

        IntermediateData data = computeIntermediateData(pvalues);
        double[] cumminInput = new double[size];
        for (int i = 0; i < size; ++i)
            cumminInput[i] = (i + 1) * pvalues[data.o[i]];
        return data.result(cumminInput);
    }


    /**
     * P-value correction method
     */
    public enum Method {
        /**
         * False discovery rate: Yoav Benjamini, Yosef Hochberg "Controlling the False Discovery Rate: A Practical and Powerful Approach to Multiple Testing", Journal of the Royal Statistical Society. Series B, Vol. 57, No. 1 (1995), pp. 289-300, JSTOR:2346101
         */
        BenjaminiHochberg,
        /**
         * Yoav Benjamini, Daniel Yekutieli, "The control of the false discovery rate in multiple testing under dependency", Ann. Statist., Vol. 29, No. 4 (2001), pp. 1165-1188, DOI:10.1214/aos/1013699998 JSTOR:2674075
         */
        BenjaminiYekutieli,
        /**
         * https://en.wikipedia.org/wiki/Holm%E2%80%93Bonferroni_method
         */
        Bonferroni,
        /**
         * Yosef Hochberg, "A sharper Bonferroni procedure for multiple tests of significance", Biometrika, Vol. 75, No. 4 (1988), pp 800–802, DOI:10.1093/biomet/75.4.800 JSTOR:2336325
         */
        Hochberg,
        /**
         * Sture Holm, "A Simple Sequentially Rejective Multiple Test Procedure", Scandinavian Journal of Statistics, Vol. 6, No. 2 (1979), pp. 65-70, JSTOR:4615733
         */
        Holm,
        /**
         * Gerhard Hommel, "A stagewise rejective multiple test procedure based on a modified Bonferroni test", Biometrika, Vol. 75, No. 2 (1988), pp 383–386, DOI:10.1093/biomet/75.2.383 JSTOR:2336190
         */
        Hommel,
    }

    public static double[] adjustPValues(double[] pValues, Method method) {
        switch (method) {
            case BenjaminiHochberg: {
                return BenjaminiHochberg(pValues);
            }
            case BenjaminiYekutieli: {
                return BenjaminiYekutieli(pValues);
            }
            case Bonferroni: {
                return Bonferoni(pValues);
            }
            case Hochberg: {
                return Hochberg(pValues);
            }
            case Holm: {
                return Holm(pValues);
            }
            case Hommel: {
                return Hommel(pValues);
            }
            default:
                throw new RuntimeException();
        }
    }
}
