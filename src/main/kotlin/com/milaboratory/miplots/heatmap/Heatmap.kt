@file:Suppress("LocalVariableName")

package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.*
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.clustering.HierarchicalClustering
import com.milaboratory.miplots.clustering.asTree
import com.milaboratory.miplots.dendro.Node
import com.milaboratory.miplots.dendro.geomDendro
import com.milaboratory.miplots.dendro.leaves
import com.milaboratory.miplots.dendro.mapId
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
import kotlin.Double.Companion.NaN
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
        private fun toDouble(v: Any?, alt: Double = NaN): Double = run {
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
                            .computeIfAbsent(row[y]) { toDouble(row[z], order.alt) }
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
                    clust.leaves().map { it.id!! }
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

    val xcoord = xax.map { xmap[it]!! }
    val ycoord = yax.map { ymap[it]!! }
    val xmin: Double get() = min(xminBase, layers.minOfOrNull { it.xmin } ?: xminBase)
    val xmax: Double get() = max(xmaxBase, layers.maxOfOrNull { it.xmax } ?: xminBase)
    val ymin: Double get() = min(yminBase, layers.minOfOrNull { it.ymin } ?: yminBase)
    val ymax: Double get() = max(ymaxBase, layers.maxOfOrNull { it.ymax } ?: yminBase)
    val width: Double get() = xmax - xmin
    val heigh: Double get() = ymax - ymin

    val features = mutableListOf<Feature>()

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

            for (feature in features) {
                plt += feature
            }

            plt
        }
        set(_) {
            throw IllegalStateException()
        }
}

operator fun Heatmap.plusAssign(f: Feature) {
    features += f
}

