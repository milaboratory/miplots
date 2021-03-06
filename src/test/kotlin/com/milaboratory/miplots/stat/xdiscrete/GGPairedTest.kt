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
package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.stat.util.TestMethod
import com.milaboratory.miplots.writePDF
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.rowsReversed
import org.jetbrains.kotlinx.dataframe.api.toColumn
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGPairedTest {

    fun toothGrowthBase() = GGPaired(
        data = TestData.toothGrowth.rowsReversed().toDataFrame(),
        x = "supp",
        y = "len",
    ) {
        color = "supp"
    }

    fun toothGrowthWithPValue() = toothGrowthBase() + statCompareMeans(
        paired = true,
        method = TestMethod.Wilcoxon
    )

    fun toothGrowthFacetBase() = GGPaired(
        data = TestData.toothGrowth.rowsReversed().toDataFrame(),
        x = "supp",
        y = "len",
        facetBy = "dose",
        facetNRow = 1
    ) {
        color = "supp"
    }

    fun toothGrowthFacetWithPValue() = toothGrowthFacetBase() + statCompareMeans(
        paired = true,
        method = TestMethod.Wilcoxon
    )

    @Test
    internal fun toothGrowthAll() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            toothGrowthBase(),
            toothGrowthWithPValue(),
            toothGrowthFacetBase(),
            toothGrowthFacetWithPValue()
        )
    }

    @Test
    internal fun test2() {
        val a = listOf(200.1, 190.9, 192.7, 213, 241.4, 196.9, 172.2, 185.5, 205.2, 193.7)
        val b = listOf(392.9, 393.2, 345.1, 393, 434, 427.9, 422, 383.9, 392.3, 352.2)
        val df = dataFrameOf(a.toColumn("A"), b.toColumn("B"))

        val plt = GGPaired(df, color = "black", cond1 = "A", cond2 = "B") {
            fill = "condition"
        }

        plt += statCompareMeans(paired = true)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
