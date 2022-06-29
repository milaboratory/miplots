/*
 *
 * Copyright (c) 2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/miplots/blob/main/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.GGBase
import com.milaboratory.miplots.stat.WithAes
import com.milaboratory.miplots.stat.isCategorial
import com.milaboratory.miplots.stat.util.DescStatByRow
import com.milaboratory.miplots.stat.util.NA
import com.milaboratory.miplots.stat.util.descStatBy
import jetbrains.letsPlot.elementLine
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.label.xlab
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXContinuous
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs

open class GGXDiscrete(
    _data: AnyFrame,
    /** x series (discrete) */
    x: String,
    /** y series (continuous) */
    y: String,
    /** plot only for specific x values */
    val xValues: List<Any>?,
    /** plot only for specific x values */
    val groupByValues: List<Any>?,
    /** Organize data in facets */
    facetBy: String? = null,
    /** Number of columns in facet view */
    facetNCol: Int? = null,
    /** Number of rows in facet view `*/
    facetNRow: Int? = null,
    /** Outline color */
    color: String? = null,
    /** Fill color */
    fill: String? = null,
    /** Point shape */
    shape: String? = null,
    /** Linetype */
    linetype: String? = null,
    /** Size */
    size: Number? = null,
    /** Line width */
    width: Double? = null,
    /** Alpha */
    alpha: Double? = null,
    /** Position */
    position: PosOptions? = null,
    /** Plot orientation */
    orientation: Orientation = Orientation.Vertical,
    /** Color scale */
    colorScale: DiscreteColorMapping,
    /** Fill scale */
    fillScale: DiscreteColorMapping,
    /** Aesthetics mapping */
    aesMapping: GGAes.() -> Unit = {}
) : GGBase(
    x,
    y,
    facetBy,
    facetNCol,
    facetNRow,
    color,
    fill,
    shape,
    linetype,
    size,
    width,
    alpha,
    position,
    orientation,
    colorScale,
    fillScale,
    aesMapping
) {
    init {
        if (orientation == Orientation.Horizontal)
            throw UnsupportedOperationException("horizontal orientation is not supported yet")
    }

    final override val data: AnyFrame

    /** group data by column */
    override val groupBy: String? = null

    // numeric x axis name
    internal val xNumeric = x + "__Numeric"

    // distinct x values
    internal val xdist: List<Any?>

    // x ordinals
    internal val xord: Map<Any?, Int>

    // x numeric values
    internal val xnum: Map<Any?, Double>

    init {
        var data = _data
        if (data[x].all { it is Double })
            throw IllegalArgumentException("x must be categorical")

        val groupBy = aes.list.filterNotNull()
            .filter { it != x }
            .filter { data[it].isCategorial() }
            .firstOrNull()

        if (groupByValues != null && groupBy == null)
            throw IllegalArgumentException("groupBy is null while groupByValues is not")

        if (xValues != null) {

            val vals = data[x].distinct().toSet()
            val xValues = this.xValues.filter { vals.contains(it) }
            val xset = xValues.toSet()
            val xmap = xValues.mapIndexed { i, v -> v to i }.toMap()
            data = data
                .filter { xset.contains(it[x]) }
                .sortWith(Comparator.comparing { xmap[it[x]]!! })
        }

        if (groupByValues != null) {
            groupBy!!

            val vals = data[groupBy].distinct().toSet()
            val groupByValues = this.groupByValues.filter { vals.contains(it) }
            val xset = groupByValues.toSet()
            val xmap = groupByValues.mapIndexed { i, v -> v to i }.toMap()
            data = data
                .filter { xset.contains(it[groupBy]) }
                .sortWith(Comparator.comparing { xmap[it[groupBy]]!! })
        }

        xdist = data[x].distinct().toList()
        xord = xdist.mapIndexed { i, v -> v to i }.toMap()
        xnum = xord.mapValues { (_, v) -> v.toDouble() }
        this.data = data
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

    internal val prependFeatures: MutableList<Feature> = mutableListOf()
    internal val appendFeatures: MutableList<Feature> = mutableListOf()

    protected open fun basePlot(): Plot = run {
        var plt = letsPlot(data.toMap()) {
            this.x = this@GGXDiscrete.xNumeric
            this.y = this@GGXDiscrete.y
        }

        plt += xlab(x)

        plt += scaleXContinuous(
            breaks = xnum.values.toList(),
            labels = xnum.keys.toList().map { it.toString() }
        )

        if (facetBy != null)
            plt += facetWrap(facets = facetBy, ncol = facetNCol, nrow = facetNRow)

        if (aes.color != null)
            plt += colorScale.colorScale(data[aes.color!!].distinct().toList())

        if (aes.fill != null)
            plt += fillScale.fillScale(data[aes.fill!!].distinct().toList())

        plt += theme(axisLineY = elementLine())

        for (f in prependFeatures) {
            plt += f
        }

        plt
    }

    internal fun adjustAes(feature: WithAes) {
        if (aes.color == null && feature.aes.color != null)
            this += colorScale.colorScale(data[feature.aes.color!!].distinct().toList())
        if (aes.fill == null && feature.aes.fill != null)
            this += fillScale.fillScale(data[feature.aes.fill!!].distinct().toList())
    }

    /** base plot */
    final override var plot: Plot
        get() = run {
            var p = basePlot()
            for (f in appendFeatures) {
                p += f
            }
            p
        }
        set(_) {
            throw UnsupportedOperationException()
        }

    @Suppress("UNCHECKED_CAST")
    val descStat by lazy {
        cache.computeIfAbsent("__descriptiveStatistics__") {
            descStatBy(data, y, listOfNotNull(x, facetBy, groupBy)).add(xNumeric) { xnum[it[x]] }
        } as DataFrame<DescStatByRow>
    }
}

operator fun GGXDiscrete.plusAssign(feature: GGXDiscreteFeature) {
    (if (feature.prepend)
        this.prependFeatures
    else
        this.appendFeatures).add(feature.getFeature(this))
}

fun GGXDiscrete.append(feature: GGXDiscreteFeature) = run {
    this.appendFeatures.add(feature.getFeature(this))
    this
}

fun GGXDiscrete.prepend(feature: GGXDiscreteFeature) = run {
    this.prependFeatures.add(feature.getFeature(this))
    this
}

operator fun GGXDiscrete.plus(feature: GGXDiscreteFeature) = run {
    (if (feature.prepend)
        this.prependFeatures
    else
        this.appendFeatures).add(feature.getFeature(this))
    this
}

operator fun GGXDiscrete.plus(feature: Feature) = run {
    this.appendFeatures.add(feature)
    this
}

operator fun GGXDiscrete.plusAssign(feature: Feature) {
    this.appendFeatures.add(feature)
}
