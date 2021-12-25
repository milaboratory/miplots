package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Format
import com.milaboratory.statplots.boxplot.LabelFormat.Companion.Significance
import com.milaboratory.statplots.util.*
import jetbrains.letsPlot.elementLine
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomLine
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
import kotlin.reflect.KProperty0

sealed interface LabelFormat {
    companion object {
        private object significance : LabelFormat

        val Significance: LabelFormat = significance

        data class Format(val fmt: String = "{Method}, p = {pValue}") : LabelFormat {
            fun format(method: TestMethod, pValue: String) =
                fmt.replace("{Method}", method.str)
                    .replace("{pValue}}", pValue)
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

    private fun ord(column: String, value: Any) = data[column].distinct().toList().indexOf(value)

    private fun ensureNotSet(vararg props: KProperty0<Any?>) {
        for (prop in props)
            if (prop.get() != null)
                throw IllegalArgumentException("${prop.name} != null");
    }

    private val mode: BoxPlotMode = run {
        if (comparisons != null || allComparisons == true) {
            ensureNotSet(::group, ::refGroup)
            return@run BoxPlotMode.WithComparisons
        }

        if (refGroup != null) {
            ensureNotSet(::group, ::comparisons)
            return@run BoxPlotMode.WithReference
        }

        if (group != null) {
            ensureNotSet(::refGroup, ::comparisons, ::showOverallPValue)
            return@run if (facet)
                BoxPlotMode.FacetBy
            else
                BoxPlotMode.WithGroupBy
        }

        return@run BoxPlotMode.Empty
    }

    /** */
    private fun adjustAndGetYMax() = run {
        yMax *= 1.1
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
            is Format -> stat.pValueFmt.map { labelFormat.format(method, it) }.toList()
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
//        comparisons!!

        val stat = if (allComparisons == true)
            compareMeans.stat
        else {
            val set = comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1.value() to group2.value()) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = yMax * 0.01
        var plt = plot
        for (row in stat.rows()) {
            val yValue = adjustAndGetYMax()
            val gr1 = row.group1.value()
            val gr2 = row.group2.value()

            plt += geomLine(
                mapOf(
                    x to listOf(gr1, gr2),
                    y to listOf(yValue, yValue)
                )
            )

            plt += geomLine(
                mapOf(
                    x to listOf(gr1, gr1),
                    y to listOf(yValue, yValue - mustach)
                )
            )

            plt += geomLine(
                mapOf(
                    x to listOf(gr2, gr2),
                    y to listOf(yValue, yValue - mustach)
                ),

                )

            plt += geomText(
                x = (ord(x, gr1) + ord(x, gr2)) / 2.0,
                y = yValue + mustach,
                vjust = "top",
                label = row.pValueFmt
            )
        }

        plt
    }

    val plot = run {
        var plt = basePlot()

        when (mode) {
            BoxPlotMode.Empty -> {
                // nothing to do
            }
            BoxPlotMode.WithReference -> {
                plt = addGroupPValues(plt)
            }
            BoxPlotMode.WithComparisons -> {
                plt = addComparisons(plt)
            }
            BoxPlotMode.FacetBy -> {}
            BoxPlotMode.WithGroupBy -> {}
        }

        if (showOverallPValue == true) {
            plt = addOverallPValue(plt)
        }

        plt
    }
}
