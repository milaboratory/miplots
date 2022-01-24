package com.milaboratory.miplots.stat.util

import com.milaboratory.miplots.formatPValue
import org.apache.commons.math3.stat.inference.MannWhitneyUTest
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest
import org.junit.jupiter.api.Test

/**
 *
 */
internal class UtilKtTest {
    @Test
    internal fun test1() {
        println(formatPValue(123.0 / 1231238769324.0))
        println(formatPValue(11.0 / 23.0))
        println(formatPValue(11.0 / 23.0 / 5.0))
        println(formatPValue(11.0 / 23.0 / 7.0))
        println(formatPValue(11.0 / 23.0 / 7.0 / 7.0))
        println(formatPValue(11.0 / 23.0 / 7.0 / 8.0))
        println(formatPValue(11.0 / 23.0 / 7.0 / 11.0))
        println(formatPValue(11.0 / 23.0 / 7.0 / 11.0 / 2))
        println(formatPValue(11.0 / 23.0 / 7.0 / 11.0 / 4))
        println(formatPValue(11.0 / 23.0 / 7.0 / 11.0 / 8))
    }

    @Test
    internal fun asdasd() {
        val a = arrayOf(134.0, 1232.0, 343.0, 423.0, 541.0).toDoubleArray()
        val b = arrayOf(143.0, 212.0, 4313.0, 412.0, 534.0).toDoubleArray()

        println(WilcoxonSignedRankTest().wilcoxonSignedRankTest(a, b, false))
        println(WilcoxonSignedRankTest().wilcoxonSignedRankTest(a.sortedArray(), b, false))
        println(WilcoxonSignedRankTest().wilcoxonSignedRankTest(a, b.sortedArray(), false))
        println(WilcoxonSignedRankTest().wilcoxonSignedRankTest(a.sortedArray(), b.sortedArray(), false))

        println("-----")

        println(MannWhitneyUTest().mannWhitneyUTest(a, b))
        println(MannWhitneyUTest().mannWhitneyUTest(a.sortedArray(), b))
        println(MannWhitneyUTest().mannWhitneyUTest(a, b.sortedArray()))
        println(MannWhitneyUTest().mannWhitneyUTest(a.sortedArray(), b.sortedArray()))
    }
}
