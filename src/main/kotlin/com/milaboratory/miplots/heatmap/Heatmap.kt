package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.PlotWrapper
import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.clustering.HierarchicalClustering
import com.milaboratory.miplots.clustering.asTree
import com.milaboratory.miplots.dendro.Node
import com.milaboratory.miplots.dendro.geomDendro
import com.milaboratory.miplots.dendro.leaves
import com.milaboratory.miplots.dendro.mapId
import com.milaboratory.miplots.isTopBottom
import com.milaboratory.miplots.stat.util.themeBlank
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toMap
import kotlin.math.max
import kotlin.math.min

const val tileWidth = 1.0
const val tileHeight = 1.0
const val tileFillWidth = 0.95 * tileWidth
const val tileFillHeight = 0.95 * tileHeight


sealed class Order {
    companion object {
        fun groupBy(col: String): Order = WithComparator(Comparator.comparing { it[col].toString() })
        fun sortBy(col: String): Order = groupBy(col)
    }
}

data class WithComparator(val comparator: Comparator<DataRow<*>>) : Order()

enum class HierarchyType { X, Y }

class Hierarchical(val alt: Double = 0.0) : Order()

internal object HeatmapVar {
    const val xnum = "__xnum__"
    const val ynum = "__ynum__"
}

/** */
class Heatmap(
    _data: AnyFrame,
    val x: String,
    val y: String,
    val z: String,
    val xOrder: Order? = null,
    val yOrder: Order? = null,
) : PlotWrapper {
    companion object {
        private fun toDouble(v: Any?, alt: Double = Double.NaN): Double = run {
            if (v == null)
                alt
            else
                (v as? Double) ?: alt
        }

        private data class AxData(
            val ax: List<Any>,
            val clust: Node<Any?>?
        )

        private fun ax(
            data: AnyFrame, order: Order?,
            x: String, y: String, z: String
        ): AxData = run {
            var clust: Node<Any?>? = null

            val ax = when (order) {
                null -> data[x].distinct().toList().filterNotNull()

                is WithComparator -> data.rows()
                    .asSequence()
                    .distinctBy { it[x] }
                    .sortedWith(order.comparator)
                    .mapNotNull { it[x] }
                    .toList()

                is Hierarchical -> {
                    // x -> {y -> z}
                    val xmap = mutableMapOf<Any?, MutableMap<Any?, Double>>()
                    for (row in data) {
                        xmap
                            .computeIfAbsent(row[x]) { mutableMapOf() }
                            .computeIfAbsent(row[y]) { Companion.toDouble(row[z], order.alt) }
                    }

                    val yvals = data[y].distinct().toList()

                    // data for clustering

                    val forClustering = xmap.mapValues { yz ->
                        yvals.map { yz.value[it] ?: order.alt }.toDoubleArray()
                    }.toList()

                    clust = HierarchicalClustering.clusterize(
                        forClustering.map { it.second },
                        0.0,
                        HierarchicalClustering::EuclideanDistance
                    )
                        .asTree()
                        .mapId { if ((it ?: -1) < 0) null else forClustering[it!!].first }

                    // resulting xax
                    clust.leaves()
                }
            }

            AxData(ax, clust)
        }
    }

    val data: AnyFrame

    internal val xclust: Node<Any?>?
    internal val yclust: Node<Any?>?
    internal val xmap: Map<Any, Double>
    internal val ymap: Map<Any, Double>
    internal val xax: List<Any>
    internal val yax: List<Any>

    internal var xmaxBase: Double
    internal var ymaxBase: Double
    internal var xminBase: Double = tileWidth / 2
    internal var yminBase: Double = tileHeight / 2


    init {
        var data = _data
        val xdata = ax(data, xOrder, x, y, z)
        val ydata = ax(data, yOrder, y, x, z)
        xax = xdata.ax
        xclust = xdata.clust
        yax = ydata.ax
        yclust = ydata.clust

        xmap = xax.mapIndexed { i, xx -> xx to tileWidth * (1 + i) }.toMap()
        ymap = yax.mapIndexed { i, yy -> yy to tileHeight * (1 + i) }.toMap()
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

    override var plot
        get() = run {
            var plt = letsPlot(data.toMap()) {
                this.x = HeatmapVar.xnum
                this.y = HeatmapVar.ynum
                this.fill = z
            }

            plt += geomTile(
                width = tileFillWidth,
                height = tileFillHeight
            )

            plt += coordFixed()

            for (layer in layers) {
                plt += layer.feature
            }
            plt += themeBlank()

            plt += geomPoint(x = xmin, y = ymin, size = 0.0)
            plt += geomPoint(x = xmax, y = ymax, size = 0.0)

            plt
        }
        set(_) {
            throw IllegalStateException()
        }
}


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

internal fun Heatmap.withLabels(
    pos: Position,
    labels: List<String>? = null,
    angle: Number? = null,
    sep: Double = 0.0,
    width: Double? = null,
    height: Double? = null
) = run {
    val l_height = height ?: (tileHeight / 2)
    val l_width = width ?: (tileWidth / 2)
    val l_xpos: List<Any?>
    val l_ypos: List<Any?>
    val l_labs: List<Any?>
    val l_xmin: Double
    val l_xmax: Double
    val l_ymin: Double
    val l_ymax: Double
    val vjust: Double?
    val hjust: Double?

    when (pos) {
        Top -> {
            l_xmin = xminBase
            l_xmax = xmaxBase
            val l_y = ymax + sep// + tileHeight / 2
            l_ymin = l_y
            l_ymax = l_y + l_height
            l_xpos = xax.map { xmap[it] }
            l_ypos = List(xax.size) { l_y }
            l_labs = labels ?: xax
            vjust = 0.0
            hjust = 1.0
        }
        Bottom -> {
            l_xmin = xminBase
            l_xmax = xmaxBase
            val l_y = ymin - sep// - tileHeight / 2
            l_ymax = l_y
            l_ymin = l_ymax - l_height
            l_xpos = xax.map { xmap[it] }
            l_ypos = List(xax.size) { l_y }
            l_labs = labels ?: xax
            vjust = 1.0
            hjust = 0.0
        }
        Left -> {
            l_ymin = yminBase
            l_ymax = ymaxBase
            val l_x = xmin - sep //- tileWidth / 2
            l_xmax = l_x
            l_xmin = l_xmax - l_width
            l_ypos = yax.map { ymap[it] }
            l_xpos = List(yax.size) { l_x }
            l_labs = labels ?: yax
            hjust = 0.0
            vjust = 1.0
        }
        Right -> {
            l_ymin = yminBase
            l_ymax = ymaxBase
            val l_x = xmax + sep// + tileWidth / 2
            l_xmin = l_x
            l_xmax = l_xmin + l_width
            l_ypos = yax.map { ymap[it] }
            l_xpos = List(yax.size) { l_x }
            l_labs = labels ?: yax
            hjust = 1.0
            vjust = 0.0
        }
    }

    val layerData = mutableMapOf(
        "x" to l_xpos,
        "y" to l_ypos,
        "l" to l_labs
    )

    val feature = geomText(
        layerData,
        angle = angle,
        vjust = vjust,
        hjust = hjust
    ) {
        this.x = "x"
        this.y = "y"
        this.label = "l"
    }

    layers += HLayer(pos, l_xmin, l_xmax, l_ymin, l_ymax, feature)

    this
}

internal fun Heatmap.withLabel(
    x: Double,
    y: Double,
    label: String,
    pos: Position,
    width: Double? = null,
    height: Double? = null,
    sep: Double = 0.0,
    angle: Double = 0.0
) = run {
    val l_height = height ?: (tileHeight / 2)
    val l_width = width ?: (tileWidth / 2)
    val l_xmin: Double
    val l_xmax: Double
    val l_ymin: Double
    val l_ymax: Double
    val vjust: Double?
    val hjust: Double?

    when (pos) {
        Top -> {
            l_xmin = x - tileWidth / 2
            l_xmax = x + tileWidth / 2
            val l_y = y
            l_ymin = l_y
            l_ymax = l_y + l_height
            vjust = 0.0
            hjust = 1.0
        }
        Bottom -> {
            l_xmin = x - tileWidth / 2
            l_xmax = x + tileWidth / 2
            val l_y = y
            l_ymax = l_y
            l_ymin = l_ymax - l_height
            vjust = 1.0
            hjust = 0.0
        }
        Left -> {
            l_ymin = y - tileHeight / 2
            l_ymax = y + tileHeight / 2
            val l_x = x
            l_xmax = l_x
            l_xmin = l_xmax - l_width
            hjust = 0.0
            vjust = 1.0
        }
        Right -> {
            l_ymin = y - tileHeight / 2
            l_ymax = y + tileHeight / 2
            val l_x = x
            l_xmin = l_x
            l_xmax = l_xmin + l_width
            hjust = 1.0
            vjust = 0.0
        }
    }

    val feature = geomText(
        x = x,
        y = y,
        label = label,
        angle = angle,
        vjust = vjust,
        hjust = hjust
    )

    layers += HLayer(pos, l_xmin, l_xmax, l_ymin, l_ymax, feature)

    this
}

internal fun Heatmap.withColorKey(
    key: String,
    pos: Position = Bottom,
    sep: Double = 0.0,
    label: String? = null,
    labelPos: Position? = null,
    labelWidth: Double? = 0.0,
    labelHeight: Double? = 0.0,
) = run {

    val axCol: String
    val ax: List<Any>
    val l_x: Double
    val l_xmin: Double
    val l_xmax: Double
    val l_y: Double
    val l_ymin: Double
    val l_ymax: Double
    val l_lx: Double
    val l_ly: Double
    val l_pos = labelPos ?: if (pos.isTopBottom) Right else Top

    when (pos) {
        Top -> {
            axCol = x
            ax = xax
            l_x = Double.NaN
            l_xmin = xminBase
            l_xmax = xmaxBase
            l_y = ymax + sep + tileHeight / 2
            l_ymin = l_y - tileHeight / 2
            l_ymax = l_y + tileHeight / 2

            l_lx = if (l_pos == Left) xminBase else xmaxBase
            l_ly = l_y
        }
        Bottom -> {
            axCol = x
            ax = xax
            l_x = Double.NaN
            l_xmin = xminBase
            l_xmax = xmaxBase
            l_y = ymin - sep - tileHeight / 2
            l_ymin = l_y - tileHeight / 2
            l_ymax = l_y + tileHeight / 2

            l_lx = if (l_pos == Left) xminBase else xmaxBase
            l_ly = l_y
        }
        Left -> {
            axCol = y
            ax = yax
            l_y = Double.NaN
            l_ymin = yminBase
            l_ymax = ymaxBase
            l_x = xmin - sep - tileWidth / 2
            l_xmin = l_x - tileWidth / 2
            l_xmax = l_x + tileWidth / 2

            l_ly = if (l_pos == Top) ymaxBase else yminBase
            l_lx = l_x
        }
        Right -> {
            axCol = y
            ax = yax
            l_y = Double.NaN
            l_ymin = yminBase
            l_ymax = ymaxBase
            l_x = xmax + sep + tileWidth / 2
            l_xmin = l_x - tileWidth / 2
            l_xmax = l_x + tileWidth / 2

            l_ly = if (l_pos == Top) ymaxBase else yminBase
            l_lx = l_x
        }
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
                width = tileFillWidth,
                height = tileFillHeight
            )
            else -> geomTile(
                x = l_x,
                y = ymap[el],
                fill = colorMap[ck[el]],
                width = tileFillWidth,
                height = tileFillHeight
            )
        }
        if (feature == null)
            feature = f
        else
            feature += f
    }

    labelPos?.apply {
        this@withColorKey.withLabel(
            l_lx,
            l_ly,
            label ?: key,
            l_pos,
            sep = sep,
            width = labelWidth,
            height = labelHeight,
            angle = if (pos.isTopBottom) 0.0 else 90.0,
        )
    }

    layers += HLayer(pos, l_xmin, l_xmax, l_ymin, l_ymax, feature!!)

    this
}

internal fun Heatmap.withDendrogram(pos: Position) = run {
    if ((pos == Left || pos == Right) && yclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")
    if ((pos == Top || pos == Bottom) && xclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")

    val clust: Node<Any?>
    val l_height: Double
    val l_xmin: Double
    val l_xmax: Double
    val l_ymin: Double
    val l_ymax: Double
    val ax: List<Any>
    val axmap: Map<Any, Double>
    when (pos) {
        Top -> {
            clust = xclust!!
            l_height = heigh / 5
            l_xmin = xminBase
            l_xmax = xmaxBase
            l_ymin = ymax
            l_ymax = l_ymin + heigh
            ax = xax
            axmap = xmap
        }
        else -> TODO()
    }

    val feature = geomDendro(
        clust,
        rpos = pos,
        coord = ax.map { axmap[it]!! },
        height = l_height,
        color = "black",
        linetype = 3,
        fill = "white"
    )

    layers += HLayer(pos, l_xmin, l_xmax, l_ymin, l_ymax, feature.feature)

    this
}

