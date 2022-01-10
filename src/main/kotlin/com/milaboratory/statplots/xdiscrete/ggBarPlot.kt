package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.StatPoint
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.PosKind
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.intern.layer.StatOptions
import jetbrains.letsPlot.positionDodge
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class ggBarPlotFeature(
    val stat: StatOptions = Stat.count(),
    val statFun: StatFun? = null,
    val errorPlotType: ErrorPlotType = ErrorPlotType.ErrorBar,
    val position: PosOptions = Pos.stack,
    val color: String? = null,
    val fill: String? = null,
    val size: Double? = null,
    val width: Double? = null,
    val aesMapping: ggBaseAes.() -> Unit = {}
) : WithFeature {
    override fun getFeature(base: ggBase): Feature = run {
        val aes = ggBaseAes().apply(aesMapping)
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
            } + addSummary(
                statFun = statFun,
                errorPlotType = errorPlotType,
                color = color,
                position = if (position.kind == PosKind.DODGE) positionDodge(1.0) else position,
                fill = fill,
                size = size,
                width = 0.1,
                aesMapping = aesMapping
            ).getFeature(base)
        }
    }
}

/**
 *
 */
class ggBarPlot(
    data: AnyFrame,
    x: String,
    y: String,
    val stat: StatOptions = Stat.count(),
    val statFun: StatFun? = null,
    val errorPlotType: ErrorPlotType = ErrorPlotType.ErrorBar,
    val position: PosOptions = Pos.stack,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    fill: String? = null,
    orientation: Orientation = Orientation.Vertical,
    val size: Double? = null,
    val width: Double? = null,
    aesMapping: ggBaseAes.() -> Unit = {}
) : ggBase(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy: String? = distinctGroupBy(aes.fill ?: aes.color)

    override var plot = super.plot + ggBarPlotFeature(
        stat = stat,
        color = color,
        statFun = statFun,
        errorPlotType = errorPlotType,
        fill = fill,
        size = size,
        width = width,
        position = position,
        aesMapping = aesMapping
    ).getFeature(this)
}
