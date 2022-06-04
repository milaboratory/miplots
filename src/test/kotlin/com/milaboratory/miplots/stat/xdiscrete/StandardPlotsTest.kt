package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.intern.Plot
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class StandardPlotsTest {

    private fun test(type: StandardPlots.PlotType) {
        val list = mutableListOf<Plot>()
        if (type.noGroupingAllowed)
            list.add(
                type.plot(
                    TestData.toothGrowth,
                    y = "len"
                )
            )

        list.add(
            type.plot(
                TestData.toothGrowth,
                y = "len",
                primaryGroup = "dose",
            )
        )

        list.add(
            type.plot(
                TestData.toothGrowth,
                y = "len",
                primaryGroup = "dose",
                secondaryGroup = "supp"
            )
        )

        if (type.noGroupingAllowed)
            list.add(
                type.plot(
                    TestData.myeloma,
                    y = "DEPDC1"
                )
            )

        list.add(
            type.plot(
                TestData.myeloma,
                y = "DEPDC1",
                primaryGroup = "molecular_group"
            )
        )

        list.add(
            type.plot(
                TestData.myeloma,
                y = "DEPDC1",
                primaryGroup = "molecular_group",
                secondaryGroup = "chr1q21_status"
            )
        )

        writePDF(
            Paths.get("scratch/bp.pdf"),
            list
        )
    }

    @Test
    internal fun testBoxPlot() {
        test(StandardPlots.PlotType.BoxPlot)
    }

    @Test
    internal fun testBoxPlotBinDot() {
        test(StandardPlots.PlotType.BoxPlotBinDot)
    }

    @Test
    internal fun testBoxPlotJitter() {
        test(StandardPlots.PlotType.BoxPlotJitter)
    }

    @Test
    internal fun testViolin() {
        test(StandardPlots.PlotType.Violin)
    }

    @Test
    internal fun testViolinBinDot() {
        test(StandardPlots.PlotType.ViolinBinDot)
    }

    @Test
    internal fun testBarPlot() {
        test(StandardPlots.PlotType.BarPlot)
    }

    @Test
    internal fun testStackedBarPlot() {
        test(StandardPlots.PlotType.StackedBarPlot)
    }

    @Test
    internal fun testLine() {
        test(StandardPlots.PlotType.Line)
    }

    @Test
    internal fun testLineJitter() {
        test(StandardPlots.PlotType.LineJitter)
    }

    @Test
    internal fun testLineBinDot() {
        test(StandardPlots.PlotType.LineBinDot)
    }
}