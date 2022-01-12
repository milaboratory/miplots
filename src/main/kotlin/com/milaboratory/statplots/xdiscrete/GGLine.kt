@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.StatPoint
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.intern.Feature
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.toMap

enum class ggLineType {
    LineAndDot,
    Line,
    Dot
}

/**
 *
 */
class ggLine(
    val statFun: StatFun? = null,
    val color: String? = null,
    val linetype: String? = null,
    val size: Double? = null,
    val aesMapping: GGAes.() -> Unit = {}
) : WithFeature {
    override fun getFeature(base: GGXDiscrete): Feature = run {
        val aes = GGAes().apply(aesMapping)
        if (statFun == null) {
            geomLine(
                color = color,
                linetype = linetype,
            ) {
                color = aes.color
                linetype = aes.linetype
                group = color ?: linetype
            }
        } else {
            val statData = statFun.apply(base.descStat).rename(
                StatPoint::mid.name to base.y,
            ).toMap()
            geomLine(
                data = statData,
                color = color,
                linetype = linetype
            ) {
                color = aes.color
                linetype = aes.linetype
                group = color ?: linetype
            }
        }
    }
}

class GGLinePlot(
    data: AnyFrame,
    x: String,
    y: String,
    val statFun: StatFun? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    orientation: Orientation = Orientation.Vertical,
    val size: Double? = null,
    val linetype: String? = null,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(data, x, y, facetBy, facetNCol, facetNrow, color, null, orientation, aesMapping) {

    override val groupBy = filterGroupBy(aes.linetype, aes.color)

    override var plot = super.plot + ggLine(
        color = color,
        statFun = statFun,
        size = size,
        linetype = linetype,
        aesMapping = aesMapping
    ).getFeature(this)
}
