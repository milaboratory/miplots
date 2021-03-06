/*
 *
 * Copyright (c) 2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/miplots/blob/main/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
@file:Suppress("ClassName")

package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.dendro.ConnectionType.Rectangle
import com.milaboratory.miplots.dendro.ConnectionType.Triangle
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.alignment
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.isotype
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.label
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.node
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.root
import com.milaboratory.miplots.dendro.GGDendroTest.TestNodeUtil.text
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.themeClassic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.math.abs

/**
 *
 */
internal class GGDendroTest {
    private val tree =
        Node(0.0, "age" to "12", "sex" to "m") {
            Node(1.5, "age" to "10", "sex" to "m") {
                Node(1.5, "age" to "10", "sex" to "m")
                Node(1.5, "age" to "10", "sex" to "m")
            }
            Node(2.1, "age" to "11", "sex" to "f") {
                Node(1.5, "age" to "10", "sex" to "m")
                Node(5.5, "age" to "10", "sex" to "m")
            }
        }

    @Test
    internal fun test0() {
        Assertions.assertEquals(7.6, tree.xy().height)
    }

    @Test
    internal fun test1() {
        val plot = GGDendroPlot(
            tree,
            ctype = Triangle,
            einh = EdgeMetaInheritance.Up
        ) {
            color = "age"
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plot
        )
    }

