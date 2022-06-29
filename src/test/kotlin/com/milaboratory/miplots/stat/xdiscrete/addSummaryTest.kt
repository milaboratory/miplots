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
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.writePDF
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class addSummaryTest {

    @Test
    fun test1() {
        val plt = GGStripChart(
            TestData.toothGrowth,
            x = "supp", y = "len", size = 3.0,
            facetBy = "dose"
        ) {
            shape = "supp"
        } + addSummary(
            statFun = StatFun.MeanRange,
            errorPlotType = ErrorPlotType.ErrorBar,
            width = 0.1
        )

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    fun test2() {
        val plt = GGPaired(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facetBy = "dose",
            facetNRow = 1,
            lineColor = "#aaaaaa"
        ) { color = "supp" }

        plt += addSummary(
            statFun = StatFun.MeanStdErr,
            errorPlotType = ErrorPlotType.PointRange,
            color = "black",
        )

        plt += statCompareMeans()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
