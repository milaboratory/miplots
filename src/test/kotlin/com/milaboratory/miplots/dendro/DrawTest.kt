package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.dendro.ConnectionType.Triangle
import com.milaboratory.miplots.dendro.RootPosition.*
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.Figure
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class DrawTest {
    val tree = Node(0.0, "age" to "12", "sex" to "m") {
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
}
