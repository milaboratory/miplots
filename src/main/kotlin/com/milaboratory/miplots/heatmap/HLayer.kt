package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.dendro.Node
import com.milaboratory.miplots.isLeftRight
import com.milaboratory.miplots.isTopBottom
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.intern.Feature

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
    val position: Position?,
    val xmin: Double, val xminadd: Double = 0.0,
    val xmax: Double, val xmaxadd: Double = 0.0,
    val ymin: Double, val yminadd: Double = 0.0,
    val ymax: Double, val ymaxadd: Double = 0.0,
    val feature: Feature,
    val colorMap: Map<Any?, Color>? = null,
    val title: String? = null
) {
    val xminvis = xmin - xminadd
    val xmaxvis = xmax + xmaxadd
    val yminvis = ymin - yminadd
    val ymaxvis = ymax + ymaxadd
}

internal enum class Ax { x, y }

internal val Ax.complement: Ax
    get() = when (this) {
        Ax.x -> Ax.y
        Ax.y -> Ax.x
    }

internal val Position.ax: Ax
    get() = when (this) {
        Position.Top, Position.Bottom -> Ax.x
        Position.Left, Position.Right -> Ax.y
    }

internal fun Heatmap.axCol(ax: Ax): String = when (ax) {
    Ax.x -> x
    Ax.y -> y
}

internal fun Heatmap.ax(ax: Ax): List<Any> = when (ax) {
    Ax.x -> xax
    Ax.y -> yax
}

internal fun Heatmap.axmap(ax: Ax): Map<Any, Double> = when (ax) {
    Ax.x -> xmap
    Ax.y -> ymap
}

internal fun Heatmap.axcoord(ax: Ax): List<Double> = when (ax) {
    Ax.x -> xcoord
    Ax.y -> ycoord
}

internal fun Heatmap.minmax(ax: Ax): Pair<Double, Double> = when (ax) {
    Ax.x -> xmin to xmax
    Ax.y -> ymin to ymax
}

internal fun Heatmap.minmaxBase(ax: Ax): Pair<Double, Double> = when (ax) {
    Ax.x -> xminBase to xmaxBase
    Ax.y -> yminBase to ymaxBase
}

internal fun Heatmap.clust(ax: Ax): Node<Any?>? = when (ax) {
    Ax.x -> xclust
    Ax.y -> yclust
}

internal data class LayerPosData(
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
) {
    val lxminmax = lxmin to lxmax
    val lyminmax = lymin to lymax
}

internal fun Heatmap.posData(
    pos: Position,
    height: Double,
    width: Double,
    sep: Double
) = run {
    val (lxmin, lxmax) =
        if (pos.isTopBottom)
            minmaxBase(Ax.x)
        else if (pos == Position.Left)
            xmin - sep - width to xmin - sep
        else
            xmax + sep to xmax + sep + width

    val (lymin, lymax) =
        if (pos.isLeftRight)
            minmaxBase(Ax.y)
        else if (pos == Position.Top)
            ymax + sep to ymax + sep + height
        else
            ymin - sep - height to ymin - sep

    val (lx, ly) = when (pos) {
        Position.Top -> Double.NaN to ymax + sep
        Position.Right -> xmax + sep to Double.NaN
        Position.Bottom -> Double.NaN to ymin - sep
        Position.Left -> xmin - sep to Double.NaN
    }

    val (hjust, vjust) = when (pos) {
        Position.Top -> 1.0 to 0.5
        Position.Right -> 1.0 to 0.5
        Position.Bottom -> 0.0 to 0.5
        Position.Left -> 0.0 to 0.5
    }

    val pax = pos.ax
    val ax = ax(pax)
    val coord = axcoord(pax)

    val (lxpos, lypos) = when (pos) {
        Position.Top, Position.Bottom -> coord to List(ax.size) { ly }
        Position.Left, Position.Right -> List(ax.size) { lx } to coord
    }

    LayerPosData(lx, ly, lxmin, lxmax, lymin, lymax, vjust, hjust, lxpos, lypos)
}
