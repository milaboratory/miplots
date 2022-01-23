package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.dendro.RootPosition.*
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.letsPlot
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
        var plot = letsPlot()

        plot += geomDendro(
            tree,
            ctype = ConnectionType.Triangle,
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
        val top = letsPlot() + geomDendro(tree, rpos = Top)
        val rht = letsPlot() + geomDendro(tree, rpos = Right)
        val btm = letsPlot() + geomDendro(tree, rpos = Bottom)
        val lft = letsPlot() + geomDendro(tree, rpos = Left)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            top,
            rht,
            btm,
            lft
        )
    }
}
