@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData.myeloma
import com.milaboratory.miplots.TestData.toothGrowth
import com.milaboratory.miplots.stat.util.RefGroup
import com.milaboratory.miplots.stat.util.TestMethod
import com.milaboratory.miplots.stat.xdiscrete.LabelFormat.Companion.Formatted
import com.milaboratory.miplots.toPDF
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.ggsize
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class statCompareMeansTest {
    fun myelomaBase() = GGBoxPlot(
        myeloma,
        x = "molecular_group",
        y = "IRF4",
    ) {
        fill = "molecular_group"
    } + statCompareMeans()

    fun myelomaRefSign() = myelomaBase() + statCompareMeans(
        refGroup = RefGroup.all
    )

    fun myelomaRefPVal() = myelomaBase() + statCompareMeans(
        refGroup = RefGroup.of("MAF"),
        labelFormat = Formatted("{pValue}")
    )

    fun myelomaAllComps() = myelomaBase() + statCompareMeans(
        allComparisons = true
    )

    fun myelomaGroupped1() = GGBoxPlot(
        myeloma,
        x = "molecular_group",
        y = "IRF4"
    ) {
        color = "chr1q21_status"
    } + statCompareMeans()

    @Test
    internal fun testMyeloma() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            myelomaBase().toPDF(),
            myelomaRefSign().toPDF(),
            myelomaRefPVal().toPDF(),
            myelomaAllComps().toPDF(),
            myelomaGroupped1().toPDF()
        )
    }

    fun toothEmpty() = GGBoxPlot(
        toothGrowth,
        x = "dose",
        y = "len"
    ) {
        fill = "dose"
    }

    fun toothOverall() = GGBoxPlot(
        toothGrowth,
        x = "dose",
        y = "len"
    ) {
        fill = "dose"
    } + statCompareMeans()

    fun toothRefSign() = toothEmpty() + statCompareMeans(
        refGroup = RefGroup.all
    ) + statCompareMeans()

    fun toothRefPVal() = toothEmpty() + statCompareMeans(
        refGroup = RefGroup.of("1.0"),
        labelFormat = Formatted("{pValue}")
    ) + statCompareMeans()

    fun toothAllComps() = toothEmpty() + statCompareMeans(
        allComparisons = true,
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    ) + statCompareMeans()

    fun toothGrouped() = GGBoxPlot(
        toothGrowth,
        x = "dose",
        y = "len"
    ) {
        fill = "supp"
    } + statCompareMeans(
        labelFormat = Formatted("p = {pValue}"),
        method = TestMethod.KruskalWallis,
        multipleGroupsMethod = TestMethod.KruskalWallis
    )

    @Test
    internal fun testTooth() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            toothEmpty().toPDF(),
            toothOverall().toPDF(),
            toothRefSign().toPDF(),
            toothRefPVal().toPDF(),
            toothAllComps().toPDF(),
            toothGrouped().toPDF()
        )
    }

    @Test
    internal fun testFacetsMyeloma() {
        val plt = GGBoxPlot(
            myeloma,
            x = "molecular_group",
            y = "IRF4",
            facetBy = "chr1q21_status",
            facetNCol = 2
        ) {
            fill = "molecular_group"
        }
        plt += statCompareMeans(allComparisons = true, pAdjustMethod = null)
        plt += statCompareMeans()
        plt += ggsize(2000, 1000)

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun testFacetsTooth() {
        val plt = GGBoxPlot(
            toothGrowth,
            x = "supp",
            y = "len",
            facetBy = "dose",
            facetNCol = 2
        ) {
            fill = "supp"
        }

        plt += statCompareMeans(allComparisons = true, pAdjustMethod = null)
        plt += statCompareMeans()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }


    @Test
    internal fun testFacetsTooth2() {
        val plt = GGBoxPlot(
            toothGrowth,
            x = "supp",
            y = "len",
            facetBy = "dose",
            facetNCol = 2
        ) {
            fill = "supp"
        }

        plt += statCompareMeans(allComparisons = true, pAdjustMethod = null)
        plt += statCompareMeans(refGroup = RefGroup.all, pAdjustMethod = null)
        plt += statCompareMeans()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun testComparisonsLabel() {
        val plt = GGBoxPlot(
            myeloma,
            x = "molecular_group",
            y = "IRF4",
        ) {
            fill = "molecular_group"
        } + statCompareMeans(
            allComparisons = true,
            labelFormat = Formatted()
        )

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }

    @Test
    internal fun testComparisonsFilter() {
        val plt = GGBoxPlot(
            toothGrowth,
            x = "dose",
            y = "len",
        ) + statCompareMeans(
            comparisons = listOf("0.5" to "1.0", "0.5" to "2.0"),
            labelFormat = Formatted()
        )

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF(),
        )
    }
}
