@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.RefGroup
import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.xdiscrete.LabelFormat.Companion.Formatted
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.positionJitter
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGBarPlotTest {

    private fun base() = GGBarPlot(
        TestData.toothGrowth,
        x = "dose", y = "len",
        statFun = StatFun.MeanStdErr,
        color = "black"
    ) {
        fill = "dose"
    } + addSummary(
        color = "black",
        statFun = StatFun.MeanStdErr,
        errorPlotType = ErrorPlotType.PointRange
    )

    private fun withCompareMeans() =
        base() + statCompareMeans(allComparisons = true) + statCompareMeans()

    private fun withCompareMeansRef() =
        base() + statCompareMeans(refGroup = RefGroup.all, labelPosFit = true) + statCompareMeans()

    private fun withFacet() =
        GGBarPlot(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
            facetBy = "chr1q21_status",
            facetNCol = 2,
            statFun = StatFun.MeanStdErr,
        ) {
            fill = "molecular_group"
        } + addSummary(
            statFun = StatFun.MeanStdErr, errorPlotType = ErrorPlotType.PointRange
        )

    private fun withFacetWithCompareMeans() =
        withFacet() + statCompareMeans(allComparisons = true) + statCompareMeans()

    private fun withFacetWithCompareMeansWithStripChart() =
        withFacet() + ggStrip(
            color = "black",
            position = positionJitter(0.1),
            size = 3.0
        ) + statCompareMeans(
            allComparisons = true,
            pAdjustMethod = null
        ) + statCompareMeans()

    private fun withGroup() = GGBarPlot(
        TestData.toothGrowth,
        x = "dose", y = "len",
        statFun = StatFun.MeanStdErr,
        position = Pos.dodge,
        color = "black"
    ) {
        fill = "supp"
    } + addSummary(
        color = "black",
        statFun = StatFun.MeanStdErr,
        errorPlotType = ErrorPlotType.PointRange
    )

    private fun withGroupWithCompareMeans() =
        withGroup() + statCompareMeans(labelPosFit = true, labelFormat = Formatted("p = {pValue}"))

    @Test
    fun test1() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            base(),
            withCompareMeans() + ggStrip(color = "black", position = positionJitter(0.1)),
            withCompareMeansRef(),
            withFacet(),
            withFacetWithCompareMeans(),
            withFacetWithCompareMeansWithStripChart(),
            withGroup(),
            withGroupWithCompareMeans()
        )
    }
}
