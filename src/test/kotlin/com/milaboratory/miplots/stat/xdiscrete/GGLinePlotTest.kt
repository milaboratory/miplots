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

import com.milaboratory.miplots.stat.util.RefGroup
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.positionJitter
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGLinePlotTest {

    private fun base() = GGLinePlot(
        TestData.toothGrowth,
        x = "dose", y = "len",
        statFun = StatFun.MeanStdErr,
        color = "black"
    ) + addSummary(
        color = "black",
        statFun = StatFun.MeanStdErr,
        errorPlotType = ErrorPlotType.PointRange
    )

    private fun withCompareMeansRef() =
        base() + statCompareMeans(refGroup = RefGroup.all, labelPosFit = true) + statCompareMeans()

    private fun withFacet() =
        GGLinePlot(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
            facetBy = "chr1q21_status",
            facetNCol = 2,
            statFun = StatFun.MeanStdErr,
        ) + addSummary(
            statFun = StatFun.MeanStdErr, errorPlotType = ErrorPlotType.PointRange
        )

    private fun withFacetWithCompareMeans() =
        withFacet() + statCompareMeans(refGroup = RefGroup.all, pAdjustMethod = null) + statCompareMeans()

    private fun withFacetWithCompareMeansWithStripChart() =
        withFacet() + ggStrip(
            color = "black",
            position = positionJitter(0.1),
            size = 3.0
        ) + statCompareMeans(
            refGroup = RefGroup.all,
            pAdjustMethod = null
        ) + statCompareMeans()

    private fun withGroup() = GGLinePlot(
        TestData.toothGrowth,
        x = "dose", y = "len",
        statFun = StatFun.MeanStdErr,
    ) {
        linetype = "supp"
        color = "supp"
    } + addSummary(
        color = "black",
        statFun = StatFun.MeanStdErr,
        errorPlotType = ErrorPlotType.PointRange
    )

    private fun withGroupWithCompareMeans() =
        withGroup() + statCompareMeans(
            labelPosFit = true,
        )

    @Test
    fun test1() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            base(),
            withCompareMeansRef(),
            withFacet(),
            withFacetWithCompareMeans(),
            withFacetWithCompareMeansWithStripChart(),
            withGroup(),
            withGroupWithCompareMeans()
        )
    }
}
