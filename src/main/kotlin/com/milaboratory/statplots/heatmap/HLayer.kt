package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.heatmap.Position.Bottom
import com.milaboratory.statplots.heatmap.Position.Top
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import org.jetbrains.kotlinx.dataframe.api.rows
import kotlin.Double.Companion.NaN

enum class Position { Top, Right, Bottom, Left }

/**
 *
 */
data class HLayer(
    val position: Position,
    val xmin: Double, val xmax: Double,
    val ymin: Double, val ymax: Double,
    val feature: Feature
) {
    val width = xmax - xmin
    val height = ymax - ymin
}


//
//private fun yTextLayer() = run {
//    val layerData = mutableMapOf(
//        "x" to List(yax.size) { xmaxBase + 1.1 * tileWidth / 2 },
//        "y" to yax.map { ymap[it] },
//        "l" to yax
//    )
//
//    geomText(layerData) {
//        x = "x"
//        y = "y"
//        label = "l"
//    }
//}
//

internal fun Heatmap3.xLabelsLayer(
    pos: Position = Bottom,
    angle: Number? = null
) = run {
    val height = tileHeight * 3
    val l_ymin: Double
    val l_ymax: Double
    val l_y: Double
    val vjust: Double
    when (pos) {
        Bottom -> {
            l_y = ymin - 1.1 * tileHeight / 2
            l_ymax = l_y
            l_ymin = l_ymax - height
            vjust = 0.0

        }
        Top -> {
            l_y = ymax + 1.1 * tileHeight / 2
            l_ymin = l_y
            l_ymax = l_y + height
            vjust = 1.0
        }
        else -> throw IllegalArgumentException()
    }

    val layerData = mutableMapOf(
        "x" to xax.map { xmap[it] },
        "y" to List(xax.size) { l_y },
        "l" to xax
    )

    val feature = geomText(
        layerData,
        angle = angle,
        vjust = vjust,
        hjust = 0
    ) {
        this.x = "x"
        this.y = "y"
        this.label = "l"
    }

    layers += HLayer(pos, xmin, xmax, l_ymin, l_ymax, feature)
}

internal fun Heatmap3.colorKey(
    key: String,
    pos: Position = Bottom
) = run {
    val axCol: String
    val ax: List<Any>
    val l_x: Double
    val l_xmin: Double
    val l_xmax: Double
    val l_y: Double
    val l_ymin: Double
    val l_ymax: Double

    when (pos) {
        Top -> {
            axCol = x
            ax = xax
            l_x = NaN
            l_xmin = xmin
            l_xmax = xmax
            l_y = ymax + 1.1 * tileHeight / 2
            l_ymin = l_y - tileHeight / 2
            l_ymax = l_y + tileHeight / 2
        }
        Bottom -> {
            axCol = x
            ax = xax
            l_x = NaN
            l_xmin = xmin
            l_xmax = xmax
            l_y = ymin - 1.1 * tileHeight / 2
            l_ymin = l_y - tileHeight / 2
            l_ymax = l_y + tileHeight / 2
        }
        else -> throw RuntimeException()
    }

    val ck = data.rows().map { it[axCol] to it[key] }.distinct().toMap()
    val colorMap = data[key].distinct().toList().mapIndexed { i, e ->
        e to when (i % 3) {
            0 -> "red"
            1 -> "green"
            2 -> "blue"
            else -> "black"
        }
    }.toMap()

    var feature: Feature? = null
    for (i in ax.indices) {
        val el = ax[i]
        val f = when (pos) {
            Top, Bottom -> geomTile(
                x = xmap[el],
                y = l_y,
                fill = colorMap[ck[el]],
                width = tileWidth,
                height = tileHeight
            )
            else -> throw RuntimeException()
        }
        if (feature == null)
            feature = f
        else
            feature += f
    }


    layers += HLayer(pos, l_xmin, l_xmax, l_ymin, l_ymax, feature!!)
}


