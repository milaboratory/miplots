@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.StatFun
import com.milaboratory.statplots.util.StatPoint
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
    val color: String? = null,
    val fill: String? = null,
    val shape: String? = null,
    val size: Double? = null,
    val width: Double? = null,
    val linetype: String? = null,
    val aesMapping: GGAes.() -> Unit = {}
) : WithFeature {
    @Suppress("UNCHECKED_CAST")
    override fun getFeature(base: GGBase) = run {
        val position = this.position ?: if (base is GGBarPlot && base.groupBy != null) positionDodge(1.0) else Pos.identity
        val stat = statFun.apply(base.descStat).toMap()
        val aes = GGAes().apply(aesMapping)

        return@run when (errorPlotType) {
            ErrorPlotType.LineRange -> {
                geomLineRange(
                    stat,
                    color = color,
                    linetype = linetype,
                    size = size,
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
                    color = color,
                    linetype = linetype,
                    size = size,
                    shape = shape,
                    fill = fill,
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
                    stat, color = color,
                    linetype = linetype,
                    size = size,
                    width = width,
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
                    color = color,
                    linetype = linetype,
                    size = size,
                    width = width,
                    fill = fill,
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
