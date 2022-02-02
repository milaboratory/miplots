@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.stat.util.StatPoint
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.geom.geomCrossbar
import jetbrains.letsPlot.geom.geomErrorBar
import jetbrains.letsPlot.geom.geomLineRange
import jetbrains.letsPlot.geom.geomPointRange
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.positionDodge
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class addSummary(
    val statFun: StatFun,
    val errorPlotType: ErrorPlotType,
    val position: PosOptions? = null,
    color: String? = "#000000",
    fill: String? = null,
    shape: String? = null,
    size: Double? = null,
    width: Double? = null,
    linetype: String? = null,
    aesMapping: GGAes.() -> Unit = {}
) : WithAes(
    color = color,
    fill = fill,
    shape = shape,
    size = size,
    width = width,
    linetype = linetype,
    aesMapping = aesMapping,
), GGXDiscreteFeature {
    @Suppress("UNCHECKED_CAST")
    override fun getFeature(base: GGXDiscrete) = run {
        val position =
            this.position ?: if (base is GGBarPlot && base.groupBy != null) positionDodge(1.0) else Pos.identity
        val stat = statFun.apply(base.descStat).toMap()

        return@run when (errorPlotType) {
            ErrorPlotType.LineRange -> {
                geomLineRange(
                    stat,
                    color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    position = position
                ) {
                    ymin = StatPoint::lower.name
                    ymax = StatPoint::upper.name
                    size = aes.size
                    group = base.groupBy
                    color = aes.color
                    linetype = aes.linetype
                }
            }
            ErrorPlotType.PointRange -> {
                geomPointRange(
                    stat,
                    color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    shape = this.shape,
                    fill = this.fill,
                    position = position
                ) {
                    y = StatPoint::mid.name
                    ymin = StatPoint::lower.name
                    ymax = StatPoint::upper.name
                    size = aes.size
                    fill = aes.fill
                    group = base.groupBy
                    color = aes.color
                    shape = aes.shape
                    linetype = aes.linetype
                }
            }
            ErrorPlotType.ErrorBar -> {
                geomErrorBar(
                    stat, color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    width = this.width,
                    position = position
                ) {
                    ymin = StatPoint::lower.name
                    ymax = StatPoint::upper.name
                    size = aes.size
                    color = aes.color
                    group = base.groupBy
                    width = aes.width
                    linetype = aes.linetype
                }
            }
            ErrorPlotType.Crossbar -> {
                geomCrossbar(
                    stat,
                    color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    width = this.width,
                    fill = this.fill,
                    position = position
                ) {
                    ymin = StatPoint::lower.name
                    ymax = StatPoint::upper.name
                    size = aes.size
                    fill = aes.fill
                    group = base.groupBy
                    width = aes.width
                    color = aes.color
                    middle = StatPoint::mid.name
                    linetype = aes.linetype
                }
            }
        }
    }
}