operator fun Heatmap.plus(f: Feature) = run {
    features += f
    this
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

private enum class Ax { x, y }

private val Ax.complement: Ax
    get() = when (this) {
        Ax.x -> Ax.y
        Ax.y -> Ax.x
    }

private val Position.ax: Ax
    get() = when (this) {
        Top, Bottom -> Ax.x
        Left, Right -> Ax.y
    }

private fun Heatmap.axCol(ax: Ax): String = when (ax) {
    Ax.x -> x
    Ax.y -> y
}

private fun Heatmap.ax(ax: Ax): List<Any> = when (ax) {
    Ax.x -> xax
    Ax.y -> yax
}

private fun Heatmap.axmap(ax: Ax): Map<Any, Double> = when (ax) {
    Ax.x -> xmap
    Ax.y -> ymap
}

private fun Heatmap.axcoord(ax: Ax): List<Double> = when (ax) {
    Ax.x -> xcoord
    Ax.y -> ycoord
}

private fun Heatmap.minmax(ax: Ax): Pair<Double, Double> = when (ax) {
    Ax.x -> xmin to xmax
    Ax.y -> ymin to ymax
}

private fun Heatmap.minmaxBase(ax: Ax): Pair<Double, Double> = when (ax) {
    Ax.x -> xminBase to xmaxBase
    Ax.y -> yminBase to ymaxBase
}

private fun Heatmap.clust(ax: Ax): Node<Any?>? = when (ax) {
    Ax.x -> xclust
    Ax.y -> yclust
}

private data class LayerPosData(
    val lx: Double,
    val ly: Double,
    val lxmin: Double,
    val lxmax: Double,
    val lymin: Double,
    val lymax: Double,
    val vjust: Double?,
    val hjust: Double?,
    val lxpos: List<Double>,
    val lypos: List<Double>,
)

private fun Heatmap.posData(
    pos: Position,
    height: Double,
    width: Double,
    sep: Double
) = run {
    val (lxmin, lxmax) =
        if (pos.isTopBottom)
            minmaxBase(Ax.x)
        else if (pos == Left)
            xmin - sep - width to xmin - sep
        else
            xmax + sep to xmax + sep + width

    val (lymin, lymax) =
        if (pos.isLeftRight)
            minmaxBase(Ax.y)
        else if (pos == Top)
            ymax + sep to ymax + sep + height
        else
            ymin - sep - height to ymin - sep

    val (lx, ly) = when (pos) {
        Top -> NaN to ymax + sep
        Right -> xmax + sep to NaN
        Bottom -> NaN to ymin - sep
        Left -> xmin - sep to NaN
    }

    val (hjust, vjust) = when (pos) {
        Top -> 1.0 to 0.5
        Right -> 1.0 to 0.5
        Bottom -> 0.0 to 0.5
        Left -> 0.0 to 0.5
    }

    val pax = pos.ax
    val ax = ax(pax)
    val coord = axcoord(pax)

    val (lxpos, lypos) = when (pos) {
        Top, Bottom -> coord to List(ax.size) { ly }
        Left, Right -> List(ax.size) { lx } to coord
    }

    LayerPosData(lx, ly, lxmin, lxmax, lymin, lymax, vjust, hjust, lxpos, lypos)
}

internal fun Heatmap.withLabels(
    pos: Position,
    labels: List<String>? = null,
    angle: Number? = null,
    sep: Double = 0.0,
    width: Double? = null,
    height: Double? = null
) = run {
    val pdata = posData(pos, height ?: (tileHeight / 2), width ?: (tileWidth / 2), sep)

    val layerData = mutableMapOf(
        "x" to pdata.lxpos,
        "y" to pdata.lypos,
        "l" to (labels ?: ax(pos.ax))
    )

    val feature = geomText(
        layerData,
        angle = angle,
        vjust = pdata.vjust,
        hjust = pdata.hjust
    ) {
        this.x = "x"
        this.y = "y"
        this.label = "l"
    }

    layers += HLayer(pos, pdata.lxmin, pdata.lxmax, pdata.lymin, pdata.lymax, feature)

    this
}

internal fun Heatmap.withColorKey(
    key: String,
    pos: Position = Bottom,
    sep: Double = 0.0,
    label: String? = null,
    labelPos: Position? = null,
    labelSep: Double = 0.0,
    labelWidth: Double? = 0.0,
    labelHeight: Double? = 0.0,
) = run {
    val pdata = posData(pos, tileHeight, tileWidth, sep)

    val ck = data.rows().map { it[axCol(pos.ax)] to it[key] }.distinct().toMap()
    val colorMap = data[key].distinct().toList().mapIndexed { i, e ->
        e to when (i % 3) {
            0 -> "red"
            1 -> "green"
            2 -> "blue"
            else -> "black"
        }
    }.toMap()

    var feature: Feature? = null
    val ax = ax(pos.ax)

    val (xadj, yadj) = when (pos) {
        Top -> 0.0 to tileHeight / 2
        Bottom -> 0.0 to -tileHeight / 2
        Left -> -tileWidth / 2 to 0.0
        Right -> tileWidth / 2 to 0.0
    }
    for (i in ax.indices) {
        val el = ax[i]
        val f = geomTile(
            x = pdata.lxpos[i] + xadj,
            y = pdata.lypos[i] + yadj,
            fill = colorMap[ck[el]],
            width = tileFillWidth,
            height = tileFillHeight
        )

        if (feature == null)
            feature = f
        else
            feature += f
    }

    labelPos?.apply {
        val (llx, lly) = when (pos) {
            Top, Bottom -> (if (labelPos == Left) xminBase - labelSep else xmaxBase + labelSep) to pdata.ly
            Left, Right -> pdata.lx to (if (labelPos == Top) ymaxBase + labelSep else yminBase - labelSep)
        }

        val (hjust, vjust) = when (labelPos) {
            Top -> 1.0 to 0.5
            Right -> 1.0 to 0.5
            Bottom -> 0.0 to 0.5
            Left -> 0.0 to 0.5
        }

        features.add(
            geomText(
                x = llx + xadj,
                y = lly + yadj,
                label = label ?: key,
                angle = if (pos.isTopBottom) 0.0 else 90.0,
                vjust = vjust,
                hjust = hjust
            )
        )
    }

    layers += HLayer(pos, pdata.lxmin, pdata.lxmax, pdata.lymin, pdata.lymax, feature!!)

    this
}

internal fun Heatmap.withDendrogram(
    pos: Position,
    sep: Double = 0.0
) = run {
    if ((pos == Left || pos == Right) && yclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")
    if ((pos == Top || pos == Bottom) && xclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")

    val h = heigh / 5
    val w = width / 5
    val pdata = posData(pos, h, w, sep)
    val clust = clust(pos.ax)!!
    val dheight = if (pos.isTopBottom) h else w
    val rshift = when (pos) {
        Top -> ymax + dheight
        Right -> xmax + dheight
        Bottom -> ymin - dheight
        Left -> xmin - dheight
    }

    val feature = geomDendro(
        clust,
        rpos = pos,
        points = false,
        balanced = true,
        rshift = rshift,
        coord = axcoord(pos.ax),
        height = dheight,
        color = "black",
        linetype = 1,
        fill = "black",
    )

    layers += HLayer(pos, pdata.lxmin, pdata.lxmax, pdata.lymin, pdata.lymax, feature.feature)

    this
}

