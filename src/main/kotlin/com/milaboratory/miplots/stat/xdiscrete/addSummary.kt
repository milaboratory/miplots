@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.stat.util.StatPoint
import com.milaboratory.miplots.stat.xdiscrete.ErrorPlotType.*
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.positionDodge
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class addSummary(
    var statFun: StatFun = StatFun.MeanStdDev,
    val errorPlotType: ErrorPlotType = LineRange,
    val position: PosOptions? = null,
    val inheritAes: Boolean = true,
    color: String? = null,
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
    override val prepend = true

    @Suppress("UNCHECKED_CAST")
    override fun getFeature(base: GGXDiscrete) = run {
        if (inheritAes) {
            this.inheritColors(base)
        }

        if (errorPlotType == BoxPlot)
            statFun = StatFun.BoxPlot

        val position =
            this.position ?: if (base is GGBarPlot && base.groupBy != null)
                positionDodge(1.0)
            else if (base is GGStripChart && base.position != null) {
                if (base.position.parameters.has("width"))
                    positionDodge(base.position.parameters.get("width") as Number)
                else if (base.position.parameters.has("dodge_width"))
                    positionDodge(base.position.parameters.get("dodge_width") as Number)
                else
                    base.position
            } else
                base.position ?: Pos.identity
        val stat = statFun.apply(base.descStat).toMap()

        return@run when (errorPlotType) {
            BoxPlot -> {
                geomBoxplot(
                    color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    position = position
                ) {
                    size = aes.size
//                    group = base.groupBy
                    color = aes.color
                    linetype = aes.linetype
                }
            }
            LineRange -> {
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
            PointRange -> {
                geomPointRange(
                    stat,
                    color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    shape = this.shape,
                    fill = this.fill,
                    position = position
                ) {
                    y = StatPoint::middle.name
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
            ErrorBar -> {
                geomErrorBar(
                    stat, color = this.color,
                    linetype = this.linetype,
                    size = this.size,
                    width = this.width ?: 0.1,
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
            Crossbar -> {
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
                    middle = StatPoint::middle.name
                    linetype = aes.linetype
                }
            }
        }
    }
}
