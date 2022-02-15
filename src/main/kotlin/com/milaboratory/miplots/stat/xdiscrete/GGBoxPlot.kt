@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.kotlinx.dataframe.AnyFrame

/**
 * Add box plot geometry & stat to the plot
 *
 * @param color Outline color
 * @param fill Fill color
 * @param aesMapping additional aes mapping
 */
class ggBox(
    /** Outline color */
    color: String? = null,
    /** Fill color */
    fill: String? = null,
    /** Additional mapping */
    aes: GGAes,
    val outlierColor: Any? = null,
    val outlierFill: Any? = null,
    val outlierShape: Any? = 8,
    val outlierSize: Number? = null,
) : WithAes(color = color, fill = fill, aes = aes), GGXDiscreteFeature {
    constructor(
        color: String? = null,
        fill: String? = null,
        aesMapping: GGAes.() -> Unit = {}
    ) : this(color, fill, GGAes().apply(aesMapping))

    override val prepend = true
    override fun getFeature(base: GGXDiscrete) =
        geomBoxplot(
            color = this.color,
            fill = this.fill,
            outlierColor = outlierColor,
            outlierFill = outlierFill,
            outlierShape = outlierShape,
            outlierSize = outlierSize,
        ) {
            this.color = aes.color
            this.fill = aes.fill
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
 * */
class GGBoxPlot(
    data: AnyFrame, x: String, y: String,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = "#000000",
    fill: String? = null,
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palletes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palletes.Categorical.auto,
    val outlierColor: Any? = null,
    val outlierFill: Any? = null,
    val outlierShape: Any? = 8,
    val outlierSize: Number? = null,
    val aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    color = color,
    fill = fill,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {
    override val groupBy = filterGroupBy(aes.fill, aes.color)

    /** base box plot */
    override fun basePlot() = super.basePlot() + ggBox(
        color = this.color,
        fill = this.fill,
        aes = aes,
        outlierColor = outlierColor,
        outlierFill = outlierFill,
        outlierShape = outlierShape,
        outlierSize = outlierSize,
    ).getFeature(this)
}
