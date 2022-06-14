@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.util.*
import com.milaboratory.miplots.stat.xdiscrete.LabelFormat.Companion.Formatted
import com.milaboratory.miplots.stat.xdiscrete.LabelFormat.Companion.Significance
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs
import kotlin.math.max

/**
 * Format p-value label for `statCompareMeans`
 */
sealed interface LabelFormat {
    companion object {
        private object significance : LabelFormat

        /**
         * Show significance level only
         */
        val Significance: LabelFormat = significance

        /**
         * Format p-value with specified format.
         *
         * @param fmt format string, example: "{Method}, p = {pValue}"
         */
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
    val labelFormat: LabelFormat?

    /** Positions of p-value labels */
    val labelPos: List<Double>?

    /** Fit positions to max value */
    val labelPosFit: Boolean?

    /** Text & lines color */
    val color: String?

    /** Text & lines color */
    val textSize: Double?

    /** Text size unit */
    val sizeUnit: String?
}

private class StatCompareMeansFeature(
    val plt: GGXDiscrete,
    val statData: StatCompareMeansData,
    val ops: StatCompareMeansOptions
) {
    val textSize = run {
        val s = ops.textSize ?: (1.5 * plt.yDelta)
        if (s.isNaN())
            0.0
        else
            s
    }

    private val mode: BoxPlotMode = run {
        if (plt.groupBy != null && plt.groupBy != plt.x) {
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
    private fun overallPValueLayer(compareMeans: CompareMeans, facet: Any?, yCoord: Double): Feature {
        val lf = ops.labelFormat ?: Formatted()
        val label = when (lf) {
            Significance -> compareMeans.overallPValueSign
            is Formatted -> lf.format(compareMeans.overallPValueMethod, compareMeans.overallPValueFmt)
            else -> throw RuntimeException()
        }
        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to listOf(0.0),
            plt.y to listOf(yCoord),
            "label" to listOf(label)
        )
        if (facet != null) {
            data += plt.facetBy!! to listOf(facet)
        }
        return geomText(
            data = data,
            size = textSize,
            color = ops.color,
            sizeUnit = ops.sizeUnit,
            hjust = 1.0
        ) {
            this.label = "label"
        }
    }

    private fun overallPValueLayer(): Feature {
        val yCoord = plt.adjustAndGetYMax()
        return if (plt.facetBy != null) {
            facetLayer(statData.compareMeansFacets) { c, f -> overallPValueLayer(c, f, yCoord) }
        } else
            overallPValueLayer(statData.compareMeans, null, yCoord)
    }

    /** Add group p-values to the plot */
    private fun pValuesLayer(compareMeans: CompareMeans, facet: Any?, yCoord: Double): Feature = run {
        val stat: DataFrame<CompareMeansRow> = compareMeans.stat

        val lf = ops.labelFormat ?: Significance
        val labels = when (lf) {
            Significance -> stat.pSignif.map { if (ops.hideNS && it == SignificanceLevel.NS) "" else it.string }
                .toList()
            is Formatted -> stat.pValueFmt.map {
                lf.format(ops.method, it)
            }.toList()
            else -> throw RuntimeException()
        }

        val yVals: List<Any> =
            if (ops.labelPos != null) {
                ops.labelPos!!
            } else if (ops.labelPosFit == true) {
                if (facet == null)
                    stat.group2.toList()
                        .map { group ->
                            plt.data.filter { plt.x<Any>() == group }[plt.y]
                                .convertToDouble()
                                .max() * 1.0
                        }
                else {
                    stat.group2.toList()
                        .map { group ->
                            plt.data.filter { plt.x<Any>() == group && plt.facetBy!!<Any>() == facet }[plt.y]
                                .convertToDouble()
                                .max() * 1.0
                        }
                }
            } else {
                List(labels.size) { yCoord }
            }

        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to stat.group2.toList().map { plt.xnum[it]!! },
            plt.y to yVals,
            "__label" to labels
        )
        if (facet != null)
            data += plt.facetBy!! to List(labels.size) { facet }

        geomText(
            data,
            size = textSize,
            color = ops.color,
            sizeUnit = ops.sizeUnit
        ) { label = "__label" }
    }

    private fun pValuesLayer(): Feature {
        val yCoord = plt.adjustAndGetYMax()
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacets) { c, f -> pValuesLayer(c, f, yCoord) }
        else
            pValuesLayer(statData.compareMeans, null, yCoord)
    }

