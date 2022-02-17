package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.color.Palletes.Categorical
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.ggsize
import org.jetbrains.kotlinx.dataframe.api.*
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
            TestData.sampleMatrix(10, 10), "x", "y", "z",
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

    @Test
    internal fun testDendro3() {
        val plt = Heatmap(
            TestData.sampleMatrix(15, 15), "x", "y", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
            .withBorder()
            .withColorKey(
                "xcat", Top,
                sep = 0.1, pallete = Categorical.Triadic9Bright,
                label = "X Category", labelPos = Left, labelSep = 0.2, labelSize = 2.0
            )
            .withColorKey(
                "ycat", Right,
                sep = 0.1, pallete = Categorical.Triadic9Bright,
                label = "Y Category", labelPos = Top, labelSep = 0.2, labelSize = 2.0, labelAngle = 90.0
            )
            .withDendrogram(Top)
            .withDendrogram(Right)
            .withLabels(Left, sep = 0.2)
            .withLabels(Bottom, sep = 0.2, angle = 45)

            .withFillLegend(Bottom, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")

        plt += ggsize(1000 / 1, 500 / 1)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testLegendTile() {
        val plt = Heatmap(
            TestData.sampleMatrix(15, 15), "x", "y", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
            .withBorder()
            .withFillLegend(Top, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withFillLegend(Right, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withFillLegend(Bottom, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withFillLegend(Left, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")


        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testLegendColorKey() {
        val plt = Heatmap(
            TestData.sampleMatrix(15, 15), "x", "y", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
            .debug()
            .withBorder()
            .withColorKey(
                "xcat", Top,
                sep = 0.1, pallete = Categorical.Triadic9Bright,
                label = "X Category", labelPos = Left, labelSep = 0.2, labelSize = 2.0
            )
            .withColorKey(
                "ycat", Right,
                sep = 0.1, pallete = Categorical.Triadic9Bright,
                label = "Y Category", labelPos = Top, labelSep = 0.2, labelSize = 2.0, labelAngle = 90.0
            )
            .withFillLegend(Top, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withColorKeyLegend(Top)

            .withColorKeyLegend(Right, sep = 1.0)
            .withFillLegend(Right, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")

            .withFillLegend(Bottom, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withColorKeyLegend(Bottom)

            .withFillLegend(Left, title = "Awesome Z label", textSize = 1.5, sizeUnit = "x")
            .withColorKeyLegend(Left)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testNA() {
        val data = TestData.sampleMatrix(15, 15)
            .filter { ("z"<Double>() * 99).toInt() % 10 != 3 }
            .update("z")
            .where { ("z"<Double>() * 99).toInt() % 10 == 2 }
            .withNull()

        val plt = Heatmap(
            data, "x", "y", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
            .withDendrogram(Top)
            .withDendrogram(Left)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun test1Row() {
        val data = TestData.sampleMatrix(1, 5)

        val plt = Heatmap(
            data, "x", "y", "z",
            xOrder = Hierarchical(),
            yOrder = Hierarchical()
        )
            .withDendrogram(Top)
            .withDendrogram(Left)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
