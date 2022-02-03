@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.stat.util.NA
import com.milaboratory.miplots.stat.util.StatFun.MeanStdDev
import com.milaboratory.miplots.stat.xdiscrete.ErrorPlotType.*
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.positionDodge
import jetbrains.letsPlot.positionJitterDodge
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGStripChartTest {
    @Test
    internal fun examples() {
        val plt1 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
        } + addSummary()

        val plt2 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
        ) {
            shape = "supp"
            color = "supp"
            fill = "supp"
        } + statCompareMeans() + addSummary()

        val plt3 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
        ) {
            color = "dose"
        } + statCompareMeans() + addSummary(errorPlotType = Crossbar)

        val plt4 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "dose"
        } + addSummary(errorPlotType = ErrorBar)

        val plt5 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "dose"
        } + addSummary(errorPlotType = BoxPlot)


        val plt6 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "supp"
        } + addSummary(errorPlotType = BoxPlot, color = "black")

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
            plt3,
            plt4,
            plt5,
            plt6
        )
    }

    @Test
    internal fun testTooth() {
        val plt1 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
            position = positionJitterDodge(dodgeWidth = 0.2, jitterWidth = 0.2)
        ) {
            shape = "dose"
        }

        val plt2 = GGStripChart(
            TestData.toothGrowth, x = "dose", y = "len",
            size = 3.0,
            position = positionJitterDodge(dodgeWidth = 0.2, jitterWidth = 0.2)
        ) {
            shape = "supp"
            color = "supp"
        } + statCompareMeans() + addSummary(MeanStdDev, PointRange)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
        )
    }
}
