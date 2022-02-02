@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
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
        }

        val plt2 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
        ) {
            shape = "supp"
            color = "supp"
            fill = "supp"
        } + statCompareMeans() + addSummary(StatFun.MeanStdDev, ErrorPlotType.PointRange) {
            color = "supp"
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
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
        } + statCompareMeans() + addSummary(StatFun.MeanStdDev, ErrorPlotType.PointRange)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
        )
    }
}
