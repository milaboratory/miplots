package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.util.*
import com.milaboratory.statplots.xdiscrete.BoxPlotMode.*
import com.milaboratory.statplots.xdiscrete.LabelFormat.Companion.Formatted
import com.milaboratory.statplots.xdiscrete.LabelFormat.Companion.Significance
import jetbrains.letsPlot.elementBlank
import jetbrains.letsPlot.elementLine
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.xlab
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXContinuous
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
    WithFacet
}

interface BoxPlotParameters : CompareMeansParameters {
    /** Group data by specified column */
    val group: String?

    /** Organize groupped data in facets */
    val facetWrap: Boolean?

    /** Number of columns/rows in facet view */
    val ncol: Int?
    val nrow: Int?

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
    override val facetWrap: Boolean? = null,
    override val ncol: Int? = null,
    override val nrow: Int? = null,
    override val showOverallPValue: Boolean? = null,
    override val comparisons: List<Pair<String, String>>? = null,
    override val allComparisons: Boolean? = null,
    override val refGroup: RefGroup? = null,
    override val hideNS: Boolean = false,
    override val labelFormat: LabelFormat = Significance,
    _yMin: Double? = null,
    _yMax: Double? = null,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = null,
) : BoxPlotParameters {
    override val paired = false
    override val data: AnyFrame

    // numeric x axis name
    private val xNumeric = x + "__Numeric"

    // x ordinals
    private val xord: Map<Any?, Int>

    // x numeric values
    private val xnum: Map<Any?, Double>

    init {
        if (_data[x].all { it is Double })
            throw IllegalArgumentException("x must be categorical")
        val xdist = _data[x].distinct().toList()
        xord = xdist.mapIndexed { i, v -> v to i }.toMap()
        xnum = xord.mapValues { (_, v) -> v.toDouble() }
        data = _data
            .fillNA { cols(x, *listOfNotNull(group).toTypedArray()) }
            .with { NA }
            .add(xNumeric) { xnum[it[x]]!! }
    }

    /** Compare means stat */
    private val compareMeans = CompareMeans(
        data = data, x = x, y = y,
        method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
        refGroup = refGroup
    )

    /** y column */
    private val ydata = data[y].convertToDouble()

    /** minmax y */
    private var yMin = _yMin ?: ydata.min()
    private var yMax = _yMax ?: ydata.max()
    private val yDelta = abs(yMax - yMin) * 0.1

    private fun ensureNotSet(mode: BoxPlotMode, vararg props: KProperty0<Any?>) {
        for (prop in props)
            if (prop.get() != null)
                throw IllegalArgumentException("${prop.name} != null for $mode");
    }

    private val mode: BoxPlotMode = run {
        if (group != null) {
            ensureNotSet(WithGroupBy, ::refGroup, ::comparisons, ::showOverallPValue)
            return@run if (facetWrap == true) WithFacet else WithGroupBy
        }

        if (comparisons != null || allComparisons == true) {
            ensureNotSet(WithComparisons, ::group, ::refGroup, ::facetWrap, ::ncol, ::nrow)
            return@run WithComparisons
        }

        if (refGroup != null) {
            ensureNotSet(WithReference, ::group, ::comparisons, ::facetWrap, ::ncol, ::nrow)
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
            x = this@BoxPlot.xNumeric
            y = this@BoxPlot.y
        }

        plt += geomBoxplot {
            fill =
                if (this@BoxPlot.group == null)
                    this@BoxPlot.x
                else
                    if (this@BoxPlot.facetWrap == true)
                        this@BoxPlot.x
                    else
                        this@BoxPlot.group

        }

        plt += xlab(x)

        plt += theme(panelGrid = elementBlank(), panelGridMajorY = elementLine())
            .legendPositionNone()

        plt += scaleXContinuous(
            breaks = xnum.values.toList(),
            labels = xnum.keys.toList().map { it.toString() })

        if (group != null && facetWrap == false)
            plt += theme().legendPositionTop()

        plt
    }

    /** Add group p-values to the plot */
    private fun addPValues(plot: Plot): Plot = run {
        val stat = compareMeans.stat

        val labels = when (labelFormat) {
            Significance -> stat.pSignif.map { if (hideNS && it == SignificanceLevel.NS) "" else it.string }.toList()
            is Formatted -> stat.pValueFmt.map { labelFormat.format(method, it) }.toList()
            else -> throw RuntimeException()
        }

        plot + geomText(
            mapOf(
                xNumeric to stat.group2.toList().map { xnum[it] },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
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
    /** Add comparisons */
    private fun addComparisons(
        plot: Plot,
        compareMeans: CompareMeans,
        facet: Any?
    ): Plot = run {
        var plt = plot
        val stat = if (allComparisons == true)
            compareMeans.stat
        else {
            val set = comparisons!!.toSet()
            compareMeans.stat
                .filter { (group1 to group2) in set }
        }.filter { pSignif != SignificanceLevel.NS }

        val mustach = yDelta * 0.2
        val rows = stat.rows().sortedBy { abs(xord[it.group1!!]!! - xord[it.group2]!!) }
        for (row in rows) {
            val yValue = adjustAndGetYMax()
            val gr1 = xnum[row.group1!!]!!
            val gr2 = xnum[row.group2]!!

            val pathData: MutableMap<String, List<Any?>> = mutableMapOf(
                xNumeric to listOf(gr1, gr1, gr2, gr2),
                y to listOf(yValue - mustach, yValue, yValue, yValue - mustach),
            )
            if (facet != null)
                pathData += group!! to List(4) { facet }

            plt += geomPath(pathData, color = "black")

            val textData: MutableMap<String, List<Any?>> = mutableMapOf(
                xNumeric to listOf((gr1 + gr2) / 2.0),
                y to listOf(yValue + yDelta / 2),
                "label" to listOf(row.pValueFmt)
            )
            if (facet != null)
                textData += group!! to listOf(facet)

            plt += geomText(data = textData, size = 6) {
                label = "label"
            }
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
    private fun addPValuesByGroup(plot: Plot): Plot = run {
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
                xNumeric to stat.keys.toList().map { xnum[it] },
                "labels" to labels
            ),
            y = adjustAndGetYMax()
        ) {
            label = "labels"
        }
    }

    /** Compare means stat by groups */
    private val compareMeansFacets by lazy {
        data.groupBy(group!!).groups.toList().map {
            it.first()[group] to CompareMeans(
                data = it, x = x, y = y,
                method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
                refGroup = refGroup
            )
        }
    }

    /** Add comparisons */
    private fun addFacetComparisons(plot: Plot): Plot = run {
        var plt = plot
        for ((facet, compareMeans) in compareMeansFacets) {
            plt = addComparisons(plt, compareMeans, facet)
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
                plt = addPValues(plt)
            }
            WithComparisons -> {
                plt = addComparisons(plt, this.compareMeans, null)
            }
            WithGroupBy -> {
                plt = addPValuesByGroup(plt)
            }

            WithFacet -> {
                plt = addFacetComparisons(plt)
            }
        }

        if (showOverallPValue == true) {
            plt = addOverallPValue(plt)
        }

        if (facetWrap == true)
            plt += facetWrap(facets = group!!, ncol = ncol, nrow = nrow)

        plt
    }
}
