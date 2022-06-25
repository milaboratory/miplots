@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.WithAes
import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.intern.layer.PosOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame

/**
 * Add box plot geometry & stat to the plot
 *
 * @param color Outline color
 * @param fill Fill color
 * @param aesMapping additional aes mapping
 */
class ggBox(
    color: String? = null,
    fill: String? = null,
    alpha: Double? = null,
    width: Double? = null,
    aes: GGAes,
    val position: PosOptions = Pos.dodge,
    val outlierColor: Any? = null,
    val outlierFill: Any? = null,
    val outlierShape: Any? = 8,
    val outlierSize: Number? = null,
) : WithAes(color = color, fill = fill, width = width, aes = aes), GGXDiscreteFeature {
    constructor(
        color: String? = null,
        fill: String? = null,
        alpha: Double? = null,
        width: Double? = null,
        position: PosOptions = Pos.dodge,
        outlierShape: Any? = 8,
        outlierColor: Any? = null,
        outlierFill: Any? = null,
        outlierSize: Number? = null,
        aesMapping: GGAes.() -> Unit = {}
    ) : this(
        color, fill, alpha, width, GGAes().apply(aesMapping),
        position = position,
        outlierShape = outlierShape,
        outlierColor = outlierColor,
        outlierFill = outlierFill,
        outlierSize = outlierSize,
    )

    override val prepend = true
    override fun getFeature(base: GGXDiscrete) = run {
        this.inheritColors(base)
        geomBoxplot(
            color = this.color,
            fill = this.fill,
            alpha = this.alpha,
            width = this.width,
            position = position,
            outlierColor = outlierColor,
            outlierFill = outlierFill,
            outlierShape = outlierShape,
            outlierSize = outlierSize,
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
 * */
class GGBoxPlot(
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
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palettes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palettes.Categorical.auto,
    val outlierColor: Any? = null,
    val outlierFill: Any? = null,
    val outlierShape: Any? = 8,
    val outlierSize: Number? = null,
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
    width = width,
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
        width = this.width,
        alpha = this.alpha,
        aes = aes,
        outlierColor = outlierColor,
        outlierFill = outlierFill,
        outlierShape = outlierShape,
        outlierSize = outlierSize,
    ).getFeature(this)
}
