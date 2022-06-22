@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.positionJitterDodge
import org.jetbrains.kotlinx.dataframe.AnyFrame


/**
 * Add stripchart to the plot.
 */
class ggStrip(
    color: String? = "#000000",
    fill: String? = null,
    shape: Any? = null,
    size: Number? = null,
    val position: PosOptions = Pos.jitterdodge,
    aes: GGAes
) : WithAes(
    color = color,
    fill = fill,
    shape = shape,
    size = size,
    aes = aes
), GGXDiscreteFeature {

    constructor(
        color: String? = "#000000",
        fill: String? = null,
        shape: Any? = null,
        size: Number? = null,
        position: PosOptions = Pos.jitterdodge,
        aesMapping: GGAes.() -> Unit = {}
    ) : this(color, fill, shape, size, position, GGAes().apply(aesMapping))

    override val prepend = false
    override fun getFeature(base: GGXDiscrete) = geomPoint(
        size = this.size,
        shape = this.shape,
        fill = this.fill,
        color = this.color,
        position = this.position
    ) {
        this.fill = aes.fill
        this.color = aes.color
        this.shape = aes.shape
        this.size = aes.size
    }
}

/**
 *
 * Create a stripchart, also known as one dimensional scatter plots. These plots are suitable compared to box plots when sample sizes are small.
 *
 * @param data data frame
 * @param x x series (discrete)
 * @param y y series (continuous)
 * @param facetBy Organize data in facets
 * @param facetNCol Number of columns in facet view
 * @param facetNRow Number of rows in facet view
 * @param color Outline color
 * @param fill Fill color
 * @param shape Shape of points
 * @param orientation Plot orientation
 * @param colorScale Color scale
 * @param fillScale Fill scale
 * @param aesMapping Aesthetics mapping
 */
class GGStripChart(
    data: AnyFrame,
    x: String,
    y: String,
    xValues: List<Any>? = null,
    groupByValues: List<Any>? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = "#000000",
    fill: String? = null,
    shape: String? = null,
    size: Double? = null,
    position: PosOptions = positionJitterDodge(dodgeWidth = 0.2, jitterWidth = 0.2),
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palletes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palletes.Categorical.auto,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
    xValues = xValues,
    groupByValues = groupByValues,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    color = color,
    fill = fill,
    shape = shape,
    size = size,
    position = position,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.shape, aes.color, aes.fill, aes.size)

    override fun basePlot() =
        super.basePlot() + ggStrip(this.color, this.fill, this.shape, this.size, this.position!!, aes)
            .getFeature(this)
}
