@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

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
    aesMapping: GGAes.() -> Unit = {}
) : WithAes(color = color, linetype = linetype, size = size, aesMapping = aesMapping), GGXDiscreteFeature {
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

//* @param colorScale Color scale
//* @param fillScale Fill scale
class GGLinePlot(
    data: AnyFrame,
    x: String,
    y: String,
    val statFun: StatFun? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = null,
    size: Double? = null,
    linetype: String? = null,
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palletes.Diverging.viridis2magma,
    fillScale: DiscreteColorMapping = Palletes.Diverging.viridis2magma,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
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

    override var plot = super.plot + ggLine(
        color = this.color,
        statFun = statFun,
        size = this.size,
        linetype = this.linetype,
        aesMapping = aesMapping
    ).getFeature(this)
}