    /** Add comparisons */
    private fun comparisonsLayer(
        compareMeans: CompareMeans,
        facet: Any?,
        yCoordBase: Double
    ): Pair<Feature, Double> = run {
        val stat = if (ops.allComparisons == true)
            compareMeans.stat
        else {
            val set = ops.comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1 to group2) in set || (group2 to group1) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = plt.yDelta * 0.2
        val rows = stat.rows().sortedBy { abs(plt.xord[it.group1!!]!! - plt.xord[it.group2]!!) }
        if (rows.isEmpty())
            FeatureList(emptyList()) to yCoordBase
        else {
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

            var yValue = yCoordBase
            val lf = ops.labelFormat ?: Formatted("{pValue}")
            for ((group, row) in rows.withIndex()) {
                val gr1 = plt.xnum[row.group1!!]!!
                val gr2 = plt.xnum[row.group2]!!

                pathData[plt.xNumeric]!!.addAll(listOf(gr1, gr1, gr2, gr2))
                pathData[plt.y]!!.addAll(listOf(yValue - mustach, yValue, yValue, yValue - mustach))
                pathData["__group"]!!.addAll(List(4) { group })
                if (facet != null)
                    pathData[plt.facetBy!!]!!.addAll(List(4) { facet })

                textData[plt.xNumeric]!!.addAll(listOf((gr1 + gr2) / 2.0))
                textData[plt.y]!!.addAll(listOf(yValue + plt.yDelta / 2))
                val label = when (lf) {
                    Significance -> row.pSignif
                    is Formatted -> lf.format(compareMeans.method, row.pValueFmt)
                    else -> throw RuntimeException()
                }
                textData["__label"]!!.addAll(listOf(label))
                if (facet != null)
                    textData[plt.facetBy]!!.addAll(listOf(facet))

                yValue += plt.yDelta
            }

            geomPath(
                pathData,
                color = ops.color
            ) {
                this.group = "__group"
            } + geomText(
                textData,
                size = textSize,
                color = ops.color,
                sizeUnit = ops.sizeUnit
            ) {
                label = "__label"
            } to yValue
        }
    }

    private fun comparisonsLayer(): Feature = run {
        val yCoordBase = plt.yMax
        val r = if (plt.facetBy != null)
            facetLayerP(statData.compareMeansFacets) { c, f -> comparisonsLayer(c, f, yCoordBase) }
        else
            comparisonsLayer(statData.compareMeans, null, yCoordBase)
        plt.yMax = r.second
        r.first
    }

    /** Add p-value for each group in grouped boxplot (no facets) */
    private fun pValuesGroupByLayer(stat: Map<Any, CompareMeans>, facet: Any?, yCoord: Double): Feature = run {
        val lf = ops.labelFormat ?: Significance
        val labels = stat.values.map {
            when (lf) {
                Significance -> it.overallPValueSign.string
                is Formatted -> lf.format(ops.method, it.overallPValueFmt)
                else -> throw RuntimeException()
            }
        }

        val yVals: List<Any> =
            if (ops.labelPos != null) {
                ops.labelPos!!
            } else if (ops.labelPosFit == true) {
                if (facet == null)
                    stat.keys.toList()
                        .map { group ->
                            plt.data.filter { plt.x<Any>() == group }[plt.y]
                                .convertToDouble()
                                .max() * 1.0
                        }
                else {
                    stat.keys.toList()
                        .map { group ->
                            plt.data.filter { plt.x<Any>() == group && plt.facetBy!!<Any>() == facet }[plt.y]
                                .convertToDouble()
                                .max() * 1.0
                        }
                }
            } else {
                List(labels.size) { yCoord }
            }

        val data = mutableMapOf<String, List<Any>>(
            plt.xNumeric to stat.keys.toList().map { plt.xnum[it]!! },
            "labels" to labels,
            plt.y to yVals
        )
        if (facet != null)
            data += plt.facetBy!! to listOf(facet)

        geomText(
            data,
            size = textSize,
            color = ops.color,
            sizeUnit = ops.sizeUnit
        ) { label = "labels" }
    }

