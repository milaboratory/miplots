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

import com.milaboratory.miplots.TestData
import org.junit.jupiter.api.Test

/**
 *
 */
internal class DescStatByTest {
    @Test
    internal fun test1() {
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).mean.toList())
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).std.toList())
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).err.toList())
    }
}
