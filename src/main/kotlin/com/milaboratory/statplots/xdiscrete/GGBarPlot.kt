@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.StatPoint
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
    val color: String? = null,
    val fill: String? = null,
    val size: Double? = null,
    val width: Double? = null,
    val aesMapping: GGAes.() -> Unit = {}
) : WithFeature {
    override fun getFeature(base: GGXDiscrete): Feature = run {
        val aes = GGAes().apply(aesMapping)
        if (statFun == null) {
            geomBar(fill = fill, color = color, width = width, stat = stat, position = position) {
                fill = aes.fill
                color = aes.color
                width = aes.width
            }
        } else {
            val statData = statFun.apply(base.descStat).rename(
                StatPoint::mid.name to base.y,
            ).toMap()
            geomBar(
                data = statData,
                fill = fill,
                color = color,
                width = width,
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
    val stat: StatOptions = Stat.count(),
    val statFun: StatFun? = StatFun.MeanStdErr,
    val position: PosOptions = Pos.stack,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    fill: String? = null,
    orientation: Orientation = Orientation.Vertical,
    val size: Double? = null,
    val width: Double? = null,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy = filterGroupBy(aes.fill, aes.color)

    override var plot = super.plot + ggBar(
        stat = stat,
        color = color,
        statFun = statFun,
        fill = fill,
        size = size,
        width = width,
        position = position,
        aesMapping = aesMapping
    ).getFeature(this)
}
