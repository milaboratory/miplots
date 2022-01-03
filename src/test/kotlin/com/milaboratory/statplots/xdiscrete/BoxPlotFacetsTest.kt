package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class BoxPlotFacetsTest {
    @Test
    internal fun test1() {
        val plt = BoxPlot(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
            group = "chr1q21_status",
            facetWrap = true,
            ncol = 2,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun test2() {
        val plt = BoxPlot(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            group = "dose",
            facetWrap = true,
            ncol = 2,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }
}
