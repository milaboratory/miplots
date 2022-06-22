@file:Suppress("ClassName")

package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.dendro.ConnectionType.Rectangle
import com.milaboratory.miplots.dendro.ConnectionType.Triangle
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.Figure
import jetbrains.letsPlot.themeClassic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.*
import kotlin.math.abs

/**
 *
 */
internal class ggDendroTest {
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
        val plot = ggDendro(
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
        val plot = ggDendro(
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
            listOf<Figure>(
                ggDendro(tree, ctype = ct, rpos = Top),
                ggDendro(tree, ctype = ct, rpos = Right),
                ggDendro(tree, ctype = ct, rpos = Bottom),
                ggDendro(tree, ctype = ct, rpos = Left)
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
        val plt = ggDendro(tree, coord = coord) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testImposeX2() {
        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = ggDendro(tree, coord = coord, rpos = Left) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testHeight() {
        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = ggDendro(tree, coord = coord, height = 2.0, rpos = Left) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testBalanced() {
        val plots = ConnectionType.values().flatMap { ct ->
            listOf<Figure>(
                ggDendro(tree, ctype = ct, rpos = Top, balanced = true),
                ggDendro(tree, ctype = ct, rpos = Right, balanced = true),
                ggDendro(tree, ctype = ct, rpos = Bottom, balanced = true),
                ggDendro(tree, ctype = ct, rpos = Left, balanced = true)
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
        val plt = ggDendro(tree1, coord = coord, height = 2.0, rpos = Top, ctype = Rectangle) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }

    @Test
    internal fun testRect() {
        val plt1 = ggDendro(
            tree,
            ctype = Triangle,
            rpos = Right,
            size = 0.10,
            color = "red",
            linewidth = 0.05,
        )

        val plt2 = ggDendro(
            tree,
            ctype = Triangle,
            rpos = Top,
            size = 0.10,
            color = "red",
            linewidth = 0.05,
            balanced = true
        )

        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt3 = ggDendro(
            tree,
            ctype = Triangle,
            coord = coord,
            size = 0.0,
            color = "red",
            linewidth = 5.0,
        ) + themeClassic()

        val plt4 = ggDendro(
            tree,
            ctype = Triangle,
            coord = coord,
            size = 0.0,
            color = "red",
            linewidth = 5.0,
            balanced = true
        ) + themeClassic()

        val plt5 = ggDendro(
            tree,
            rpos = Left,
            ctype = Triangle,
            coord = coord,
            size = 0.0,
            color = "red",
            linewidth = 5.0,
            balanced = true
        ) + themeClassic()

        val plt6 = ggDendro(
            tree,
            ctype = Rectangle,
            rpos = Top,
            size = 0.10,
            color = "red",
            linewidth = 0.05,
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
            ggDendro(tree, ctype = Rectangle, coord = coord, rpos = pos, linewidth = 10.0) + themeClassic()
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
            ggDendro(tree, ctype = Rectangle, rshift = tree.totalHeight, rpos = pos) + themeClassic()
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
            ggDendro(tree, ctype = Rectangle, rshift = tree.totalHeight, height = 1.0, rpos = pos) + themeClassic()
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
            ggDendro(tree, ctype = Rectangle, height = 1.0, rpos = pos) + themeClassic()
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
            ggDendro(tree, ctype = Rectangle, height = 1.0, rpos = pos) + themeClassic()
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
            ggDendro(tree, height = 1.0, linewidth = 0.05, size = 0.2, linecolor = "#aaaaaa") {
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

        val rnd = Random()
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
            ggDendro(
                tree,
                rpos = Left,
                linecolor = "#aaaaaa",
                alpha = 0.5
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
                ggDendro(
                    tree,
                    rpos = pos,
                    height = 1.0,
                    linewidth = 0.05,
                    size = 2.0,
                    angle = angle,
                    linecolor = "#aaaaaa"
                ) {
                    color = isotype
                    this.label = label
                }
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
                ggDendro(
                    tree,
                    rpos = pos,
                    height = 1.0,
                    linewidth = 0.05,
                    size = 2.0,
                    angle = angle,
                    linecolor = "#aaaaaa"
                ) {
                    color = isotype
                    this.label = label
                } + themeClassic()
            }
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plots
        )
    }
}
