package com.milaboratory.statplots.boxplot

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
        val plt = BoxPlotFacets(
            TestData.mieloma,
            x = "molecular_group",
            y = "IRF4",
            facet = "chr1q21_status",
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun test2() {
        val plt = BoxPlotFacets(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facet = "dose",
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }
}
