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
package com.milaboratory.miplots

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
            ).plot
        )

        list.add(
            type.plot(
                TestData.toothGrowth,
                y = "len",
                primaryGroup = "dose",
                secondaryGroup = "supp"
            ).plot
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
            ).plot
        )

        list.add(
            type.plot(
                TestData.myeloma,
                y = "DEPDC1",
                primaryGroup = "molecular_group",
                secondaryGroup = "chr1q21_status"
            ).plot
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