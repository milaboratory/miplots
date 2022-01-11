@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.PlotWrapper
import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.DescStatByRow
import com.milaboratory.statplots.util.NA
import com.milaboratory.statplots.util.descStatBy
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.xlab
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs

enum class Orientation {
    Vertical,
    Horizontal
}

/**
 *
 */
open class GGBase(
    _data: AnyFrame,
    /** x series (discrete) */
    val x: String,
    /** y series (continuous) */
    val y: String,

    /** Organize data in facets */
    val facetBy: String? = null,
    /** Number of columns in facet view */
    val facetNCol: Int? = null,
    /** Number of rows in facet view */
    val facetNrow: Int? = null,
    /** Outline color */
    val color: String? = null,
    /** Fill color */
    val fill: String? = null,
    /** Plot orientation */
    val orientation: Orientation = Orientation.Vertical,
    /** Aesthetics mapping */
    aesMapping: GGAes.() -> Unit = {}
) : PlotWrapper {
    init {
        if (orientation == Orientation.Horizontal)
            throw UnsupportedOperationException("horizontal orientation is not supported yet")
    }

    val aes = GGAes().apply(aesMapping)

    val data: AnyFrame

    /** whether grouping was applied */
    open val groupBy: String? = null
    protected fun distinctGroupBy(g: String?) = if (g == x) null else g

    // numeric x axis name
    internal val xNumeric = x + "__Numeric"

    // x ordinals
    internal val xord: Map<Any?, Int>

    // x numeric values
    internal val xnum: Map<Any?, Double>

    init {
        if (_data[x].all { it is Double })
            throw IllegalArgumentException("x must be categorical")

        val xdist = _data[x].distinct().toList()
        xord = xdist.mapIndexed { i, v -> v to i }.toMap()
        xnum = xord.mapValues { (_, v) -> v.toDouble() }
        data = _data
            .fillNA { cols(x, *listOfNotNull(facetBy).toTypedArray()) }
            .with { NA }
            .add(xNumeric) { xnum[it[x]]!! }
    }

    /** y column */
    internal val ydata = data[y].convertToDouble()

    /** minmax y */
    internal var yMin = ydata.min()
    internal var yMax = ydata.max()
    internal val yDelta = abs(yMax - yMin) * 0.1

    /** */
    internal fun adjustAndGetYMax() = run {
        yMax += yDelta
        yMax
    }

    /** base plot */
    override var plot: Plot = run {
        var plt = letsPlot(data.toMap()) {
            x = this@GGBase.xNumeric
            y = this@GGBase.y
        }

        plt += xlab(x)

        plt += scaleXContinuous(
            breaks = xnum.values.toList(),
            labels = xnum.keys.toList().map { it.toString() })

        if (facetBy != null)
            plt += facetWrap(facets = facetBy, ncol = facetNCol, nrow = facetNrow)

        plt
    }

    /** general cache */
    internal val cache = mutableMapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    val descStat by lazy {
        cache.computeIfAbsent("__descriptiveStatistics__") {
            descStatBy(data, y, listOfNotNull(x, facetBy, groupBy)).add(xNumeric) { xnum[it[x]] }
        } as DataFrame<DescStatByRow>
    }
}

operator fun GGBase.plusAssign(feature: WithFeature) {
    this.plot += feature.getFeature(this)
}

operator fun GGBase.plus(feature: WithFeature) = run {
    this.plot += feature.getFeature(this)
    this
}
