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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MannWhitneyUTest {
    @Test
    internal fun test1() {

        val a = doubleArrayOf(52168.0, 48923.0, 52201.0, 53905.0, 50853.0)
        val b = doubleArrayOf(43414.0, 42488.0, 39714.0, 42399.0, 39342.0, 42614.0)
        val c = doubleArrayOf(37757.0, 24898.0, 34296.0, 46734.0, 42468.0, 41020.0)

        Assertions.assertEquals(
            0.004329004329004329,
            MannWhitneyU().mannWhitneyUTest(a, b, true),
            1e-5
        )
        Assertions.assertEquals(
            0.004329004329004329,
            MannWhitneyU().mannWhitneyUTest(a, c, true),
            1e-5
        )
        Assertions.assertEquals(
            0.008113117265565767,
            MannWhitneyU().mannWhitneyUTest(a, b, false),
            1e-5
        )
    }
}