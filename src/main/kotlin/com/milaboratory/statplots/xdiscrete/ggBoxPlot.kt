@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.kotlinx.dataframe.AnyFrame

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
    /** Additional mapping */
    aesMapping: ggBaseAes.() -> Unit = {}
) : ggBase(data, x, y, facetBy, facetNCol, facetNrow, color, fill, aesMapping) {
    /** base box plot */
    override var plot = run {
        var plt = super.plot
        plt += geomBoxplot(color = color, fill = fill) {
            this.color = aes.color
            this.fill = aes.fill
        }
        plt
    }
}