    @Test
    internal fun test2() {
        val plot = GGDendroPlot(
            tree,
            ctype = Rectangle,
            einh = EdgeMetaInheritance.Up
        ) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plot
        )
    }

    @Test
    internal fun testRootPos() {
        val plots = ConnectionType.values().flatMap { ct ->
            listOf(
                GGDendroPlot(tree, ctype = ct, rpos = Top),
                GGDendroPlot(tree, ctype = ct, rpos = Right),
                GGDendroPlot(tree, ctype = ct, rpos = Bottom),
                GGDendroPlot(tree, ctype = ct, rpos = Left)
            )
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testImposeX() {
        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = GGDendroPlot(tree, coord = coord) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testImposeX2() {
        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = GGDendroPlot(tree, coord = coord, rpos = Left) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testHeight() {
        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = GGDendroPlot(tree, coord = coord, height = 2.0, rpos = Left) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testBalanced() {
        val plots = ConnectionType.values().flatMap { ct ->
            listOf(
                GGDendroPlot(tree, ctype = ct, rpos = Top, balanced = true),
                GGDendroPlot(tree, ctype = ct, rpos = Right, balanced = true),
                GGDendroPlot(tree, ctype = ct, rpos = Bottom, balanced = true),
                GGDendroPlot(tree, ctype = ct, rpos = Left, balanced = true)
            )
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testTrivial() {
        val tree1 = Node(0.0) {
            Node(1.0)
            Node(1.0)
            Node(1.0)
        }

        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = GGDendroPlot(tree1, coord = coord, height = 2.0, rpos = Top, ctype = Rectangle) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testRect() {
        val plt1 = GGDendroPlot(
            tree,
            ctype = Triangle,
            rpos = Right,
            nodeSize = 0.10,
            nodeColor = "red",
            lineWidth = 0.05,
        )

        val plt2 = GGDendroPlot(
            tree,
            ctype = Triangle,
            rpos = Top,
            nodeSize = 0.10,
            nodeColor = "red",
            lineWidth = 0.05,
            balanced = true
        )

        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt3 = GGDendroPlot(
            tree,
            ctype = Triangle,
            coord = coord,
            nodeSize = 0.0,
            nodeColor = "red",
            lineWidth = 5.0,
        ) + themeClassic()

        val plt4 = GGDendroPlot(
            tree,
            ctype = Triangle,
            coord = coord,
            nodeSize = 0.0,
            nodeColor = "red",
            lineWidth = 5.0,
            balanced = true
        ) + themeClassic()

        val plt5 = GGDendroPlot(
            tree,
            rpos = Left,
            ctype = Triangle,
            coord = coord,
            nodeSize = 0.0,
            nodeColor = "red",
            lineWidth = 5.0,
            balanced = true
        ) + themeClassic()

        val plt6 = GGDendroPlot(
            tree,
            ctype = Rectangle,
            rpos = Top,
            nodeSize = 0.10,
            nodeColor = "red",
            lineWidth = 0.05,
        )

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
            plt3,
            plt4,
            plt5,
            plt6
        )
    }

    @Test
    internal fun testLinewidth() {
        val tree =
            Node(20.0) {
                Node(130.0) {
                    Node(1.5)
                    Node(1.5)
                }
                Node(130.0) {
                    Node(1.5)
                    Node(1.5)
                }
            }

        val coord = listOf(0.0, 50.0, 100.0, 150.0)
        val plots = Position.values().map { pos ->
            GGDendroPlot(tree, ctype = Rectangle, coord = coord, rpos = pos, lineWidth = 10.0) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testRShift() {
        val tree =
            Node(0.0) {
                Node(1.0) {
                    Node(1.5)
                    Node(2.5)
                }
                Node(2.0) {
                    Node(2.5)
                    Node(3.5)
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(tree, ctype = Rectangle, rshift = tree.totalHeight, rpos = pos) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testRShiftHeight() {
        val tree =
            Node(0.0) {
                Node(1.0) {
                    Node(1.5)
                    Node(2.5)
                }
                Node(2.0) {
                    Node(2.5)
                    Node(3.5)
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(tree, ctype = Rectangle, rshift = tree.totalHeight, height = 1.0, rpos = pos) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testSingleChild1() {
        val tree =
            Node(0.0) {
                Node(1.0) {
                    Node(1.5)
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(tree, ctype = Rectangle, height = 1.0, rpos = pos) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testSingleChild2() {
        val tree =
            Node(0.0) {
                Node(1.0) {
                    Node(1.5)
                    Node(3.5)
                }
                Node(1.0) {
                    Node(4.0)
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(tree, ctype = Rectangle, height = 1.0, rpos = pos) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testColor() {
        val igg = "igg"
        val igm = "igm"
        val iga = "iga"
        val isotype = "isotype"

        val tree =
            Node(0.0, isotype to igg) {
                Node(1.0, isotype to igg) {
                    Node(1.5, isotype to igm)
                    Node(3.5, isotype to iga)
                }
                Node(1.0, isotype to iga) {
                    Node(4.0, isotype to igm)
                    Node(4.0, isotype to igm)
                }
            }

        val plots =
            GGDendroPlot(tree, height = 1.0, lineWidth = 0.05, nodeSize = 0.2, lineColor = "#aaaaaa") {
                color = isotype
            }


        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testBig1() {
        val igg = "igg"
        val igm = "igm"
        val iga = "iga"
        val isotype = "isotype"
        val abundance = "abundance"

        val rnd = kotlin.random.Random(System.currentTimeMillis())
        fun ab() = Math.log10(1.0 + (abs(rnd.nextInt()) % 10))
        val tree =
            Node(0.0, isotype to igg, abundance to ab()) {
                Node(1.0, isotype to igg, abundance to ab()) {
                    Node(1.5, isotype to igm, abundance to ab()) {
                        Node(13.5, isotype to iga, abundance to ab())
                        Node(23.5, isotype to iga, abundance to ab())
                        Node(34.5, isotype to igm, abundance to ab()) {
                            Node(13.5, isotype to iga, abundance to ab())
                            Node(1.5, isotype to igg, abundance to ab())
                        }
                    }
                    Node(3.5, isotype to iga, abundance to 3.0)
                }
                Node(1.0, isotype to iga, abundance to 3.0) {
                    Node(4.0, isotype to igm, abundance to 3.0)
                    Node(4.0, isotype to igm, abundance to 3.0) {
                        Node(24.0, isotype to igm, abundance to 3.0)
                        Node(4.0, isotype to igm, abundance to 3.0) {
                            Node(4.0, isotype to igg, abundance to 3.0)
                            Node(14.0, isotype to iga, abundance to 3.0) {
                                Node(3.0, isotype to igg, abundance to 3.0)
                                Node(3.0, isotype to igm, abundance to 3.0)
                                Node(3.0, isotype to igm, abundance to 3.0)
                            }
                        }
                    }

                    Node(1.0, isotype to igm, abundance to 3.0) {
                        Node(4.0, isotype to igm, abundance to 3.0)
                        Node(4.0, isotype to igm, abundance to 3.0) {
                            Node(14.0, isotype to iga, abundance to ab())
                            Node(41.0, isotype to igg, abundance to ab())
                            Node(14.0, isotype to iga, abundance to ab())
                            Node(41.0, isotype to igg, abundance to ab())
                            Node(41.0, isotype to igg, abundance to ab()) {
                                Node(14.0, isotype to igg, abundance to ab())
                                Node(41.0, isotype to igm, abundance to ab())
                                Node(14.0, isotype to iga, abundance to ab()) {
                                    Node(3.0, isotype to igg, abundance to ab())
                                    Node(3.0, isotype to igm, abundance to ab())
                                    Node(3.0, isotype to igm, abundance to ab())
                                }
                            }
                            Node(14.0, isotype to iga, abundance to 3.0) {
                                Node(3.0, isotype to igg, abundance to ab())
                                Node(3.0, isotype to igm, abundance to ab())
                                Node(3.0, isotype to igm, abundance to ab())
                            }
                        }
                    }
                }
            }

        val plots =
            GGDendroPlot(
                tree,
                rpos = Left,
                lineColor = "#aaaaaa",
                nodeAlpha = 0.5
            ) {
                color = isotype
                size = abundance
            }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testLabels() {
        val igg = "igg"
        val igm = "igm"
        val iga = "iga"
        val isotype = "isotype"
        val label = "label"

        val tree =
            Node(0.0, isotype to igg) {
                Node(1.0, isotype to igg) {
                    Node(1.5, isotype to igm, label to "A")
                    Node(3.5, isotype to iga, label to "B")
                }
                Node(1.0, isotype to iga) {
                    Node(4.0, isotype to igm, label to "C")
                    Node(4.0, isotype to igm, label to "asdfas")
                }
            }

        val plots = Position.values().flatMap { pos ->
            listOf(0.0, 45, 90.0).map { angle ->
                GGDendroPlot(
                    tree,
                    rpos = pos,
                    height = 1.0,
                    lineWidth = 0.05,
                    nodeSize = 2.0,

                    lineColor = "#aaaaaa"
                ) {
                    color = isotype
                }.withLabels(label, labelAngle = angle)
            }
        }


        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testLabelBorder() {
        val igg = "igg"
        val igm = "igm"
        val iga = "iga"
        val isotype = "isotype"
        val label = "label"

        val tree =
            Node(0.0, isotype to igg) {
                Node(1.0, isotype to igg) {
                    Node(1.5, isotype to igm, label to "A")
                    Node(3.5, isotype to iga, label to "B")
                }
                Node(1.0, isotype to iga) {
                    Node(4.0, isotype to igm, label to "C")
                    Node(4.0, isotype to igm, label to "D")
                }
            }

        val plots = Position.values().flatMap { pos ->
            listOf(0.0, 45, 90.0).map { angle ->
                GGDendroPlot(
                    tree,
                    rpos = pos,
                    height = 1.0,
                    lineWidth = 0.05,
                    nodeSize = 2.0,
                    lineColor = "#aaaaaa"
                ) {
                    color = isotype
                }.withLabels(label, labelAngle = angle) + themeClassic()
            }
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testTextLayer() {
        val igg = "igg"
        val igm = "igm"
        val iga = "iga"
        val isotype = "isotype"
        val label = "label"
        val text = "text"
        val rnd = kotlin.random.Random(System.currentTimeMillis())
        fun text() = text to (0..rnd.nextInt(1, 10)).map { ('a'..'z').toList()[it] }.joinToString("")

        val tree =
            Node(0.0, isotype to igg, text()) {
                Node(1.0, isotype to igg, text()) {
                    Node(1.5, isotype to igm, label to "A", text())
                    Node(3.5, isotype to iga, label to "B", text())
                }
                Node(1.0, isotype to iga, text()) {
                    Node(4.0, isotype to igm, label to "C", text())
                    Node(4.0, isotype to igm, label to "D", text())
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(
                tree,
                rpos = pos,
                height = 1.0,
                lineWidth = 0.05,
                nodeSize = 2.0,
                lineColor = "#aaaaaa"
            ) {
                color = isotype
            }
                .withLabels(label)
                .withTextLayer(text) +
                    themeClassic()

        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    object TestNodeUtil {
        val isotype = "isotype"
        val label = "label"
        val text = "text"
        val rnd = kotlin.random.Random(System.currentTimeMillis())
        val alignment = "alignment"
        private val chars: List<Char> = ('a'..'z').toList() + ('a'..'z').toList().map { it.uppercaseChar() }
        private fun rndChar() = chars[rnd.nextInt(0, chars.size)]
        fun isotype() = isotype to listOf("IGG", "IGM", "IGA", "IGM")[rnd.nextInt(4)]
        fun label() = label to listOf("A", "B", "C", "D")[rnd.nextInt(4)]
        fun text() = text to (0..rnd.nextInt(1, 100)).map { rndChar() }.joinToString("").uppercase()
        fun alignment() = alignment to run {
            val str = "ATCGC-JASD-KHBFNDFJHGAS-BMNCASXCASD--HJLASD".toCharArray()
            repeat(5) {
                str[rnd.nextInt(str.size)] = rndChar()
            }
            String(str)
        }

        fun h() = rnd.nextDouble(1.0, 10.0)

        fun root(builder: ChildrenReceiver<*>.() -> Unit = {}) =
            Node(h(), label(), isotype(), text(), alignment(), builder = builder)

        fun ChildrenReceiver<*>.node(builder: ChildrenReceiver<*>.() -> Unit = {}) =
            this.Node(h(), label(), isotype(), text(), alignment(), builder = builder)
    }

    @Test
    internal fun testTextLayer2() {
        val tree =
            root {
                repeat(5) {
                    node {
                        repeat(5) {
                            node {
                                repeat(5) {
                                    node()
                                }
                            }
                        }
                    }
                }
            }

        val plots = GGDendroPlot(
            tree,
            rpos = Left,
            lineColor = "#aaaaaa"
        ) {
            color = isotype
        }
            .withLabels(label)
            .withTextLayer(text) +
                themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testTextLayer3() {
        val tree =
            root {
                repeat(2) {
                    node {
                        repeat(2) {
                            node {
                                repeat(2) {
                                    node {
                                        repeat(2) {
                                            node()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(
                tree,
                rpos = pos
            ) {
                color = label
            }
                .withLabels(label)
                .withTextLayer(text) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }

    @Test
    internal fun testAlignmentLayer1() {
        val tree =
            root {
                repeat(2) {
                    node {
                        repeat(2) {
                            node {
                                repeat(2) {
                                    node {
                                        repeat(2) {
                                            node()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        val plots = Position.values().map { pos ->
            GGDendroPlot(
                tree,
                rpos = pos
            ) {
                color = label
            }
                .withLabels(label)
                .withAlignmentLayer(alignment) + themeClassic()
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }
}
