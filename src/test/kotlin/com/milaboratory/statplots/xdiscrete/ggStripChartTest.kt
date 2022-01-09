@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.ErrorFun
import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.writePDF
import jetbrains.letsPlot.positionDodge
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class ggStripChartTest {
    @Test
    internal fun testTooth() {
        val plt1 = ggStripChart(TestData.toothGrowth, x = "dose", y = "len", size = 3.0) {
            shape = "dose"
        }

        val plt2 = ggStripChart(
            TestData.toothGrowth, x = "dose", y = "len",
            size = 3.0,
            position = positionDodge(0.8)
        ) {
            shape = "supp"
            color = "supp"
        } + statCompareMeans() + addSummary(ErrorFun.MeanStdDev, ErrorPlotType.PointRange)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1.toPDF(),
            plt2.toPDF(),
        )
    }
}
