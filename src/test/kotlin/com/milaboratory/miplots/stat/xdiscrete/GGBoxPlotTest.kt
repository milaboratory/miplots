package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.positionJitterDodge
import jetbrains.letsPlot.scale.ylim
import org.junit.jupiter.api.Test
import java.nio.file.Paths


internal class GGBoxPlotTest {
    @Test
    internal fun examples() {
        val plt1 = GGBoxPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len"
        ) {
            fill = "dose"
        } + ggStrip(position = positionJitterDodge(jitterWidth = 0.2))

        val plt2 = GGBoxPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
        ) {
            fill = "supp"
        } + statCompareMeans()

        val plt7 = GGBoxPlot(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facetBy = "dose",
            facetNRow = 1,
        ) {
            color = "supp"
        }

        val plt8 = GGBoxPlot(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
        ) {
            color = "molecular_group"
        }

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
            plt7,
            plt8
        )
    }


    @Test
    internal fun testFacets() {

        val data = mapOf(
            "x" to listOf("A", "B", "A", "B", "A", "B", "A", "B", "A", "B", "A", "B"),
            "f" to listOf("C", "D", "D", "C", "C", "D", "D", "C", "C", "D", "D", "C"),
            "y" to listOf(101, 102, 103, 104, 105, 106, 111, 112, 113, 114, 115, 116),
        )

        var plt = ggplot(data) { x = "x"; y = "y" }
        plt += geomBoxplot()
        plt += facetWrap(facets = "f", nrow = 1)

        val txt = mapOf(
            "x" to listOf("A"), //"B", "A", "B"),
            "f" to listOf("C"), //"D", "D", "C"),
            "y" to listOf(120), //120, 120, 120),
            "l" to listOf("AC"),//"BD", "AD", "BC")
        )

        plt += geomText(
            data = txt,
            color = "black",
            size = 2.0,
            sizeUnit = "y",
            hjust = 1.0,
        ) {
            label = "l"
            x = "x"
            y = "y"
        }

        plt += ylim(100 to 130)



        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}