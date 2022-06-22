@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import jetbrains.letsPlot.geom.geomViolin
import org.jetbrains.kotlinx.dataframe.AnyFrame


/**
 * Add violin plot geometry & stat to the plot
 *
 * @param color Outline color
 * @param fill Fill color
 * @param drawQuantiles list of float, optional.
 *     Draw horizontal lines at the given quantiles of the density estimate.
 * @param scale string, optional.
 *     If 'area' (default), all violins have the same area.
 *     If 'count', areas are scaled proportionally to the number of observations.
 *     If 'width', all violins have the same maximum width.
 * @param aesMapping additional aes mapping
 */
class ggViolin(
    alpha: Double? = null,
    color: String? = null,
    fill: String? = null,
    width: Double? = null,
    private val trim: Boolean = false,
    private val scale: String? = null,
    private val drawQuantiles: Any? = null,
    /** Additional mapping */
    aes: GGAes,
) : WithAes(alpha = alpha, color = color, fill = fill, width = width, aes = aes), GGXDiscreteFeature {
    override val prepend = true
    override fun getFeature(base: GGXDiscrete) =
        geomViolin(
            alpha = this.alpha,
            color = this.color,
            fill = this.fill,
            width = this.width,
            drawQuantiles = this.drawQuantiles,
            scale = this.scale,
            trim = trim,
        ) {
            this.color = aes.color
            this.fill = aes.fill
        }
}

/**
 * Violin plot
 *
 * @param data data frame
 * @param x x series (discrete)
 * @param y y series (continuous)
 * @param facetBy Organize data in facets
 * @param facetNCol Number of columns in facet view
 * @param facetNRow Number of rows in facet view
 * @param color Outline color
 * @param fill Fill color
 * @param orientation Plot orientation
 * @param colorScale Color scale
 * @param fillScale Fill scale
 * @param drawQuantiles list of float, optional.
 *     Draw horizontal lines at the given quantiles of the density estimate.
 * @param scale string, optional.
 *     If 'area' (default), all violins have the same area.
 *     If 'count', areas are scaled proportionally to the number of observations.
 *     If 'width', all violins have the same maximum width.
 * @param aesMapping Aesthetics mapping
 *
 */
class GGViolinPlot(
    data: AnyFrame,
    x: String,
    y: String,
    xValues: List<Any>? = null,
    groupByValues: List<Any>? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    alpha: Double? = null,
    color: String? = "#000000",
    fill: String? = null,
    width: Double? = null,
    private val trim: Boolean = false,
    private val scale: String? = null,
    private val drawQuantiles: Any? = null,
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palletes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palletes.Categorical.auto,
    val aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
    xValues = xValues,
    groupByValues = groupByValues,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    alpha = alpha,
    color = color,
    width = width,
    fill = fill,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.fill, aes.color)

    /** base box plot */
    override fun basePlot() = super.basePlot() + ggViolin(
        alpha = this.alpha,
        color = this.color,
        fill = this.fill,
        trim = this.trim,
        width = this.width,
        drawQuantiles = drawQuantiles,
        scale = this.scale,
        aes = aes,
    ).getFeature(this)
}
