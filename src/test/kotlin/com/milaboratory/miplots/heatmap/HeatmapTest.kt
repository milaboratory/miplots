package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.ggsize
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
        .withLabels(Top)
        .withLabels(Bottom)
        .withLabels(Left)
        .withLabels(Right)

    fun simpleWithLablesCustom1() = simple()
        .withLabels(Top, labels = List(3) { "long word" })
        .withLabels(Bottom, labels = List(3) { "long word" })
        .withLabels(Left, labels = List(3) { "long word" })
        .withLabels(Right, labels = List(3) { "long word" })

    fun simpleWithLablesCustom2() = simple()
        .withLabels(Top, angle = 90, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Bottom, angle = 90, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Left, width = 2.0, labels = List(3) { "long word" })
        .withLabels(Right, width = 2.0, labels = List(3) { "long word" })

    fun simpleWithLablesCustom3() = simple()
        .withLabels(Top, angle = 45, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Right, angle = 45, width = 2.0, labels = List(3) { "long word" })
        .withLabels(Bottom, angle = 45, height = 2.0, labels = List(3) { "long word" })
        .withLabels(Left, angle = 45, width = 2.0, labels = List(3) { "long word" })

    fun simpleWithColorKey() = simple()
        .withColorKey("s", Top, label = "color key", labelPos = Left)
        .withColorKey("x", Top)
        .withColorKey("x", Bottom, sep = 0.1)

    fun simpleWithColorKeyWithLabels() = simpleWithColorKey()
        .withLabels(Top)
        .withLabels(Bottom)
        .withLabels(Left)
        .withLabels(Right)

    fun simpleWithColorKey2() = simple()
        .withColorKey("s", Top, label = "top color key", labelPos = Left, sep = 0.1, labelSep = 0.1)
        .withColorKey("x", Right, label = "right color key", labelPos = Top, sep = 0.1, labelSep = 0.1)
        .withColorKey("x", Bottom, label = "bottom color key", labelPos = Right, sep = 0.1, labelSep = 0.1)
        .withColorKey("s", Left, label = "left color key", labelPos = Bottom, sep = 0.1, labelSep = 0.1)

    fun simpleWithColorKeyWithLabels2() = simpleWithColorKey2()
        .withLabels(Top)
        .withLabels(Bottom)
        .withLabels(Left)
        .withLabels(Right)

    fun simpleWithColorKey3() = simpleWithColorKey2()
        .withColorKey("s", Top, label = "top color key", labelPos = Left, labelSep = 0.1)
        .withColorKey("x", Right, label = "right color key", labelPos = Top, labelSep = 0.1)
        .withColorKey("x", Bottom, label = "bottom color key", labelPos = Right, labelSep = 0.1)
        .withColorKey("s", Left, label = "left color key", labelPos = Bottom, labelSep = 0.1)

    fun simpleWithColorKeyWithLabels3() = simpleWithColorKey3()
        .withLabels(Top)
        .withLabels(Bottom)
        .withLabels(Left)
        .withLabels(Right)

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

    @Test
    internal fun testDendro1() {
        val plt = Heatmap(
            TestData.mtcarsMatrix, "model", "option", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
        plt.withDendrogram(Top)
        plt.withDendrogram(Right)
        plt.withLabels(Left)
        plt.withLabels(Bottom, angle = 90, height = 3.0)
        plt += ggsize(1000, 500)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testDendro2() {
        val plt = Heatmap(
            TestData.mtcarsMatrix, "model", "option", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
        plt.withDendrogram(Top)
        plt.withDendrogram(Right)
        plt.withDendrogram(Bottom)
        plt.withDendrogram(Left)
        plt += ggsize(1000, 500)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
