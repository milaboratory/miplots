@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.*
import com.milaboratory.statplots.xdiscrete.LabelFormat.Companion.Formatted
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs

sealed interface LabelFormat {
    companion object {
        private object significance : LabelFormat

        val Significance: LabelFormat = significance

        data class Formatted(val fmt: String = "{Method}, p = {pValue}") : LabelFormat {
            fun format(method: TestMethod, pValue: String) =
                fmt.replace("{Method}", method.str)
                    .replace("{pValue}", pValue)
        }
    }
}

internal
enum class BoxPlotMode {
    OverallPValue,
    WithComparisons,
    WithReference,
    WithGroupBy,
}

interface StatCompareMeansOptions : CompareMeansOptions {
    /** Pairs to show */
    val comparisons: List<Pair<String, String>>?

    /** Show all pairs */
    val allComparisons: Boolean?

    /** Hide non significant p-value*/
    val hideNS: Boolean

    /** Format of p-value labels */
    val labelFormat: LabelFormat
}

private class StatCompareMeansFeature(
    val plt: ggBase,
    val statData: StatCompareMeansData,
    val ops: StatCompareMeansOptions
) {
    private val mode: BoxPlotMode = run {
        if (plt.aes.fill != null && plt.aes.fill != plt.x) {
            return@run BoxPlotMode.WithGroupBy
        }

        if (ops.comparisons != null || ops.allComparisons == true) {
            return@run BoxPlotMode.WithComparisons
        }

        if (ops.refGroup != null) {
            return@run BoxPlotMode.WithReference
        }

        return@run BoxPlotMode.OverallPValue
    }

    /** Add overall p-value to the plot */
    private fun overallPValueLayer(compareMeans: CompareMeans, facet: Any?): Feature {
        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to listOf(0.0),
            plt.y to listOf(plt.adjustAndGetYMax()),
            "label" to listOf("${compareMeans.overallPValueMethod}, p = ${compareMeans.overallPValueFmt}")
        )
        if (facet != null) {
            data += plt.facetBy!! to listOf(facet)
        }
        return geomText(
            data = data,
            size = 7
        ) {
            label = "label"
        }
    }

    private fun overallPValueLayer(): Feature {
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacets, this::overallPValueLayer)
        else
            overallPValueLayer(statData.compareMeans, null)
    }

    /** Add group p-values to the plot */
    private fun pValuesLayer(compareMeans: CompareMeans, facet: Any?): Feature = run {
        val stat: DataFrame<CompareMeansRow> = compareMeans.stat

        val labels = when (ops.labelFormat) {
            LabelFormat.Significance -> stat.pSignif.map { if (ops.hideNS && it == SignificanceLevel.NS) "" else it.string }
                .toList()
            is Formatted -> stat.pValueFmt.map {
                (ops.labelFormat as Formatted).format(ops.method, it)
            }.toList()
            else -> throw RuntimeException()
        }

        val y = plt.adjustAndGetYMax()
        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to stat.group2.toList().map { plt.xnum[it]!! },
            plt.y to List(labels.size) { y },
            "__label" to labels
        )
        if (facet != null)
            data += plt.facetBy!! to List(labels.size) { facet }

        geomText(data) { label = "__label" }
    }

    private fun pValuesLayer(): Feature {
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacets, this::pValuesLayer)
        else
            pValuesLayer(statData.compareMeans, null)
    }

    /** Add comparisons */
    private fun comparisonsLayer(
        compareMeans: CompareMeans,
        facet: Any?
    ): Feature = run {
        val stat = if (ops.allComparisons == true)
            compareMeans.stat
        else {
            val set = ops.comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1 to group2) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = plt.yDelta * 0.2
        val rows = stat.rows().sortedBy { abs(plt.xord[it.group1!!]!! - plt.xord[it.group2]!!) }

        val pathData = mutableMapOf<String, MutableList<Any?>>(
            plt.xNumeric to mutableListOf(),
            plt.y to mutableListOf(),
            "__group" to mutableListOf()
        )
        if (facet != null)
            pathData += plt.facetBy!! to mutableListOf()

        val textData: MutableMap<String, MutableList<Any?>> = mutableMapOf(
            plt.xNumeric to mutableListOf(),
            plt.y to mutableListOf(),
            "__label" to mutableListOf()
        )
        if (facet != null)
            textData += plt.facetBy!! to mutableListOf()


        var group = 0
        for (row in rows) {
            val yValue = plt.adjustAndGetYMax()
            val gr1 = plt.xnum[row.group1!!]!!
            val gr2 = plt.xnum[row.group2]!!

            pathData[plt.xNumeric]!!.addAll(listOf(gr1, gr1, gr2, gr2))
            pathData[plt.y]!!.addAll(listOf(yValue - mustach, yValue, yValue, yValue - mustach))
            pathData["__group"]!!.addAll(List(4) { group })
            ++group
            if (facet != null)
                pathData[plt.facetBy!!]!!.addAll(List(4) { facet })

            textData[plt.xNumeric]!!.addAll(listOf((gr1 + gr2) / 2.0))
            textData[plt.y]!!.addAll(listOf(yValue + plt.yDelta / 2))
            textData["__label"]!!.addAll(listOf(row.pValueFmt))
            if (facet != null)
                textData[plt.facetBy]!!.addAll(listOf(facet))
        }

        geomPath(pathData, color = "black") {
            this.group = "__group"
        } + geomText(textData, size = 6) {
            label = "__label"
        }
    }

    private fun comparisonsLayer(): Feature {
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacets, this::comparisonsLayer)
        else
            comparisonsLayer(statData.compareMeans, null)
    }

    /** Add p-value for each group in grouped boxplot (no facets) */
    private fun pValuesGroupByLayer(stat: Map<Any, CompareMeans>, facet: Any?): Feature = run {
        val labels = stat.values.map {
            when (ops.labelFormat) {
                LabelFormat.Significance -> it.overallPValueSign.string
                is Formatted -> (ops.labelFormat as Formatted).format(ops.method, it.overallPValueFmt)
                else -> throw RuntimeException()
            }
        }

        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to stat.keys.toList().map { plt.xnum[it]!! },
            "labels" to labels,
            plt.y to listOf(plt.adjustAndGetYMax())
        )
        if (facet != null)
            data += plt.facetBy!! to listOf(facet)

        geomText(data) { label = "labels" }
    }

    private fun pValuesGroupByLayer(): Feature {
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacetsGroupBy, this::pValuesGroupByLayer)
        else
            pValuesGroupByLayer(statData.compareMeansGroupBy, null)
    }

    fun getFeature() = when (mode) {
        BoxPlotMode.OverallPValue -> overallPValueLayer()
        BoxPlotMode.WithReference -> pValuesLayer()
        BoxPlotMode.WithComparisons -> comparisonsLayer()
        BoxPlotMode.WithGroupBy -> pValuesGroupByLayer()
    }

    companion object {
        internal fun <T> facetLayer(
            facets: Map<Any?, T>,
            layer: (T, Any?) -> Feature
        ): Feature = run {
            var feature: Feature? = null
            for ((facet, compareMeans) in facets) {
                if (feature == null)
                    feature = layer(compareMeans, facet)
                else
                    feature += layer(compareMeans, facet)
            }
            feature!!
        }
    }
}

