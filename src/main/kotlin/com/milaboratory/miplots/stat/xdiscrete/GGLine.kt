@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.stat.util.StatPoint
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.intern.Feature
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class ggLine(
    val statFun: StatFun? = null,
    color: String? = null,
    linetype: String? = null,
    size: Number? = null,
    aes: GGAes
) : WithAes(color = color, linetype = linetype, size = size, aes = aes), GGXDiscreteFeature {
    constructor(
        statFun: StatFun? = null,
        color: String? = null,
        linetype: String? = null,
        size: Number? = null,
        aesMapping: GGAes.() -> Unit
    ) : this(statFun, color, linetype, size, GGAes().apply(aesMapping))

    override val prepend = true
    override fun getFeature(base: GGXDiscrete): Feature = run {
        if (statFun == null) {
            geomLine(
                color = this.color,
                linetype = this.linetype,
            ) {
                color = aes.color
                linetype = aes.linetype
                group = this.color ?: this.linetype
            }
        } else {
            val statData = statFun.apply(base.descStat).rename(
                StatPoint::middle.name to base.y,
            ).toMap()
            geomLine(
                data = statData,
                color = this.color,
                linetype = this.linetype
            ) {
                color = aes.color
                linetype = aes.linetype
                group = this.color ?: this.linetype
            }
        }
    }
}

class GGLinePlot(
    data: AnyFrame,
    x: String,
    y: String,
    xValues: List<Any>? = null,
    groupByValues: List<Any>? = null,
    val statFun: StatFun? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = "#000000",
    size: Double? = null,
    linetype: String? = null,
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
    size = size,
    linetype = linetype,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.linetype, aes.color)

    override fun basePlot() = super.basePlot() + ggLine(
        statFun = statFun,
        color = this.color,
        size = this.size,
        linetype = this.linetype,
        aes = aes
    ).getFeature(this)
}