    private fun pValuesGroupByLayer(): Feature {
        val yCoord = plt.adjustAndGetYMax()
        return if (plt.facetBy != null)
            facetLayer(statData.compareMeansFacetsGroupBy) { c, f -> pValuesGroupByLayer(c, f, yCoord) }
        else
            pValuesGroupByLayer(statData.compareMeansGroupBy, null, yCoord)
    }

    fun getFeature() = when (mode) {
        BoxPlotMode.OverallPValue -> overallPValueLayer()
        BoxPlotMode.WithReference -> pValuesLayer()
        BoxPlotMode.WithComparisons -> comparisonsLayer()
        BoxPlotMode.WithGroupBy -> pValuesGroupByLayer()
    }

    companion object {
        internal operator fun Pair<Feature, Double>.plus(oth: Pair<Feature, Double>) = run {
            (this.first + oth.first) to max(this.second, oth.second)
        }

        internal fun <T> facetLayerP(
            facets: Map<Any?, T>,
            layer: (T, Any?) -> Pair<Feature, Double>
        ): Pair<Feature, Double> = run {
            var feature: Pair<Feature, Double>? = null
            for ((facet, compareMeans) in facets) {
                if (feature == null)
                    feature = layer(compareMeans, facet)
                else
                    feature += layer(compareMeans, facet)
            }
            feature!!
        }

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
    val plt: GGXDiscrete,
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
                x = plt.groupBy!!,
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
                            x = plt.groupBy!!,
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

/**
 * Add mean comparison p-values to a ggplot, such as box blots, dot plots and stripcharts.
 *
 * @param comparisons Pairs to show
 * @param allComparisons Show all pairs
 * @param hideNS Hide non significant p-value
 * @param labelFormat Format of p-value labels
 * @param labelPos Positions of p-value labels
 * @param labelPosFit Fit positions to max value
 * @param method The type of test. Default is Wilcox
 * @param paired A logical indicating whether a paired test should be performed. Used only in T-test and in Wilcox
 * @param multipleGroupsMethod The type of test for multiple groups. Default is Kruskal-Wallis
 * @param pAdjustMethod Method for adjusting p values (null for no adjustment). Default is Bonferroni.
 * @param refGroup The reference group. If specified, for a given grouping variable, each of the
 *                  group levels will be compared to the reference group (i.e. control group).
 *                  refGroup can be also “all”. In this case, each of the grouping variable levels
 *                  is compared to all (i.e. base-mean).
 * @see TestMethod
 * @see PValueCorrection.Method
 */
@Suppress("ClassName")
data class statCompareMeans(
    override val comparisons: List<Pair<String, String>>? = null,
    override val allComparisons: Boolean? = null,
    override val hideNS: Boolean = false,
    override val labelFormat: LabelFormat? = null,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val paired: Boolean = false,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = PValueCorrection.Method.Bonferroni,
    override val refGroup: RefGroup? = null,
    override val labelPos: List<Double>? = null,
    override val labelPosFit: Boolean? = null,
    override val color: String? = "#000000",
    override val textSize: Double? = null,
    override val sizeUnit: String? = "y"
) : GGXDiscreteFeature, StatCompareMeansOptions {

    override val prepend = false

    override fun getFeature(base: GGXDiscrete): Feature = run {
        val cmpOps = CompareMeansOptionsCapsule(this)
        return (base.cache.computeIfAbsent(cmpOps) {
            StatCompareMeansData(base, cmpOps)
        } as StatCompareMeansData)
            .getFeature(this)
    }
}
