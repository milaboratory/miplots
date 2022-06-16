@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.stat.util.StatPoint
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.intern.layer.StatOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class ggBar(
    val stat: StatOptions = Stat.count(),
    val statFun: StatFun? = null,
    val position: PosOptions = Pos.stack,
    color: String? = null,
    fill: String? = null,
    size: Number? = null,
    width: Double? = null,
    aes: GGAes
) : WithAes(
    color = color,
    fill = fill,
    size = size,
    width = width,
    aes = aes
), GGXDiscreteFeature {
    constructor(
        stat: StatOptions = Stat.count(),
        statFun: StatFun? = null,
        position: PosOptions = Pos.stack,
        color: String? = null,
        fill: String? = null,
        size: Number? = null,
        width: Double? = null,
        aesMapping: GGAes.() -> Unit = {}
    ) : this(stat, statFun, position, color, fill, size, width, GGAes().apply(aesMapping))

    override val prepend = true
    override fun getFeature(base: GGXDiscrete): Feature = run {
        if (statFun == null) {
            geomBar(fill = this.fill, color = this.color, width = this.width, stat = stat, position = position) {
                fill = aes.fill
                color = aes.color
                width = aes.width
            }
        } else {
            val statData = statFun.apply(base.descStat).rename(
                StatPoint::middle.name to base.y,
            ).toMap()
            geomBar(
                data = statData,
                fill = this.fill,
                color = this.color,
                width = this.width,
                stat = Stat.identity,
                position = position
            ) {
                fill = aes.fill
                color = aes.color
                width = aes.width
            }
        }
    }
}

/**
 *
 */
class GGBarPlot(
    data: AnyFrame,
    x: String,
    y: String,
    xValues: List<Any>? = null,
    groupByValues: List<Any>? = null,
    val stat: StatOptions = Stat.count(),
    val statFun: StatFun? = StatFun.MeanStdErr,
    position: PosOptions = Pos.stack,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = "black",
    fill: String? = null,
    size: Number? = null,
    width: Double? = null,
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palletes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palletes.Categorical.auto,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
    xValues = xValues,
    groupByValues = groupByValues,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    color = color,
    fill = fill,
    size = size,
    width = width,
    position = position,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.fill, aes.color)

    override fun basePlot() = super.basePlot() + ggBar(
        stat = stat,
        color = this.color,
        statFun = statFun,
        fill = this.fill,
        size = this.size,
        width = this.width,
        position = this.position!!,
        aes = aes
    ).getFeature(this)
}
