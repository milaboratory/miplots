package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.writePDF
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class HeatmapTest {
    private val simpleData = mapOf(
        "s" to listOf("C", "A", "A", "C", "A", "A", "C", "A", "A"),
        "x" to listOf("C", "B", "A", "C", "B", "A", "C", "B", "A"),

        "y" to listOf("Z", "Y", "X", "Y", "X", "Z", "X", "Y", "Z"),
        "z" to listOf(2, 3, 4, 5, 6, 7, 8, 1, 2),
    ).toDataFrame()

    fun simple() = Heatmap(
        simpleData, "x", "y", "z",
//        xOrder = Order.comparing { it["s"].toString() },
//        yOrder = Order.comparing { it["y"].toString() }
    )

    fun simpleWithLables() = simple()
        .withLabels(Position.Top)
        .withLabels(Position.Bottom)
        .withLabels(Position.Left)
        .withLabels(Position.Right)

    fun simpleWithLablesCustom1() = simple()
        .withLabels(Position.Top, labels = List(3) { "long word" })
        .withLabels(Position.Bottom, labels = List(3) { "long word" })
        .withLabels(Position.Left, labels = List(3) { "long word" })
        .withLabels(Position.Right, labels = List(3) { "long word" })

    fun simpleWithLablesCustom2() = simple()
        .withLabels(Position.Top, angle = 90, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Bottom, angle = 90, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Left, width = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Right, width = 2.0, labels = List(3) { "long word" })

    fun simpleWithLablesCustom3() = simple()
        .withLabels(Position.Top, angle = 45, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Right, angle = 45, width = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Bottom, angle = 45, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Position.Left, angle = 45, width = 2.0, labels = List(3) { "long word" })

    fun simpleWithColorKey() = simple()
        .withColorKey("s", Position.Top, label = "color key", labelPos = Position.Left)
        .withColorKey("x", Position.Top)
        .withColorKey("x", Position.Bottom, sep = 0.1)

    fun simpleWithColorKeyWithLabels() = simpleWithColorKey()
        .withLabels(Position.Top)
        .withLabels(Position.Bottom)
        .withLabels(Position.Left)
        .withLabels(Position.Right)

    fun simpleWithColorKey2() = simple()
        .withColorKey("s", Position.Top, label = "top color key", labelPos = Position.Left, sep = 0.1)
        .withColorKey("x", Position.Right, label = "right color key", labelPos = Position.Top, sep = 0.1)
        .withColorKey("x", Position.Bottom, label = "bottom color key", labelPos = Position.Right, sep = 0.1)
        .withColorKey("s", Position.Left, label = "left color key", labelPos = Position.Bottom, sep = 0.1)

    fun simpleWithColorKeyWithLabels2() = simpleWithColorKey2()
        .withLabels(Position.Top)
        .withLabels(Position.Bottom)
        .withLabels(Position.Left)
        .withLabels(Position.Right)

    fun simpleWithColorKey3() = simpleWithColorKey2()
        .withColorKey("s", Position.Top, label = "top color key", labelPos = Position.Left)
        .withColorKey("x", Position.Right, label = "right color key", labelPos = Position.Top)
        .withColorKey("x", Position.Bottom, label = "bottom color key", labelPos = Position.Right)
        .withColorKey("s", Position.Left, label = "left color key", labelPos = Position.Bottom)

    fun simpleWithColorKeyWithLabels3() = simpleWithColorKey3()
        .withLabels(Position.Top)
        .withLabels(Position.Bottom)
        .withLabels(Position.Left)
        .withLabels(Position.Right)

    @Test
    internal fun test1() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            simple(),
            simpleWithLables(),
            simpleWithLablesCustom1(),
            simpleWithLablesCustom2(),
            simpleWithLablesCustom3(),
            simpleWithColorKey(),
            simpleWithColorKeyWithLabels(),
            simpleWithColorKey2(),
            simpleWithColorKeyWithLabels2(),
            simpleWithColorKey3(),
            simpleWithColorKeyWithLabels3()
        )
    }


//    @Test
//    internal fun test1() {
//        val data = TestData.spinrates
//            .convert(column<Double>("velocity")).to<String>()
//            .convert(column<Double>("spinrate")).to<String>()
//
//        var plt = letsPlot(data.toMap())
//
//        plt += geomTile {
//            x = "velocity"
//            y = "spinrate"
//            fill = "swing_miss"
//        }
//
//        writePDF(
//            Paths.get("scratch/bp.pdf"),
//            plt.toPDF()
//        )
//    }
//
//    @Test
//    internal fun test2() {
//        val data = TestData.geneUsage
//
//        data.print()
//
//        var plt = letsPlot(data.toMap()) {
//            x = "sample"
//            y = "gene"
//            fill = "weight"
//        }
//
//        plt += geomTile(color = "white")
//
//        plt += coordFixed()
//
//        plt += facetWrap(facets = listOf("cell_type", "tissue"), nrow = 1)
//
//
//        val xLabs = data["sample"].distinct().toList().map { it.toString() }
//        val xCount = xLabs.size
//        val xLabMax = xLabs.maxOf { it?.length?.toDouble() ?: 0.0 }
//
//        val yLabs = data["gene"].distinct().toList().map { it.toString() }
//        val yCount = yLabs.size
//        val yLabMax = yLabs.maxOf { it?.length?.toDouble() ?: 0.0 }
//
//        val legendWidth = 70
//
//// The target tile size: 20x20
//        val height = 20 * yCount + xLabMax * 8
//        val width = 20 * xCount + yLabMax * 8 + legendWidth
//
//        plt += ggsize(width.toInt() * 4, height.toInt())
//
//        writePDF(
//            Paths.get("scratch/bp.pdf"),
//            plt.toPDF()
//        )
//    }
}
