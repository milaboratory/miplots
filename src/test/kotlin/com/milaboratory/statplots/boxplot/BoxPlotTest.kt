package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.util.RefGroup
import com.milaboratory.statplots.util.TestData.Companion.mieloma
import com.milaboratory.statplots.util.TestData.Companion.toothGrowth
import com.milaboratory.statplots.util.TestMethod
import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class BoxPlotTest {
    fun mielomaEmpty() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
    ).plot

    fun mielomaRefSign() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        refGroup = RefGroup.all,
    ).plot

    fun mielomaRefPVal() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        refGroup = RefGroup.of("MAF"),
        labelFormat = Formatted("{pValue}")

    ).plot

    fun mielomaAllComps() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        allComparisons = true
    ).plot

    fun mielomaGroupped1() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        group = "chr1q21_status",
    ).plot

    @Test
    internal fun testMieloma() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            mielomaEmpty().toPDF(),
            mielomaRefSign().toPDF(),
            mielomaRefPVal().toPDF(),
            mielomaAllComps().toPDF(),
            mielomaGroupped1().toPDF()
        )
    }

    fun toothEmpty() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
    ).plot

    fun toothRefSign() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        refGroup = RefGroup.all,
    ).plot

    fun toothRefPVal() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        refGroup = RefGroup.of("1.0"),
        labelFormat = Formatted("{pValue}")

    ).plot

    fun toothAllComps() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        allComparisons = true,
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    ).plot

    fun toothGrouped() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        group = "supp",
        labelFormat = Formatted("p = {pValue}"),
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    ).plot


    @Test
    internal fun testTooth() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            toothEmpty().toPDF(),
            toothRefSign().toPDF(),
            toothRefPVal().toPDF(),
            toothAllComps().toPDF(),
            toothGrouped().toPDF()
        )
    }
}
