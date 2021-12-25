package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.util.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
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

    @Test
    internal fun test2() {
        val data = DataFrame.readTSV("https://raw.githubusercontent.com/kassambara/data/master/myeloma.txt")

        data.print()

        val plt = BoxPlot(
            data,
            x = "molecular_group",
            y = "IRF4",
            showOverallPValue = true,
            allComparisons = true
//            refGroup = RefGroup.all,
//            hideNS = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun test3() {
        val data =
            DataFrame.readCSV("https://raw.githubusercontent.com/vincentarelbundock/Rdatasets/master/csv/datasets/ToothGrowth.csv")
                .convert { column<Double>("dose") }.to<String>()

        data.print()

        val plt = BoxPlot(
            data,
            x = "dose",
            y = "len",
            showOverallPValue = true,
            allComparisons = true
//            refGroup = RefGroup.all,
//            hideNS = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

}
