@file:Suppress("LocalVariableName")

package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.PlotWrapper
import com.milaboratory.miplots.clustering.HierarchicalClustering
import com.milaboratory.miplots.clustering.asTree
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.color.UniversalPalette
import com.milaboratory.miplots.dendro.Node
import com.milaboratory.miplots.dendro.leaves
import com.milaboratory.miplots.dendro.mapId
import com.milaboratory.miplots.themeBlank
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.letsPlot
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.Double.Companion.NaN
import kotlin.math.max
import kotlin.math.min

const val tileWidth = 1.0
const val tileHeight = 1.0
const val tileFillWidth = 0.95 * tileWidth
const val tileFillHeight = 0.95 * tileHeight
const val defBorderWidth = 0.05 * tileWidth
const val defTextSize = 1.5 * tileHeight
const val defSizeUnit = "x"
val defBorderColor = Color.BLACK

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
    val fillNoValue: Boolean = true,
    val noValue: Any? = null,
    val fillPalette: UniversalPalette = Palettes.Diverging.viridis2magma
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

        // add missing combinations with NA
        if (fillNoValue) {
            val existing = data.rows().toList().map { it[x] to it[y] }.toSet()
            val sampleRow = data.rows().first().toMap().mapValues { null }
            val toAppend = mutableListOf<Map<String, Any?>>()
            for (_x in xax) {
                for (_y in yax) {
                    if (!existing.contains(_x to _y))
                        toAppend += sampleRow + (x to _x) + (y to _y) + (z to noValue)
                }
            }
            data = data.concat(dataFrameOf(sampleRow.keys) { c -> toAppend.map { it[c] } })
        }

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
    val xminvis: Double get() = min(xminBase, layers.minOfOrNull { it.xminvis } ?: xminBase)
    val xmaxvis: Double get() = max(xmaxBase, layers.maxOfOrNull { it.xmaxvis } ?: xminBase)
    val yminvis: Double get() = min(yminBase, layers.minOfOrNull { it.yminvis } ?: yminBase)
    val ymaxvis: Double get() = max(ymaxBase, layers.maxOfOrNull { it.ymaxvis } ?: yminBase)

    private val zdatad = data[z].convertToDouble().distinct().toList().filterNotNull().filter { !it.isNaN() }
    val zmin = zdatad.minOrNull() ?: 0.0
    val zmax = zdatad.maxOrNull() ?: 0.0

    val features = mutableListOf<Feature>()

    private var debug = false
    fun debug() = run { debug = true; this }

    override var plot
        get() = run {
            var plt = letsPlot(data.toMap())

            plt += geomTile(
                width = tileFillWidth,
                height = tileFillHeight
            ) {
                this.x = HeatmapVar.xnum
                this.y = HeatmapVar.ynum
                this.fill = z
            }

            plt += fillPalette.scaleFillContinuous(midpoint = (zmin + zmax) / 2)

            for (layer in layers) {
                plt += layer.feature
                if (debug)
                    plt += borderLayer(
                        xmin = layer.xmin,
                        xmax = layer.xmax,
                        ymin = layer.ymin,
                        ymax = layer.ymax,
                        color = "red"
                    )
            }
            plt += themeBlank()

            plt += geomPoint(x = xminvis, y = yminvis, size = 0.0)
            plt += geomPoint(x = xmaxvis, y = ymaxvis, size = 0.0)

            for (feature in features) {
                plt += feature
            }

            plt += coordFixed(ratio = 1.0)

            plt
        }
        set(_) {
            throw IllegalStateException()
        }
}
