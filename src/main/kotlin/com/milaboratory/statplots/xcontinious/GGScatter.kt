package com.milaboratory.statplots.xcontinious

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.PlotWrapper
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.toMap

enum class RegressionType {
    /** Linear Model*/
    Linear,

    /** Locally Estimated Scatterplot Smoothing */
    Loess
}


/**
 *
 */
class GGScatter(
    val data: AnyFrame,
    val x: String,
    val y: String,

    val color: String? = null,
    val fill: String? = null,
    val shape: Any? = null,
    val size: Number? = null,
    val alpha: Double? = null,
    val aesMapping: GGAes.() -> Unit = {}
) : PlotWrapper {

    val gtoupBy = color ?: shape
    val aes = GGAes().apply(aesMapping)

    override var plot = run {

        var plt = letsPlot(data.toMap()) {
            x = this@GGScatter.x
            y = this@GGScatter.y
        }

        plt += geomPoint(color = color, fill = fill, shape = shape, size = size, alpha = alpha) {
            color = aes.color
            fill = aes.color
            shape = aes.color
            size = aes.color
            color = aes.color
            alpha = aes.alpha
        }

        plt
    }
}
