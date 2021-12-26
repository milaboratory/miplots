package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.BoxPlotMode.*
import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Significance
import com.milaboratory.statplots.util.*
import jetbrains.letsPlot.elementLine
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toMap
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
    val x: String,
    val y: String,
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
    override val pAdjustMethod: PValueCorrection.Method? = null,
) : CompareMeansParameters {

    override val formula = Formula(y, Factor(x))
    override val groupBy = if (group == null) null else Factor(group)

    /** Compare means stat */
    private val compareMeans = CompareMeans(data, formula, method, groupBy, pAdjustMethod, refGroup)

    /** Let's plot data */
    private val innerData = data.toMap()

    /** max y */
    private var yMax = compareMeans.yMax
    private val yDelta = abs(compareMeans.yMax - compareMeans.yMin) * 0.1

    private fun ord(column: String, value: Any): Int = data[column].distinct().toList().indexOf(value)
    private fun ord(column: String, value: RefGroup): Int = ord(column, value.value())

    private fun ensureNotSet(mode: BoxPlotMode, vararg props: KProperty0<Any?>) {
        for (prop in props)
            if (prop.get() != null)
                throw IllegalArgumentException("${prop.name} != null for $mode");
    }

    private val mode: BoxPlotMode = run {
        if (comparisons != null || allComparisons == true) {
            ensureNotSet(WithComparisons, ::group, ::refGroup)
            return@run WithComparisons
        }

        if (refGroup != null) {
            ensureNotSet(WithReference, ::group, ::comparisons)
            return@run WithReference
        }

        if (group != null) {
            ensureNotSet(WithGroupBy, ::refGroup, ::comparisons, ::showOverallPValue)
            return@run if (facet)
                FacetBy
            else
                WithGroupBy
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
        var plt = letsPlot(innerData) {
            x = this@BoxPlot.x
            y = this@BoxPlot.y
            group = this@BoxPlot.group
        }

        plt += scaleXDiscrete(limits = data[x].distinct().toList().filterNotNull())

        plt += geomBoxplot {
            fill = this@BoxPlot.x
        }

        plt += theme(panelGrid = "blank", axisLineY = elementLine())
            .legendPositionNone()

        //.legendPositionTop()l

        plt
    }

    /**
     * Add overall p-value to the plot
     */
    private fun addGroupPValues(plot: Plot): Plot = run {
        val stat = compareMeans.stat

        val labels = when (labelFormat) {
            Significance -> stat.pSignif.map { if (hideNS && it == SignificanceLevel.NS) "" else it.string }.toList()
            is Formatted -> stat.pValueFmt.map { labelFormat.format(method, it) }.toList()
            else -> throw RuntimeException()
        }

        plot + geomText(
            mapOf(
                x to stat.group2.toList().map { it.value() },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
            x = this@BoxPlot.x
            label = "labels"
        }
    }

    /**
     * Add overall p-value to the plot
     */
    private fun addOverallPValue(plot: Plot): Plot =
        plot + geomText(
            x = 0,
            y = adjustAndGetYMax(),
//            family = "Inter",
            size = 7,
            label = "${compareMeans.overallPValueMethod}, p = ${compareMeans.overallPValueFmt}"
        )

    private fun addComparisons(plot: Plot): Plot = run {
        val stat = if (allComparisons == true)
            compareMeans.stat
        else {
            val set = comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1.value() to group2.value()) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = yDelta * 0.2
        var plt = plot
        val rows = stat.rows().sortedBy { abs(ord(x, it.group1) - ord(x, it.group2)) }
        for (row in rows) {
            val yValue = adjustAndGetYMax()
            val gr1 = row.group1.value()
            val gr2 = row.group2.value()

            plt += geomPath(
                mapOf(
                    x to listOf(gr1, gr1, gr2, gr2),
                    y to listOf(yValue - mustach, yValue, yValue, yValue - mustach)
                ),
                color = "black"
            )

            plt += geomText(
                x = (ord(x, gr1) + ord(x, gr2)) / 2.0,
                y = yValue + yDelta / 2,
                size = 7,
                label = row.pValueFmt
            )
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
            FacetBy -> {}
            WithGroupBy -> {}
        }

        if (showOverallPValue == true) {
            plt = addOverallPValue(plt)
        }

        plt
    }
}
