package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.GGBase
import com.milaboratory.statplots.util.themeBlank
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toMap


/** */
class Heatmap2(
    _data: AnyFrame,
    x: String,
    y: String,
    val z: String,
    val xOrder: Order? = null,
    val yOrder: Order? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    fill: String? = null,
    val shape: Any? = null,
    val size: Number? = null,
    val alpha: Double? = null,
    aesMapping: GGAes.() -> Unit = {}
) : GGBase(
    x = x,
    y = y,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNrow = facetNrow,
    color = color,
    fill = fill,
    aesMapping = aesMapping
) {

    private val tileWidth = 1.0
    private val tileHeight = 1.0

    override val groupBy = null
    override val data: AnyFrame

    internal val xmap: Map<Any, Double>
    internal val ymap: Map<Any, Double>
    internal val xax: List<Any>
    internal val yax: List<Any>

    internal var xmax: Double
    internal var ymax: Double
    internal var xmin: Double = 1.0
    internal var ymin: Double = 1.0

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
        xmax = xmap[xax.last()] ?: 0.0
        ymax = ymap[yax.last()] ?: 0.0

        data = data.add(HeatmapVar.xnum) { xmap[it[x]] }
        data = data.add(HeatmapVar.ynum) { ymap[it[y]] }

        this.data = data
    }

    private fun yTextLayer() = run {
        val layerData = mutableMapOf(
            "x" to List(yax.size) { xmax + 1.1 * tileWidth / 2 },
            "y" to yax.map { ymap[it] },
            "l" to yax
        )

        geomText(layerData) {
            x = "x"
            y = "y"
            label = "l"
        }
    }

    private fun xTextLayer() = run {
        val layerData = mutableMapOf(
            "x" to xax.map { xmap[it] },
            "y" to List(xax.size) { ymin - 1.1 * tileWidth / 2 },
            "l" to xax
        )

        geomText(
            layerData,
            angle = 45,
            vjust = 0,
            hjust = 0
        ) {
            x = "x"
            y = "y"
            label = "l"
        }
    }


    var axisTextX: Boolean? = null

    override var plot = run {
        var plt = letsPlot(data.toMap()) {
            this.x = HeatmapVar.xnum
            this.y = HeatmapVar.ynum
            this.fill = z
        }

        plt += geomTile(width = 0.9, height = 0.9)
        plt += coordFixed()

        if (axisTextX == true) {

        }
//        plt += scaleXDiscrete(
//            breaks = xax, limits = xax,
//            labels = xax.map { it.toString() },
//            expand = listOf(0.0, 0.1),
//        )
//        plt += scaleYDiscrete(
//            breaks = yax, limits = yax,
//            labels = yax.map { it.toString() },
//            expand = listOf(0.0, 0.1)
//        )

//        plt += xlab("") + ylab("")

        plt += yTextLayer()
        plt += xTextLayer()

        plt += themeBlank()

        plt
    }
}

internal object HeatmapVar {
    val xnum = "xnum"
    val ynum = "ynum"
}
