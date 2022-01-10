@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.writePDF
import jetbrains.letsPlot.positionDodge
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGStripChartTest {
    @Test
    internal fun testTooth() {
        val plt1 = GGStripChart(TestData.toothGrowth, x = "dose", y = "len", size = 3.0) {
            shape = "dose"
        }

        val plt2 = GGStripChart(
            TestData.toothGrowth, x = "dose", y = "len",
            size = 3.0,
            position = positionDodge(0.8)
        ) {
            shape = "supp"
            color = "supp"
        } + statCompareMeans() + addSummary(StatFun.MeanStdDev, ErrorPlotType.PointRange)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1.toPDF(),
            plt2.toPDF(),
        )
    }
}
