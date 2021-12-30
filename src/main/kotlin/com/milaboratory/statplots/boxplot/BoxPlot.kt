package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.BoxPlotMode.*
import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Significance
import com.milaboratory.statplots.util.*
import jetbrains.letsPlot.asDiscrete
import jetbrains.letsPlot.elementLine
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs
import kotlin.reflect.KProperty0

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
    /// no grouping
    Empty,
    WithComparisons,
    WithReference,

    /// with grouping
    WithGroupBy,
    FacetBy,
}

/**
 *
 */
class BoxPlot(
    override val data: AnyFrame,
    override val x: String,
    override val y: String,
    val group: String? = null,
    val facet: Boolean = false,

    /** Show overall p-value */
    val showOverallPValue: Boolean? = null,
    /** Pairs to show */
    val comparisons: List<Pair<String, String>>? = null,
    val allComparisons: Boolean? = null,

    /** Reference group */
    override val refGroup: RefGroup? = null,
    /** Hide non significant p-value*/
    val hideNS: Boolean = false,

    /** Format of p-value labels */
    val labelFormat: LabelFormat = Significance,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = null,
) : CompareMeansParameters {

    /** Let's plot data */
    private val innerData: AnyFrame

    /** ordered x values */
    private val xValuesDistinct: List<Any?>

    /** double x values */
    private val xValuesNumeric: List<Double>

    /** xValue to double */
    private val x2num: Map<Any?, Double>

    init {
        xValuesDistinct = data[x].distinct().toList().flatMap { listOf(it, "") }.dropLast(1)
        xValuesNumeric = xValuesDistinct.indices.map { it.toDouble() / 2.0 }.toList()
        x2num = mutableMapOf()
        for (i in xValuesNumeric.indices)
            x2num += xValuesDistinct[i] to xValuesNumeric[i]
        innerData = data.convert { column<Any?>(x) }.with { x2num[it] }
    }

    /** Compare means stat */
    private val compareMeans = CompareMeans(
        data = data, x = x, y = y,
        method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
        refGroup = refGroup
    )

    /** max y */
    private var yMax = compareMeans.yMax
    private val yDelta = abs(compareMeans.yMax - compareMeans.yMin) * 0.1

    private fun ensureNotSet(mode: BoxPlotMode, vararg props: KProperty0<Any?>) {
        for (prop in props)
            if (prop.get() != null)
                throw IllegalArgumentException("${prop.name} != null for $mode");
    }

    private val mode: BoxPlotMode = run {
        if (group != null) {
            return@run if (facet) {
                ensureNotSet(WithGroupBy, ::refGroup, ::showOverallPValue)
                FacetBy
            } else {
                ensureNotSet(WithGroupBy, ::refGroup, ::comparisons, ::showOverallPValue)
                WithGroupBy
            }
        }

        if (comparisons != null || allComparisons == true) {
            ensureNotSet(WithComparisons, ::group, ::refGroup)
            return@run WithComparisons
        }

        if (refGroup != null) {
            ensureNotSet(WithReference, ::group, ::comparisons)
            return@run WithReference
        }

        return@run Empty
    }

    /** */
    private fun adjustAndGetYMax() = run {
        yMax += yDelta
        yMax
    }

    /** base box plot */
    private fun basePlot() = run {
        val xDiscrete = asDiscrete(this@BoxPlot.x)
        var plt = letsPlot(innerData.toMap()) {
            x = xDiscrete
            y = this@BoxPlot.y
        }

        plt += scaleXDiscrete(
            breaks = xValuesNumeric,
            limits = xValuesNumeric,
            labels = xValuesDistinct.map { it.toString() }
        )

        plt += geomBoxplot {
            fill = if (facet) xDiscrete else this@BoxPlot.group ?: xDiscrete
        }

        plt += theme(panelGrid = "blank", axisLineY = elementLine())
            .legendPositionNone()

        if (group != null)
            plt += theme().legendPositionTop()

        if (facet)
            plt += facetWrap(facets = group!!, ncol = 1, format = "${this@BoxPlot.group} = {}")

        plt
    }

    /** Add group p-values to the plot */
    private fun addGroupPValues(plot: Plot): Plot = run {
        val stat = compareMeans.stat

        val labels = when (labelFormat) {
            Significance -> stat.pSignif.map { if (hideNS && it == SignificanceLevel.NS) "" else it.string }.toList()
            is Formatted -> stat.pValueFmt.map { labelFormat.format(method, it) }.toList()
            else -> throw RuntimeException()
        }

        plot + geomText(
            mapOf(
                x to stat.group2.toList().map { x2num[it] },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
            x = this@BoxPlot.x
            label = "labels"
        }
    }

    /** Add overall p-value to the plot */
    private fun addOverallPValue(plot: Plot): Plot =
        plot + geomText(
            x = 0,
            y = adjustAndGetYMax(),
//            family = "Inter",
            size = 7,
            label = "${compareMeans.overallPValueMethod}, p = ${compareMeans.overallPValueFmt}"
        )

    /** Add comparisons */
    private fun addComparisons(plot: Plot): Plot = run {
        val stat = if (allComparisons == true)
            compareMeans.stat
        else {
            val set = comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1 to group2) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = yDelta * 0.2
        var plt = plot
        val rows = stat.rows().sortedBy { abs(x2num[it.group1!!]!! - x2num[it.group2]!!) }
        for (row in rows) {
            val yValue = adjustAndGetYMax()
            val gr1 = x2num[row.group1!!]!!
            val gr2 = x2num[row.group2]!!

            plt += geomPath(
                mapOf(
                    x to listOf(gr1, gr1, gr2, gr2),
                    y to listOf(yValue - mustach, yValue, yValue, yValue - mustach)
                ),
                color = "black"
            )

            plt += geomText(
                data = mapOf(
                    x to listOf((gr1 + gr2) / 2.0),
                    y to listOf(yValue + yDelta / 2),
                ),
                size = 7,
                label = row.pValueFmt
            ) {
                x = this@BoxPlot.x
                label = "labels"
            }
        }

        plt
    }

    /** Compare means stat for grouped data: primaryGroup -> stat */
    private val groupedCompareMeans: Map<Any, CompareMeans> by lazy {
        val x: String
        val group: String
        if (facet) {
            x = this@BoxPlot.group!!
            group = this@BoxPlot.x
        } else {
            x = this@BoxPlot.x
            group = this@BoxPlot.group!!
        }

        data.groupBy(x).groups.toList().associate {
            it.first()[x]!! to CompareMeans(
                data = it, x = group, y = y,
                method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
                refGroup = refGroup
            )
        }
    }

    /** Add p-value for each group in grouped boxplot (no facets) */
    private fun addInterGroupPValues(plot: Plot): Plot = run {
        val stat = groupedCompareMeans

        val labels = stat.values.map {
            when (labelFormat) {
                Significance -> it.overallPValueSign.string
                is Formatted -> labelFormat.format(method, it.overallPValueFmt)
                else -> throw RuntimeException()
            }
        }

        plot + geomText(
            mapOf(
                x to stat.keys.toList().map { x2num[it] },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
            x = this@BoxPlot.x
            label = "labels"
        }
    }

//    /** Add p-value for each group in grouped boxplot (with facets) */
//    private fun addInterGroupPValuesFacet(plot: Plot): Plot = run {
//        val stat = groupedCompareMeans
//
//        val labels = stat.values.map {
//            when (labelFormat) {
//                Significance -> it.overallPValueSign.string
//                is Formatted -> labelFormat.format(method, it.overallPValueFmt)
//                else -> throw RuntimeException()
//            }
//        }
//
//        plot + geomText(
//            mapOf(
//                group to stat.keys.toList(),
//                "labels" to labels,
//                x to List(labels.size) { 0 }
//            ),
//            y = adjustAndGetYMax()
//        ) {
//            group = this@BoxPlot.group
//            label = "labels"
//        }
//    }

    /** Add comparisons */
    private fun addComparisonsOnFacets(plot: Plot): Plot = run {
        var plt = plot
        for ((dataGroup, data) in groupedCompareMeans) {
            val stat = if (allComparisons == true)
                data.stat
            else {
                val set = comparisons!!.toSet()
                data.stat
                    .filter { (group1 to group2) in set }
            }.filter { pSignif != SignificanceLevel.NS }

            val mustach = yDelta * 0.2
            val rows = stat.rows().sortedBy { abs(x2num[it.group1!!]!! - x2num[it.group2]!!) }
            for (row in rows) {
                val yValue = adjustAndGetYMax()
                val gr1 = x2num[row.group1!!]!!
                val gr2 = x2num[row.group2]!!

                plt += geomPath(
                    mapOf(
                        x to listOf(gr1, gr1, gr2, gr2),
                        y to listOf(yValue - mustach, yValue, yValue, yValue - mustach),
                        group to List(4) { dataGroup }
                    ),
                    color = "black"
                )

                println((gr1 + gr2) / 2.0)

                plt += geomText(
                    data = mapOf(
                        x to listOf((gr1 + gr2) / 2.0),
                        y to listOf(yValue + yDelta / 2),
                        group to listOf(dataGroup)
                    ),
                    size = 7,
                    label = row.pValueFmt
                ) {
                    x = this@BoxPlot.x
                    y = this@BoxPlot.y
                    group = this@BoxPlot.group
                }
            }
        }

        plt
    }

    val plot = run {
        var plt = basePlot()

        when (mode) {
            Empty -> {
                // nothing to do
            }
            WithReference -> {
                plt = addGroupPValues(plt)
            }
            WithComparisons -> {
                plt = addComparisons(plt)
            }
            WithGroupBy -> {
                plt = addInterGroupPValues(plt)
            }
            FacetBy -> {
//                plt = addInterGroupPValues(plt)
                plt = addComparisonsOnFacets(plt)
            }
        }

        if (showOverallPValue == true) {
            plt = addOverallPValue(plt)
        }

        plt
    }
}
