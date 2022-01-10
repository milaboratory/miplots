package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.writePDF
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class addSummaryTest {

    @Test
    fun test1() {
        val plt = ggStripChart(
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
            plt.toPDF()
        )
    }

    @Test
    fun test2() {
        val plt = ggPaired(
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
            plt.toPDF()
        )
    }
}
