@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.DescStatByRow
import com.milaboratory.statplots.util.ErrorFun
import com.milaboratory.statplots.util.ErrorPoint
import com.milaboratory.statplots.util.descStatBy
import jetbrains.letsPlot.geom.geomCrossbar
import jetbrains.letsPlot.geom.geomErrorBar
import jetbrains.letsPlot.geom.geomLineRange
import jetbrains.letsPlot.geom.geomPointRange
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class addSummary(
    val errorFun: ErrorFun,
    val errorPlotType: ErrorPlotType,
    val color: String? = null,
    val fill: String? = null,
    val shape: String? = null,
    val size: Double? = null,
    val width: Double? = null,
    val linetype: String? = null,
    val aesMapping: ggBaseAes.() -> Unit = {}
) : WithFeature {

    @Suppress("UNCHECKED_CAST")
    override fun getFeature(base: ggBase) = run {
        val descStat = base.cache.computeIfAbsent("__descriptiveStatistics__") {
            descStatBy(base.data, base.y, listOfNotNull(base.x, base.facetBy, base.groupBy))
        } as DataFrame<DescStatByRow>

        val stat = errorFun.apply(descStat).add(base.xNumeric) { base.xnum[it[base.x]] }.toMap()
        val aes = ggBaseAes().apply(aesMapping)

        return@run when (errorPlotType) {
            ErrorPlotType.LineRange -> {
                geomLineRange(stat, color = color, linetype = linetype, size = size) {
                    ymin = ErrorPoint::lower.name
                    ymax = ErrorPoint::upper.name
                    color = aes.color
                    linetype = aes.linetype
                    size = aes.size
                }
            }
            ErrorPlotType.PointRange -> {
                geomPointRange(stat, color = color, linetype = linetype, size = size, shape = shape, fill = fill) {
                    ymin = ErrorPoint::lower.name
                    y = ErrorPoint::mid.name
                    ymax = ErrorPoint::upper.name
                    color = aes.color
                    linetype = aes.linetype
                    size = aes.size
                    shape = aes.shape
                    fill = aes.fill
                }
            }
            ErrorPlotType.ErrorBar -> {
                geomErrorBar(stat, color = color, linetype = linetype, size = size, width = width) {
                    ymin = ErrorPoint::lower.name
                    ymax = ErrorPoint::upper.name
                    color = aes.color
                    linetype = aes.linetype
                    size = aes.size
                    width = aes.width
                }
            }
            ErrorPlotType.Crossbar -> {
                geomCrossbar(stat, color = color, linetype = linetype, size = size, width = width, fill = fill) {
                    ymin = ErrorPoint::lower.name
                    middle = ErrorPoint::mid.name
                    ymax = ErrorPoint::upper.name
                    color = aes.color
                    linetype = aes.linetype
                    size = aes.size
                    width = aes.width
                    fill = aes.fill
                }
            }
        }
    }
}
