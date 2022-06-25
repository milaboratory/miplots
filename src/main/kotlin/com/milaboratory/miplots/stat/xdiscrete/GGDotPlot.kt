@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import jetbrains.letsPlot.geom.geomYDotplot
import jetbrains.letsPlot.intern.layer.PosOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame


/**
 * Add dot plot geometry & stat to the plot
 *
 * @param color Outline color
 * @param fill Fill color
 * @param aes additional aes
 */
class ggDot(
    alpha: Double? = null,
    color: String? = null,
    fill: String? = null,
    val position: PosOptions? = null,
    val stackGroups: Boolean? = null,
    aes: GGAes,
) : WithAes(alpha = alpha, color = color, fill = fill, aes = aes), GGXDiscreteFeature {
    constructor(
        alpha: Double? = null,
        color: String? = null,
        fill: String? = null,
        position: PosOptions? = null,
        stackGroups: Boolean? = null,
        aesMapping: GGAes.() -> Unit = {}
    ) : this(alpha, color, fill, position, stackGroups, GGAes().apply(aesMapping))

    override val prepend = false
    override fun getFeature(base: GGXDiscrete) = run {
        inheritColors(base)
        base.adjustAes(this)
        geomYDotplot(
            alpha = this.alpha,
            color = this.color,
            position = this.position,
            stackGroups = this.stackGroups,
            fill = this.fill,
        ) {
            this.color = aes.color
            this.fill = aes.fill
        }
    }
}

/**
 * Box plot
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
 * @param aesMapping Aesthetics mapping
 *
 */
class GGDotPlot(
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
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palettes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palettes.Categorical.auto,
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
    fill = fill,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.fill, aes.color)

    /** base box plot */
    override fun basePlot() = super.basePlot() + ggDot(
        alpha = this.alpha,
        color = this.color,
        fill = this.fill,
        aes = aes,
    ).getFeature(this)
}