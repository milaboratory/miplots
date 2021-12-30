package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.util.RefGroup
import com.milaboratory.statplots.util.TestMethod
import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import jetbrains.datalore.plot.MonolithicCommon
import jetbrains.letsPlot.GGBunch
import jetbrains.letsPlot.facet.facetGrid
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.ggsize
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.ylim
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readTSV
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class BoxPlotTest {
    val mieloma by lazy { DataFrame.readTSV("https://raw.githubusercontent.com/kassambara/data/master/myeloma.txt") }
    val toothGrowth by lazy {
        DataFrame.readCSV(javaClass.getResource("/ToothGrowth.csv")!!)
            .convert { column<Double>("dose") }.to<String>()
    }

    fun mielomaEmpty() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
    ).plot

    fun mielomaRefSign() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        refGroup = RefGroup.all,
    ).plot

    fun mielomaRefPVal() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        refGroup = RefGroup.of("MAF"),
        labelFormat = Formatted("{pValue}")

    ).plot

    fun mielomaAllComps() = BoxPlot(
        mieloma,
        x = "molecular_group",
        y = "IRF4",
        showOverallPValue = true,
        allComparisons = true
    ).plot

    @Test
    internal fun testMieloma() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            mielomaEmpty().toPDF(),
            mielomaRefSign().toPDF(),
            mielomaRefPVal().toPDF(),
            mielomaAllComps().toPDF()
        )
    }

    fun toothEmpty() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
    ).plot

    fun toothRefSign() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        refGroup = RefGroup.all,
    ).plot

    fun toothRefPVal() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        refGroup = RefGroup.of("1.0"),
        labelFormat = Formatted("{pValue}")

    ).plot

    fun toothAllComps() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        showOverallPValue = true,
        allComparisons = true,
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    ).plot

    fun toothGrouped() = BoxPlot(
        toothGrowth,
        x = "dose",
        y = "len",
        group = "supp",
        labelFormat = Formatted("p = {pValue}"),
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    ).plot

    fun toothGroupedFacet() = BoxPlot(
        toothGrowth,
        x = "supp",
        y = "len",
        group = "dose",
        facet = true,
        labelFormat = Formatted("p = {pValue}"),
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis,
        allComparisons = true
    ).plot

    @Test
    internal fun testTooth() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            toothEmpty().toPDF(),
            toothRefSign().toPDF(),
            toothRefPVal().toPDF(),
            toothAllComps().toPDF(),
            toothGrouped().toPDF(),
            toothGroupedFacet().toPDF()
        )
    }

    @Test
    internal fun testGGBunch() {
        val size = 300
        val tg = toothGrowth
        val yax = tg["len"].convertToDouble()
        val ylim = ylim(yax.min() to yax.max())
        val a = BoxPlot(
            toothGrowth.filter { column<String>("dose") eq "0.5" },
            x = "supp",
            y = "len",
            showOverallPValue = true,
        ).plot + ylim + ggsize(size, size)

        val b = BoxPlot(
            toothGrowth.filter { column<String>("dose") eq "1.0" },
            x = "supp",
            y = "len",
            showOverallPValue = true,
        ).plot + ylim + ggsize(size, size)

        val c = BoxPlot(
            toothGrowth.filter { column<String>("dose") eq "2.0" },
            x = "supp",
            y = "len",
            showOverallPValue = true,
        ).plot + ylim + ggsize(size, size)

        val plt = GGBunch()
            .addPlot(a, 0 * size, 0)
            .addPlot(b, 1 * size, 0)
            .addPlot(c, 2 * size, 0)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun test3() {
        val data = toothGrowth.update("len") { -(it as Double) }
        val plt = BoxPlot(
            data,
            x = "dose",
            y = "len",
            group = "supp"
        ).plot

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }


    @Test
    internal fun test4() {
        val data = toothGrowth.update("len") { -(it as Double) }
        var plt = letsPlot(data.toMap()) {
            x = "supp"
            y = "len"
//            group = "supp"
        }
        plt += geomBoxplot {
//            fill= "supp"
        }

//        plt += geomText(
//            mapOf(
//                "dose" to listOf("0.5", "1.0", "2.0"),
//                "ll" to listOf("A", "B", "C"),
//                "supp" to listOf("VC", "VC", "OJ")
//            ),
//            y = -1.0
//        ) {
//            x = "dose"
//            label = "ll"
//        }

        plt += facetGrid("dose")

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }

    @Test
    internal fun rttr() {
        var p = letsPlot()

        val data3 = mapOf(
            "category" to listOf(1, 2, 3, 1, 2, 3, 1, 2, 3),
            "value" to listOf(1, 2, 3, 4, 7, 8, 11, 0, 1),
            "supp" to listOf("A", "A", "A", "A", "B", "B", "B", "B", "B")
        )

        p += geomBoxplot(data3) {
            x = "category"
            y = "value"
            group = "supp"
        }

        val sign = mapOf(
            "category" to listOf(1, 2, 3),
            "z" to listOf("*", "**", "***")
        )

        p += geomText(sign, y = 15) {
            x = "category"
            label = "z"
        }



        println(MonolithicCommon.processRawSpecs(p.toSpec(), false))
//        writePDF(
//            Paths.get("scratch/bp.pdf"),
//            p.toPDF()
//        )
    }
}
