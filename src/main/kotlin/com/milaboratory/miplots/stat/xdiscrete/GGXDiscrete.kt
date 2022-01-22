@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.GGBase
import com.milaboratory.miplots.stat.util.DescStatByRow
import com.milaboratory.miplots.stat.util.NA
import com.milaboratory.miplots.stat.util.descStatBy
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
open class GGXDiscrete(
    _data: AnyFrame,
    /** x series (discrete) */
    x: String,
    /** y series (continuous) */
    y: String,

    /** Organize data in facets */
    facetBy: String? = null,
    /** Number of columns in facet view */
    facetNCol: Int? = null,
    /** Number of rows in facet view */
    facetNrow: Int? = null,
    /** Outline color */
    color: String? = null,
    /** Fill color */
    fill: String? = null,
    /** Plot orientation */
    orientation: Orientation = Orientation.Vertical,
    /** Aesthetics mapping */
    aesMapping: GGAes.() -> Unit = {}
) : GGBase(x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {
    init {
        if (orientation == Orientation.Horizontal)
            throw UnsupportedOperationException("horizontal orientation is not supported yet")
    }

    final override val data: AnyFrame

    override val groupBy: String? = null

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
        this.data = _data
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
            this.x = this@GGXDiscrete.xNumeric
            this.y = this@GGXDiscrete.y
        }

        plt += xlab(x)

        plt += scaleXContinuous(
            breaks = xnum.values.toList(),
            labels = xnum.keys.toList().map { it.toString() })

        if (facetBy != null)
            plt += facetWrap(facets = facetBy, ncol = facetNCol, nrow = facetNrow)

        plt
    }

    @Suppress("UNCHECKED_CAST")
    val descStat by lazy {
        cache.computeIfAbsent("__descriptiveStatistics__") {
            descStatBy(data, y, listOfNotNull(x, facetBy, groupBy)).add(xNumeric) { xnum[it[x]] }
        } as DataFrame<DescStatByRow>
    }
}

operator fun GGXDiscrete.plusAssign(feature: GGXDiscreteFeature) {
    this.plot += feature.getFeature(this)
}

operator fun GGXDiscrete.plus(feature: GGXDiscreteFeature) = run {
    this.plot += feature.getFeature(this)
    this
}
