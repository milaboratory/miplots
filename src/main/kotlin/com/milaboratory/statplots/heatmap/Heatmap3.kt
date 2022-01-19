package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.util.themeBlank
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toMap
import kotlin.math.max
import kotlin.math.min


/** */
class Heatmap3(
    _data: AnyFrame,
    val x: String,
    val y: String,
    val z: String,
    val xOrder: Order? = null,
    val yOrder: Order? = null,
) {

    internal val tileWidth = 0.95
    internal val tileHeight = 0.95

    val data: AnyFrame

    internal val xmap: Map<Any, Double>
    internal val ymap: Map<Any, Double>
    internal val xax: List<Any>
    internal val yax: List<Any>

    internal var xmaxBase: Double
    internal var ymaxBase: Double
    internal var xminBase: Double = 1.0 - tileWidth / 2
    internal var yminBase: Double = 1.0 - tileHeight / 2

    init {
        var data = _data

        xax = if (xOrder != null)
            data.rows()
                .asSequence()
                .distinctBy { it[x] }
                .sortedWith(xOrder)
                .mapNotNull { it[x] }
                .toList()
        else
            data[x].distinct().toList().filterNotNull()

        yax = if (yOrder != null)
            data.rows()
                .distinctBy { it[y] }
                .sortedWith(yOrder)
                .mapNotNull { it[y] }
                .toList()
        else
            data[y].distinct().toList().filterNotNull()

        xmap = xax.mapIndexed { i, xx -> xx to (1 + i).toDouble() }.toMap()
        ymap = yax.mapIndexed { i, yy -> yy to (1 + i).toDouble() }.toMap()
        xmaxBase = (xmap[xax.last()] ?: 0.0) + tileWidth / 2
        ymaxBase = (ymap[yax.last()] ?: 0.0) + tileHeight / 2

        data = data.add(HeatmapVar.xnum) { xmap[it[x]] }
        data = data.add(HeatmapVar.ynum) { ymap[it[y]] }

        this.data = data
    }


    val layers = mutableListOf<HLayer>()
    val xmin: Double get() = min(xminBase, layers.minOfOrNull { it.xmin } ?: xminBase)
    val xmax: Double get() = max(xmaxBase, layers.maxOfOrNull { it.xmax } ?: xminBase)
    val ymin: Double get() = min(yminBase, layers.minOfOrNull { it.ymin } ?: yminBase)
    val ymax: Double get() = max(ymaxBase, layers.maxOfOrNull { it.ymax } ?: yminBase)
    val width: Double get() = xmax - xmin
    val heigh: Double get() = ymax - ymin


    val plot
        get() = run {
            var plt = letsPlot(data.toMap()) {
                this.x = HeatmapVar.xnum
                this.y = HeatmapVar.ynum
                this.fill = z
            }

            plt += geomTile(width = tileWidth, height = tileHeight)
            plt += coordFixed()

            for (layer in layers) {
                plt += layer.feature
            }
            plt += themeBlank()

            plt
        }
}
