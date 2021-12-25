package com.milaboratory.statplots.util

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
}
