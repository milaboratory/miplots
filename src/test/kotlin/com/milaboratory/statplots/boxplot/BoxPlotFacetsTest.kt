package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import jetbrains.letsPlot.*
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.scale.scaleXContinuous
import jetbrains.letsPlot.scale.xlim
import jetbrains.letsPlot.scale.ylim
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.toMap
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class BoxPlotFacetsTest {
    @Test
    internal fun test1() {
        val plt = BoxPlotFacets2(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
            facet = "chr1q21_status",
            ncol = 2,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun test2() {
        val plt = BoxPlotFacets2(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facet = "dose",
            ncol = 2,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun test3() {
        val L = 500
        var plt = BoxPlot(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
            allComparisons = true
        ).plot

        plt += ggsize(L, L)
//        plt += coord {  }

        val A = 0
        val empty = letsPlot(
            mapOf(
                "x" to listOf<String>(),
                "y" to listOf()
            )
        ) {
            x = "x"
            y = "y"
        } + geomPoint()
        val xAxis = empty + xlim(listOf("VC", "OJ")) + ggsize(A, L)
        val yAxis = empty + ylim(listOf(0.0, 40.0)) + ggsize(L, A)

        val common = theme(panelGridMajorY = elementLine())
        val noYAxis =
            theme(
                axisLineY = elementBlank(),
                axisTextY = elementBlank(),
                axisTicksY = elementBlank(),
                axisTitleY = elementBlank(),
            ) //+ xlab(" ")

        val noXAxis =
            theme(
                axisLineX = elementBlank(),
                axisTextX = elementBlank(),
                axisTicksX = elementBlank(),
                axisTitleX = elementBlank(),
            )// + xlab(" ")

        plt += common + noXAxis + noYAxis
        writePDF(
            Paths.get("scratch/bp.pdf"),
//            xAxis.toPDF()
            GGBunch()
//                .addPlot(yAxis, 0, 0)
                .addPlot(plt, A, 0)
                .addPlot(plt, A, L)
//                .addPlot(xAxis, A, L + A)

                .addPlot(plt, A + L, 0)
                .addPlot(plt, A + L, L)
//                .addPlot(plt, A + L, L)
//                .addPlot(xAxis, A + L, L + A)
                .toPDF(),
        )
    }

    @Test
    internal fun test22() {
        val xcol = "supp"
        val ycol = "len"
        val facet = "dose"
        val xNumeric = "supp__Numeric"
        var data = TestData.toothGrowth
        val mm = mapOf("VC" to 0.0, "OJ" to 1.0)
        data = data.add(xNumeric) { mm[it[xcol]]!! }

//        "","len","supp","dose"
        var plt = letsPlot(data.toMap()) {
            x = xNumeric
            y = ycol
        }

        plt += geomBoxplot {
            fill = xcol
        }

//        plt += geomPath(
//            data = mapOf(
//                "x" to listOf(0.0, 1.0),
//                // "supp" to listOf("VC", "OJ"),
//                "dose" to listOf("0.5", "0.5")
//            ),
//            y = 35.0
//        ) {
//            x = "x"
//        }
//
///        plt += geomText(
////            data = mapOf(
////                xNumeric to listOf(0.5),
////                "dose" to listOf("0.5"),
////                "len" to listOf(45.0),
////                "label" to listOf("XXX")
////            ),
////        ) {
////            //  x = "x"
////            //y = "len"
////            label = "label"
////        }
////
////        plt += geomText(
////            data = mapOf(
////                xNumeric to listOf(0.5),
////                "dose" to listOf("1.0"),
////                "len" to listOf(45.0),
////                "label" to listOf("YYY")
////            ),
////        ) {
////            //  x = "x"
////            //y = "len"
////            label = "label"
////        }

        plt += geomText(
            data = mapOf(
                xNumeric to listOf(0.5),
                ycol to listOf(24.095),
                facet to listOf("0.5"),
                "label" to listOf("0.03")
            ),
        ) {
            x = xNumeric
            y = ycol
            label = "label"
        }

        plt += geomText(
            data = mapOf(
                xNumeric to listOf(0.5),
                ycol to listOf(29.355),
                facet to listOf("1.0"),
                "label" to listOf("0.01")
            ),
        ) {
            x = xNumeric
            y = ycol
            label = "label"
        }

//        plt += geomText(
//            data = mapOf(
//                xNumeric to listOf(0.5),
//                ycol to listOf(24.095),
//                facet to listOf("0.5"),
//                "ll" to listOf("0.03")
//            ),
//            //   size = 7
//        ) {
//            x = xNumeric
//            y = ycol
//            label to "ll"
//        }
//
//
//        plt += geomText(
//            data = mapOf(
//                xNumeric to listOf(0.5),
//                ycol to listOf(29.355),
//                facet to listOf("1.0"),
//                "ll" to listOf("0.01")
//            ),
//            //   size = 7
//        ) {
//            x = xNumeric
//            y = ycol
//            label to "ll"
//        }


        plt += facetWrap(facets = facet)

        plt += scaleXContinuous(breaks = listOf(0.0, 1.0), labels = listOf("VC", "OJ"))

//        plt += geomPoint()

//        val xx = listOf("0.5", "AS", "1.0", "SA", "2.0")
//        plt += scaleXDiscrete( labels = xx)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun testx2() {
        val plt = BoxPlotFacets2(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facet = "dose",
            ncol = 2,
            allComparisons = true
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }
}
