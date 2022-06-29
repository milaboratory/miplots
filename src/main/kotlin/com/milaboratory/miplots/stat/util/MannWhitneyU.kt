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
package com.milaboratory.miplots.stat.util

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.ranking.NaNStrategy
import org.apache.commons.math3.stat.ranking.NaturalRanking
import org.apache.commons.math3.stat.ranking.TiesStrategy
import org.apache.commons.math3.util.FastMath
import java.util.*
import java.util.stream.LongStream
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt


class MannWhitneyU {

    /**
     * If the combined dataset contains no more values than this, test defaults to
     * exact test.
     */
    private val SMALL_SAMPLE_SIZE = 50

    /** Ranking algorithm.  */
    private val naturalRanking: NaturalRanking = NaturalRanking(
        NaNStrategy.FIXED,
        TiesStrategy.AVERAGE
    )

    /** Normal distribution  */
    private val standardNormal: NormalDistribution = NormalDistribution(0.0, 1.0)

    fun mannWhitneyU(x: DoubleArray, y: DoubleArray): Double {
        val z = concatenateSamples(x, y)
        val ranks = naturalRanking.rank(z)
        var sumRankX = 0.0

        /*
         * The ranks for x is in the first x.length entries in ranks because x
         * is in the first x.length entries in z
         */for (i in x.indices) {
            sumRankX += ranks[i]
        }

        /*
         * U1 = R1 - (n1 * (n1 + 1)) / 2 where R1 is sum of ranks for sample 1,
         * e.g. x, n1 is the number of observations in sample 1.
         */
        val U1 = sumRankX - x.size.toLong() * (x.size + 1) / 2

        /*
         * U1 + U2 = n1 * n2
         */
        val U2 = x.size.toLong() * y.size - U1
        return min(U1, U2)
    }

    private fun concatenateSamples(x: DoubleArray, y: DoubleArray): DoubleArray {
        val z = DoubleArray(x.size + y.size)
        System.arraycopy(x, 0, z, 0, x.size)
        System.arraycopy(y, 0, z, x.size, y.size)
        return z
    }

    fun mannWhitneyUTest(x: DoubleArray, y: DoubleArray): Double {
        // If samples are both small and there are no ties, perform exact test
        return if (x.size + y.size <= SMALL_SAMPLE_SIZE &&
            tiesMap(x, y).isEmpty()
        ) {
            mannWhitneyUTest(x, y, true)
        } else { // Normal approximation
            mannWhitneyUTest(x, y, false)
        }
    }

    fun mannWhitneyUTest(
        x: DoubleArray, y: DoubleArray,
        exact: Boolean
    ): Double {
        val tiesMap = tiesMap(x, y)
        val u = mannWhitneyU(x, y)
        if (exact) {
            if (tiesMap.isNotEmpty()) {
                throw IllegalArgumentException()
            }
            return exactP(x.size, y.size, u)
        }
        return approximateP(
            u, x.size, y.size,
            varU(x.size, y.size, tiesMap)
        )
    }


    private fun approximateP(
        u: Double, n1: Int, n2: Int,
        varU: Double
    ): Double {
        val mu = n1.toLong() * n2 / 2.0

        // If u == mu, return 1
        if (mu == u) {
            return 1.0
        }

        // Force z <= 0 so we get tail probability. Also apply continuity
        // correction
        val z: Double = -Math.abs(u - mu + 0.5) / sqrt(varU)
        return 2 * standardNormal.cumulativeProbability(z)
    }

    private fun exactP(n: Int, m: Int, u: Double): Double {
        val nm = (m * n).toDouble()
        if (u > nm) { // Quick exit if u is out of range
            return 1.0
        }
        // Need to convert u to a mean deviation, so cumulative probability is
        // tail probability
        val crit = if (u < nm / 2) u else nm / 2 - u
        var cum = 0.0
        var ct = 0
        while (ct <= crit) {
            cum += uDensity(n, m, ct.toDouble())
            ct++
        }
        return 2 * cum
    }

    private fun uDensity(n: Int, m: Int, u: Double): Double {
        if (u < 0 || u > m * n) {
            return 0.0
        }
        val freq = uFrequencies(n, m)
        return freq[round(u + 1).toInt()] / LongStream.of(*freq).sum().toDouble()
    }

    /**
     * Computes frequency counts for values of the Mann-Whitney U statistc. If
     * freq[] is the returned array, freq[u + 1] counts the frequency of U = u
     * among all possible n-m orderings. Therefore, P(u = U) = freq[u + 1] / sum
     * where sum is the sum of the values in the returned array.
     *
     *
     * Implements the algorithm presented in "Algorithm AS 62: A Generator for
     * the Sampling Distribution of the Mann-Whitney U Statistic", L. C. Dinneen
     * and B. C. Blakesley Journal of the Royal Statistical Society. Series C
     * (Applied Statistics) Vol. 22, No. 2 (1973), pp. 269-273.
     *
     * @param n first sample size
     * @param m second sample size
     * @return array of U statistic value frequencies
     */
    private fun uFrequencies(n: Int, m: Int): LongArray {
        val max: Int = FastMath.max(m, n)
        if (max > 100) {
            throw IllegalArgumentException()
        }
        val min: Int = FastMath.min(m, n)
        val out = LongArray(n * m + 2)
        val work = LongArray(n * m + 2)
        for (i in 1 until out.size) {
            out[i] = if (i <= max + 1) 1 else 0.toLong()
        }
        work[1] = 0
        var `in` = max
        for (i in 2..min) {
            work[i] = 0
            `in` = `in` + max
            var n1 = `in` + 2
            val l = (1 + `in` / 2).toLong()
            var k = i
            for (j in 1..l) {
                k++
                n1 = n1 - 1
                val sum = out[j.toInt()] + work[j.toInt()]
                out[j.toInt()] = sum
                work[k] = sum - out[n1]
                out[n1] = sum
            }
        }
        return out
    }

    private fun varU(
        n: Int, m: Int,
        tiesMap: Map<Double?, Int>
    ): Double {
        val nm = (n.toLong() * m).toDouble()
        if (tiesMap.isEmpty()) {
            return nm * (n + m + 1) / 12.0
        }
        val tSum = tiesMap.entries.stream()
            .mapToLong { (_, value): Map.Entry<Double?, Int> ->
                (value * value * value -
                        value).toLong()
            }
            .sum()
        val totalN = (n + m).toDouble()
        return nm / 12 * (totalN + 1 - tSum / (totalN * (totalN - 1)))
    }

    private fun tiesMap(x: DoubleArray, y: DoubleArray): Map<Double?, Int> {
        val tiesMap: MutableMap<Double?, Int> = TreeMap() // NOPMD - no concurrent access in the callers context
        for (i in x.indices) {
            tiesMap.merge(
                x[i], 1
            ) { a: Int?, b: Int? ->
                Integer.sum(
                    a!!,
                    b!!
                )
            }
        }
        for (i in y.indices) {
            tiesMap.merge(
                y[i], 1
            ) { a: Int?, b: Int? ->
                Integer.sum(
                    a!!,
                    b!!
                )
            }
        }
        tiesMap.entries.removeIf { (_, value): Map.Entry<Double?, Int> -> value == 1 }
        return tiesMap
    }
}