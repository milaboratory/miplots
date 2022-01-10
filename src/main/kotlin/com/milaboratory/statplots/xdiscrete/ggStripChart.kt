@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.WithFeature
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.layer.PosOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame


/**
 *
 */
class ggStripChartFeature(
    val color: String? = null,
    val fill: String? = null,
    val shape: Any? = null,
    val size: Double? = null,
    val position: PosOptions = Pos.jitterdodge,
    aesMapping: ggBaseAes.() -> Unit = {}
) : WithFeature {
    internal val aes = ggBaseAes().apply(aesMapping)
    override fun getFeature(base: ggBase) = geomPoint(
        size = size,
        shape = shape,
        fill = fill,
        color = color,
        position = position
    ) {
        this.fill = aes.fill
        this.color = aes.color
        this.shape = aes.shape
        this.size = aes.size
    }
}

/**
 *
 */
class ggStripChart(
    data: AnyFrame,
    x: String,
    y: String,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    fill: String? = null,
    orientation: Orientation = Orientation.Vertical,
    val shape: Any? = null,
    val size: Double? = null,
    val position: PosOptions = Pos.jitterdodge,
    aesMapping: ggBaseAes.() -> Unit = {}
) : ggBase(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy: String? = distinctGroupBy(aes.shape ?: aes.color ?: aes.fill ?: aes.size)

    override var plot = super.plot + ggStripChartFeature(color, fill, shape, size, position, aesMapping)
        .getFeature(this)
}