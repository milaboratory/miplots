package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.formatPValue
import jetbrains.letsPlot.geom.geomSegment
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import org.jetbrains.kotlinx.dataframe.api.convertToDouble
import org.jetbrains.kotlinx.dataframe.api.maxOrNull
import org.jetbrains.kotlinx.dataframe.api.minOrNull

fun Heatmap.withFillLegend(
    pos: Position,
    title: String? = null,
    sep: Double = 1.5,
    tsep: Double = 0.1,
    bwt: Double = defBorderWidth,
    nTiles: Int = 25,
    nLabels: Int = 5,
    textSize: Number? = defTextSize,
    sizeUnit: String = defSizeUnit,
    color: Any? = defBorderColor
) = run {

    val pdata = posData(pos, 2 * (tsep + tileFillHeight), 2 * (tsep + tileFillWidth), sep)

    val zd = data[z].convertToDouble()
    val zmin = zd.minOrNull() ?: 0.0
    val zmax = zd.maxOrNull() ?: 0.0

    var feature: Feature = FeatureList(emptyList())

    val (xtitle, ytitle) = when (pos) {
        Top -> xmaxBase to ymax + tsep + sep + tileFillHeight + tsep
        Right -> xmax + sep to ymaxBase
        Bottom -> xminBase to ymin - sep
        Left -> xmin - sep - tsep - tileFillWidth - tsep to yminBase
    }

    val (hjust, vjust) = when (pos) {
        Top -> "right" to "bottom"
        Bottom -> "left" to "bottom"
        Right -> "right" to "bottom"
        Left -> "left" to "bottom"
    }

    val angle = when (pos) {
        Top, Bottom -> 0
        Left, Right -> 90
    }

    // title
    feature += geomText(
        x = xtitle,
        y = ytitle,
        label = title ?: this.z,
        hjust = hjust,
        vjust = vjust,
        angle = angle,
        size = textSize,
        sizeUnit = sizeUnit
    )

    // tiles
    val axMinmax = minmaxBase(pos.ax)
    val tw = (axMinmax.second - axMinmax.first) / nTiles
    val zw = (zmax - zmin) / nTiles

    val zt = (0 until nTiles).map { i -> zmin + i * zw }
    val tax1 = (0 until nTiles).map { i -> axMinmax.first + tw / 2 + i * tw }
    val tax2 = when (pos) {
        Top -> ymax + sep + tsep + tileFillHeight / 2
        Right -> xmax + tsep + sep + tileFillWidth / 2
        Bottom -> ymin - tsep - sep - tileFillHeight / 2
        Left -> xmin - sep - tsep - tileFillWidth / 2
    }.run {
        List(tax1.size) { this }
    }

    val (xtiles, ytiles) = when (pos.ax) {
        Ax.x -> tax1 to tax2
        Ax.y -> tax2 to tax1
    }

    val (width, height) = when (pos) {
        Top, Bottom -> 1.0 to tileFillHeight
        Left, Right -> tileFillWidth to 1.0
    }

    feature += geomTile(
        mapOf(
            "x" to xtiles,
            "y" to ytiles,
            "z" to zt
        ),
        height = height,
        width = width,
    ) {
        this.x = "x"
        this.y = "y"
        this.fill = "z"
    }

    val (bxmin, bxmax) = when (pos) {
        Top, Bottom -> xminBase to xmaxBase
        Left -> xmin - sep - tsep - tileFillWidth to xmin - sep - tsep
        Right -> xmax + sep + tsep to xmax + sep + tsep + tileFillWidth
    }

    val (bymin, bymax) = when (pos) {
        Left, Right -> yminBase to ymaxBase
        Top -> ymax + sep + tsep to ymax + sep + tsep + tileFillHeight
        Bottom -> ymin - sep - tsep to ymin - sep - tsep - tileFillHeight
    }

    feature += borderLayer(
        width = bwt,
        xmin = bxmin, xmax = bxmax,
        ymin = bymin, ymax = bymax
    )

    // labels
    val nl = nLabels - 1
    val xlw = (axMinmax.second - axMinmax.first) / nl
    val zlw = (zmax - zmin) / nl

    val lax1 = (0..nl).map { i -> axMinmax.first + i * xlw }
    val lax2 = when (pos) {
        Top -> ymax + sep
        Right -> xmax + tsep + sep + tileFillWidth + tsep
        Bottom -> ymin - tsep - sep - tileFillHeight - tsep
        Left -> xmin - sep
    }.run {
        List(lax1.size) { this }
    }

    val ll = (0..nl).map { i -> formatPValue(zmin + i * zlw) }

    val (xlabs, ylabs) = when (pos.ax) {
        Ax.x -> lax1 to lax2
        Ax.y -> lax2 to lax1
    }

    val (lhjust, lvjust) = when (pos) {
        Top -> "center" to "top"
        Bottom -> "center" to "top"
        Right -> "left" to "center"
        Left -> "left" to "center"
    }

    feature += geomText(
        data = mapOf(
            "x" to xlabs,
            "y" to ylabs,
            "l" to ll
        ),
        size = textSize,
        sizeUnit = sizeUnit,
        hjust = lhjust,
        vjust = lvjust,
    ) {
        x = "x"
        y = "y"
        label = "l"
    }

    // ticks
    val tx = lax1.dropLast(1).drop(1)
    val max = when (pos) {
        Top -> ymax + sep + tsep
        Right -> xmax + sep + tsep + tileFillWidth
        Bottom -> ymin - sep - tsep - tileFillHeight
        Left -> xmin - sep - tsep
    }
    val frac = 5
    val (tstart, tend) = when (pos) {
        Top -> max to max + tileFillHeight / frac
        Right -> max - tileFillWidth / frac to max
        Bottom -> max + tileFillHeight / frac to max
        Left -> max to max - tileFillWidth / frac
    }.run {
        List(tx.size) { this.first } to List(tx.size) { this.second }
    }

    val (txstart, txend) = when (pos) {
        Top, Bottom -> tx to tx
        Left, Right -> tstart to tend
    }

    val (tystart, tyend) = when (pos) {
        Top, Bottom -> tstart to tend
        Left, Right -> tx to tx
    }

    feature += geomSegment(
        data = mapOf(
            "xs" to txstart,
            "xe" to txend,
            "ys" to tystart,
            "ye" to tyend,
        ),
        color = color
    ) {
        x = "xs"
        xend = "xe"
        y = "ys"
        yend = "ye"
    }

    layers += HLayer(
        position = pos,
        xmin = pdata.lxmin, xmax = pdata.lxmax,
        ymin = pdata.lymin, ymax = pdata.lymax,
        feature = feature
    )

    this
}
