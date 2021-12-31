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
import jetbrains.letsPlot.scale.scaleFillDiscrete
import jetbrains.letsPlot.scale.xlim
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
}

interface BoxPlotParameters : CompareMeansParameters {
    /** Group data by specified column */
    val group: String?

    /** Show overall p-value */
    val showOverallPValue: Boolean?

    /** Pairs to show */
    val comparisons: List<Pair<String, String>>?

    /** Show all pairs */
    val allComparisons: Boolean?

    /** Hide non significant p-value*/
    val hideNS: Boolean

    /** Format of p-value labels */
    val labelFormat: LabelFormat
}

/**
 *
 */
class BoxPlot(
    _data: AnyFrame,
    override val x: String,
    override val y: String,
    override val group: String? = null,
    override val showOverallPValue: Boolean? = null,
    override val comparisons: List<Pair<String, String>>? = null,
    override val allComparisons: Boolean? = null,
    override val refGroup: RefGroup? = null,
    override val hideNS: Boolean = false,
    override val labelFormat: LabelFormat = Significance,
    _yMin: Double? = null,
    _yMax: Double? = null,
    _xLim: List<Any?>? = null,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = null,
) : BoxPlotParameters {

    override val data = _data.fillNA { cols(x, *listOfNotNull(group).toTypedArray()) }.with { NA }

    init {
        if (data[x].all { it is Double })
            throw IllegalArgumentException("x must be categorical")
    }

    /** ordinals for x variable */
    private val xord = (_xLim ?: data[x].distinct().toList()).mapIndexed { i, e -> e to i }.toMap()

    /** Compare means stat */
    private val compareMeans = CompareMeans(
        data = data, x = x, y = y,
        method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
        refGroup = refGroup
    )

    /** minmax y */
    private var yMin = _yMin ?: compareMeans.yMin
    private var yMax = _yMax ?: compareMeans.yMax
    private val yDelta = abs(yMax - yMin) * 0.1

    private fun ensureNotSet(mode: BoxPlotMode, vararg props: KProperty0<Any?>) {
        for (prop in props)
            if (prop.get() != null)
                throw IllegalArgumentException("${prop.name} != null for $mode");
    }

    private val mode: BoxPlotMode = run {
        if (group != null) {
            ensureNotSet(WithGroupBy, ::refGroup, ::comparisons, ::showOverallPValue)
            return@run WithGroupBy
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
        var plt = letsPlot(data.toMap()) {
            x = this@BoxPlot.x
            y = this@BoxPlot.y
        }

        plt += geomBoxplot {
            fill = this@BoxPlot.group ?: this@BoxPlot.x
        }

        plt += theme(panelGrid = "blank", axisLineY = elementLine())
            .legendPositionNone()

        if (group != null)
            plt += theme().legendPositionTop()

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
                x to stat.group2.toList().map { it },
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
        val rows = stat.rows().sortedBy { abs(xord[it.group1!!]!! - xord[it.group2]!!) }
        for (row in rows) {
            val yValue = adjustAndGetYMax()
            val gr1 = row.group1!!
            val gr2 = row.group2

            plt += geomPath(
                mapOf(
                    x to listOf(gr1, gr1, gr2, gr2),
                    y to listOf(yValue - mustach, yValue, yValue, yValue - mustach)
                ),
                color = "black"
            )

            plt += geomText(
                x = (xord[gr1]!! + xord[gr2]!!) / 2.0,
                y = yValue + yDelta / 2,
                size = 7,
                label = row.pValueFmt
            )
        }

        plt
    }

    /** Compare means stat for grouped data: primaryGroup -> stat */
    private val groupedCompareMeans: Map<Any, CompareMeans> by lazy {
        data.groupBy(this@BoxPlot.x).groups.toList().associate {
            it.first()[x]!! to CompareMeans(
                data = it, x = this@BoxPlot.group!!, y = y,
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
                x to stat.keys.toList().map { it },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
            x = this@BoxPlot.x
            label = "labels"
        }
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
        }

        if (showOverallPValue == true) {
            plt = addOverallPValue(plt)
        }

        plt
    }
}