private class StatCompareMeansData(
    val plt: ggBase,
    val cmpOps: CompareMeansOptions,
) {
    /** Compare means stat */
    val compareMeans by lazy {
        CompareMeans(
            data = plt.data,
            x = plt.x,
            y = plt.y,
            method = cmpOps.method,
            multipleGroupsMethod = cmpOps.multipleGroupsMethod,
            pAdjustMethod = cmpOps.pAdjustMethod,
            refGroup = cmpOps.refGroup
        )
    }

    /** Compare means stat for grouped data: primaryGroup -> stat */
    val compareMeansGroupBy: Map<Any, CompareMeans> by lazy {
        plt.data.groupBy(plt.x).groups.toList().associate {
            it.first()[plt.x]!! to CompareMeans(
                data = it,
                x = plt.aes.fill!!,
                y = plt.y,
                method = cmpOps.method,
                multipleGroupsMethod = cmpOps.multipleGroupsMethod,
                pAdjustMethod = cmpOps.pAdjustMethod,
                refGroup = cmpOps.refGroup
            )
        }
    }

    /** Compare means stat by facets */
    val compareMeansFacets by lazy {
        plt.data.groupBy(plt.facetBy!!).groups.toList().associate {
            it.first()[plt.facetBy] to CompareMeans(
                data = it,
                x = plt.x,
                y = plt.y,
                method = cmpOps.method,
                multipleGroupsMethod = cmpOps.multipleGroupsMethod,
                pAdjustMethod = cmpOps.pAdjustMethod,
                refGroup = cmpOps.refGroup
            )
        }
    }

    /** Compare means stat for grouped data with facets */
    val compareMeansFacetsGroupBy by lazy {
        plt.data.groupBy(plt.facetBy!!).groups.toList().associate { facet ->
            facet.first()[plt.facetBy] to
                    facet.groupBy(plt.x).groups.toList().associate { group ->
                        group.first()[plt.x]!! to CompareMeans(
                            data = group,
                            x = plt.aes.fill!!,
                            y = plt.y,
                            method = cmpOps.method,
                            multipleGroupsMethod = cmpOps.multipleGroupsMethod,
                            pAdjustMethod = cmpOps.pAdjustMethod,
                            refGroup = cmpOps.refGroup
                        )
                    }
        }
    }

    fun getFeature(ops: StatCompareMeansOptions) = StatCompareMeansFeature(plt, this, ops).getFeature()
}

/** */
@Suppress("ClassName")
data class statCompareMeans(
    override val comparisons: List<Pair<String, String>>? = null,
    override val allComparisons: Boolean? = null,
    override val hideNS: Boolean = false,
    override val labelFormat: LabelFormat = LabelFormat.Significance,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val paired: Boolean = false,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = PValueCorrection.Method.Bonferroni,
    override val refGroup: RefGroup? = null
) : WithFeature, StatCompareMeansOptions {
    override fun getFeature(base: ggBase): Feature = run {
        val cmpOps = CompareMeansOptionsCapsule(this)
        return (base.cache.computeIfAbsent(cmpOps) {
            StatCompareMeansData(base, cmpOps)
        } as StatCompareMeansData)
            .getFeature(this)
    }
}
