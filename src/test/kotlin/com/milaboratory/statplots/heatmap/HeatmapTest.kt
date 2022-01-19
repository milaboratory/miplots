package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import jetbrains.letsPlot.*
import jetbrains.letsPlot.bistro.corr.CorrPlot
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.scale.scaleColorBrewer
import jetbrains.letsPlot.scale.scaleFillGradient
import jetbrains.letsPlot.scale.scaleXDiscrete
import org.jetbrains.kotlinx.dataframe.api.*
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class HeatmapTest {


    @Test
    internal fun test1() {
        val data = TestData.spinrates
            .convert(column<Double>("velocity")).to<String>()
            .convert(column<Double>("spinrate")).to<String>()

        var plt = letsPlot(data.toMap())

        plt += geomTile {
            x = "velocity"
            y = "spinrate"
            fill = "swing_miss"
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun test2() {
        val data = TestData.geneUsage

        data.print()

        var plt = letsPlot(data.toMap()) {
            x = "sample"
            y = "gene"
            fill = "weight"
        }

        plt += geomTile(color = "white")

        plt += coordFixed()

        plt += facetWrap(facets = listOf("cell_type", "tissue"), nrow = 1)


        val xLabs = data["sample"].distinct().toList().map { it.toString() }
        val xCount = xLabs.size
        val xLabMax = xLabs.maxOf { it?.length?.toDouble() ?: 0.0 }

        val yLabs = data["gene"].distinct().toList().map { it.toString() }
        val yCount = yLabs.size
        val yLabMax = yLabs.maxOf { it?.length?.toDouble() ?: 0.0 }

        val legendWidth = 70

// The target tile size: 20x20
        val height = 20 * yCount + xLabMax * 8
        val width = 20 * xCount + yLabMax * 8 + legendWidth

        plt += ggsize(width.toInt() * 4, height.toInt())

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun test3() {
        val data = mapOf(
            "s" to listOf("C", "A", "A", "C", "A", "A", "C", "A", "A"),
            "x" to listOf("C", "B", "A", "C", "B", "A", "C", "B", "A"),

            "y" to listOf("Z", "Y", "X", "Y", "X", "Z", "X", "Y", "Z"),
            "z" to listOf(2, 3, 4, 5, 6, 7, 8, 1, 2),
        ).toDataFrame()

        val plt = Heatmap(
            data, "x", "y", "z",
            xOrder = Order.comparing { it["s"].toString() },
            yOrder = Order.comparing { it["y"].toString() }
        )

        val xax = plt.xax + listOf("KK")
        plt.plot += scaleXDiscrete(
            breaks = xax, limits = xax,
            labels = xax.map { it.toString() },
            expand = listOf(0.0, 0.1),
        )
        plt.plot += scaleFillGradient(low = "red", high = "blue")

        plt.plot += geomTile(
            data = mapOf(
                "R" to listOf("KK", "KK", "KK"),
                "T" to listOf("X", "Y", "Z"),
                "U" to listOf("A", "D", "F")
            ),

            fill = "green",
            width = 0.9,
            height = 0.9,
            showLegend = true
        ) {
            x = "R"
            y = "T"
//            fill = "U"
        }

        plt.plot += scaleColorBrewer()
//        plt.plot += geomLe

//        plt.plot += scaleFillManual(
//            listOf("green", "gray"),
//            name = "U"
////            guide = "colorbar"
//        )
//
//        val rr = generateSequence(-1.0) { it + 0.1 }.takeWhile { it < 1.0 }.toList()
//
//        val xp = letsPlot(
//            mapOf(
//                "x" to rr,
//                "y" to List(rr.size) { 1 }
//            )
//        ) {
//            x = "x"
//            y = "y"
//        } + geomTile(height = 0.5) {
//            fill = "x"
//        } + themeClassic()


        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }


}
