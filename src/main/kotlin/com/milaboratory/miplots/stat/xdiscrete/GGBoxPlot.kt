@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.GGAes
import jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.kotlinx.dataframe.AnyFrame

class ggBox(
    /** Outline color */
    val color: String? = null,
    /** Fill color */
    val fill: String? = null,
    /** Additional mapping */
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscreteFeature {
    internal val aes = GGAes().apply(aesMapping)
    override fun getFeature(base: GGXDiscrete) =
        geomBoxplot(color = color, fill = fill) {
            this.color = aes.color
            this.fill = aes.fill
        }
}

/** Box plot */
class GGBoxPlot(
    data: AnyFrame, x: String, y: String,
    /** Organize data in facets */
    facetBy: String? = null,
    /** Number of columns in facet view */
    facetNCol: Int? = null,
    /** Number of rows in facet view */
    facetNrow: Int? = null,
    /** Outline color */
    color: String? = null,
    /** Fill color */
    fill: String? = null,
    /** Orientation */
    orientation: Orientation = Orientation.Vertical,
    /** Additional mapping */
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy = filterGroupBy(aes.fill, aes.color)

    /** base box plot */
    override var plot = super.plot + ggBox(color, fill, aesMapping).getFeature(this)
}
