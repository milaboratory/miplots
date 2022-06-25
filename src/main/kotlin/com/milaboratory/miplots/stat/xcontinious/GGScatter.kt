package com.milaboratory.miplots.stat.xcontinious

import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.stat.GGAes
import com.milaboratory.miplots.stat.GGBase
import com.milaboratory.miplots.Orientation
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.convertToDouble
import org.jetbrains.kotlinx.dataframe.api.max
import org.jetbrains.kotlinx.dataframe.api.min
import org.jetbrains.kotlinx.dataframe.api.toMap

/**
 *
 */
class GGScatter(
    _data: AnyFrame,
    x: String,
    y: String,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = "black",
    fill: String? = null,
    shape: Any? = null,
    size: Number? = null,
    alpha: Double? = null,
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palettes.Categorical.auto,
    fillScale: DiscreteColorMapping = Palettes.Categorical.auto,
    aesMapping: GGAes.() -> Unit = {}
) : GGBase(
    x = x,
    y = y,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    color = color,
    fill = fill,
    alpha = alpha,
    size = size,
    shape = shape,
    width = null,
    linetype = null,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {

    override val data = _data
    override val groupBy = filterGroupBy(aes.color, aes.shape)
    internal val xData = data[x].convertToDouble()
    internal val xMinMax = xData.min() to xData.max()
    internal val yData = data[y].convertToDouble()
    internal val yMinMax = yData.min() to yData.max()

    override var plot = run {
        var plt = letsPlot(data.toMap()) {
            this.x = this@GGScatter.x
            this.y = this@GGScatter.y
        }

        plt += geomPoint(
            color = this.color,
            fill = this.fill,
            shape = this.shape,
            size = this.size,
            alpha = this.alpha
        ) {
            this.color = aes.color
            this.fill = aes.fill
            this.shape = aes.shape
            this.size = aes.size
            this.alpha = aes.alpha
        }

        if (aes.color != null)
            plt += colorScale.colorScale(data[aes.color!!].distinct().toList())

        if (aes.fill != null)
            plt += fillScale.fillScale(data[aes.fill!!].distinct().toList())

        if (facetBy != null)
            plt += facetWrap(facets = facetBy, ncol = facetNCol, nrow = facetNRow)

        plt
    }
}
