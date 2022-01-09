@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.WithFeature
import jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.kotlinx.dataframe.AnyFrame

class ggBoxPlotFeature(
    /** Outline color */
    val color: String? = null,
    /** Fill color */
    val fill: String? = null,
    /** Additional mapping */
    aesMapping: ggBaseAes.() -> Unit = {}
) : WithFeature {
    internal val aes = ggBaseAes().apply(aesMapping)
    override fun getFeature(base: ggBase) =
        geomBoxplot(color = color, fill = fill) {
            this.color = aes.color
            this.fill = aes.fill
        }
}

/** Box plot */
class ggBoxPlot(
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
    aesMapping: ggBaseAes.() -> Unit = {}
) : ggBase(data, x, y, facetBy, facetNCol, facetNrow, color, fill, orientation, aesMapping) {

    override val groupBy: String? = distinctGroupBy(aes.fill ?: aes.color)

    /** base box plot */
    override var plot = super.plot + ggBoxPlotFeature(color, fill, aesMapping).getFeature(this)
}
