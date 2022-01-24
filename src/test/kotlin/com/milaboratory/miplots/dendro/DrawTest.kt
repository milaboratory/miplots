package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.dendro.ConnectionType.Rectangle
import com.milaboratory.miplots.dendro.ConnectionType.Triangle
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.Figure
import jetbrains.letsPlot.themeClassic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class DrawTest {
    val tree =
        Node(4.0, "age" to "12", "sex" to "m") {
            Node(1.5, "age" to "10", "sex" to "m") {
                Node(1.5, "age" to "10", "sex" to "m")
                Node(1.5, "age" to "10", "sex" to "m")
            }
            Node(2.1, "age" to "11", "sex" to "f") {
                Node(1.5, "age" to "10", "sex" to "m")
                Node(1.5, "age" to "10", "sex" to "m")
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
        val tree1 = Node(1.0) {
            Node(0.0)
            Node(0.0)
            Node(0.0)
        }

        val coord = listOf(12.0, 50.0, 79.0, 211.0)
        val plt = ggDendro(tree1, coord = coord, height = 2.0, rpos = Top, ctype = Rectangle) + themeClassic()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
