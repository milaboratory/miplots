package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.util.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.update
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readTSV
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class BoxPlotTest {
    @Test
    internal fun test1() {
        val Y = "Y"
        val X = "X"
        val G = "G"

        val data = randomDataset(
            "Y" to Normal,
            "X" to Category(5),
            "G" to Category(2),
            len = 100
        )

        var plt = BoxPlot(data, X, Y, showOverallPValue = true, refGroup = RefGroup.all).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    val mieloma = DataFrame.readTSV("https://raw.githubusercontent.com/kassambara/data/master/myeloma.txt")
    val toothGrowth =
        DataFrame.readCSV("https://raw.githubusercontent.com/vincentarelbundock/Rdatasets/master/csv/datasets/ToothGrowth.csv")
            .convert { column<Double>("dose") }.to<String>()


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

    @Test
    internal fun testMieloma() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            mielomaEmpty().toPDF(),
            mielomaRefSign().toPDF(),
            mielomaRefPVal().toPDF(),
            mielomaAllComps().toPDF()
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
        method = TestMethod.KruskalWallis
    ).plot

    @Test
    internal fun testTooth() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            toothEmpty().toPDF(),
            toothRefSign().toPDF(),
            toothRefPVal().toPDF(),
            toothAllComps().toPDF()
        )
    }

    @Test
    internal fun test3() {
        val data = toothGrowth.update("len") { -(it as Double) }
        val plt = BoxPlot(
            data,
            x = "dose",
            y = "len",
            showOverallPValue = true,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

}
