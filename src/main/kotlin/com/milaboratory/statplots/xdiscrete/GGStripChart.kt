@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.WithFeature
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.layer.PosOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame


/**
 *
 */
class ggStrip(
    val color: String? = null,
    val fill: String? = null,
    val shape: Any? = null,
    val size: Double? = null,
    val position: PosOptions = Pos.jitterdodge,
    aesMapping: GGAes.() -> Unit = {}
) : WithFeature {
    internal val aes = GGAes().apply(aesMapping)
    override fun getFeature(base: GGXDiscrete) = geomPoint(
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
class GGStripChart(
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
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy = filterGroupBy(aes.shape, aes.color, aes.fill, aes.size)

    override var plot = super.plot + ggStrip(color, fill, shape, size, position, aesMapping)
        .getFeature(this)
}
