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

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.apache.commons.math3.stat.inference.OneWayAnova
import org.apache.commons.math3.stat.inference.TTest
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest

/** Method for calculation of p-value */
enum class TestMethod(val multipleGroups: Boolean, val supportPaired: Boolean, val str: String) {
    TTest(false, true, "T-test") {
        override fun pValue(vararg arr: DoubleArray, paired: Boolean): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return if (paired)
                TTest().pairedTTest(a, b)
            else
                TTest().tTest(a, b)
        }
    },
    Wilcoxon(false, true, "Wilcoxon") {
        override fun pValue(vararg arr: DoubleArray, paired: Boolean): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return if (a.size != b.size)
                MannWhitneyU().mannWhitneyUTest(a, b)
            else
                return if (paired)
                    WilcoxonSignedRankTest().wilcoxonSignedRankTest(a, b, false)
                else
                    MannWhitneyU().mannWhitneyUTest(a, b)
        }
    },
    ANOVA(true, false, "Anova") {
        override fun pValue(vararg arr: DoubleArray, paired: Boolean) =
            OneWayAnova().anovaPValue(arr.toList())

    },
    KruskalWallis(true, false, "Kruskal-Wallis") {
        override fun pValue(vararg arr: DoubleArray, paired: Boolean) =
            KruskalWallis().kruskalWallisTest(arr.toList())
    },
    KolmogorovSmirnov(true, false, "Kolmogorov-Smirnov") {
        override fun pValue(vararg arr: DoubleArray, paired: Boolean): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return KolmogorovSmirnovTest().kolmogorovSmirnovTest(a, b)
        }
    };

    override fun toString(): String {
        return str
    }

    abstract fun pValue(vararg arr: DoubleArray, paired: Boolean): Double
}