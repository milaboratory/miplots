package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.formatPValue
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.geom.geomSegment
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import org.jetbrains.kotlinx.dataframe.api.convertToDouble
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.maxOrNull
import org.jetbrains.kotlinx.dataframe.api.minOrNull
import kotlin.math.max

fun Heatmap.withFillLegend(
    pos: Position,
    title: String? = null,
    size: Double = 1.0,
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

    val zd = data[z].convertToDouble().filter { it != null && it.isFinite() }
    val zmin = zd.minOrNull() ?: 0.0
    val zmax = zd.maxOrNull() ?: 0.0

    var feature: Feature = FeatureList(emptyList())

    val (xtitle, ytitle) = when (pos) {
        Top -> xmaxBase to ymax + tsep + sep + tileFillHeight + tsep
        Right -> xmax + sep to ymaxBase
        Bottom -> xminBase to ymin - sep
        Left -> xmin - sep - tsep - tileFillWidth - tsep to ymaxBase
    }

    val (hjust, vjust) = when (pos) {
        Top -> "right" to "bottom"
        Bottom -> "left" to "bottom"
        Right -> "right" to "bottom"
        Left -> "right" to "bottom"
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
    val axMinmax = minmaxBase(pos.ax).let {
        when (pos) {
            Bottom -> it.first to it.first + size * (it.second - it.first)
            else -> it.first + (1 - size) * (it.second - it.first) to it.second
        }
    }
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

    // border
    val (bxmin, bxmax) = when (pos) {
        Top -> xminBase + (1 - size) * (xmaxBase - xminBase) to xmaxBase
        Bottom -> xminBase to xminBase + size * (xmaxBase - xminBase)
        Left -> xmin - sep - tsep - tileFillWidth to xmin - sep - tsep
        Right -> xmax + sep + tsep to xmax + sep + tsep + tileFillWidth
    }

    val (bymin, bymax) = when (pos) {
        Left, Right -> yminBase + (1 - size) * (ymaxBase - yminBase) to ymaxBase
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

fun Heatmap.withColorKeyLegend(
    pos: Position,
    sep: Double = 0.0,
    spacing: Double = 0.2,
    textSize: Number? = defTextSize,
    sizeUnit: String? = defSizeUnit
) = run {
    val legends = layers
        .filter { it.colorMap != null }
        .associate { it.title!! to it.colorMap!! }
        .toList()
    if (legends.isEmpty())
        return@run this

    val sizes = legends.map { (title, map) ->
        legendWidth(title, map, spacing, textSize ?: defTextSize) to
                legendHeight(map.size, spacing)
    }

    val maxWidth = sizes.maxOf { it.first }
    val maxHeight = sizes.maxOf { it.second }

    var feature: Feature = FeatureList(emptyList())
    var x = xminBase
    var y = ymaxBase
    for (i in legends.indices) {
        val (title, map) = legends[i]
        val (lx, ly) = when (pos) {
            Top -> x to ymax + sep + maxHeight
            Right -> xmax + sep to y
            Bottom -> x to ymin - sep
            Left -> xmin - sep - maxWidth to y
        }

        feature += mkColorKeyLegend(title, map, lx, ly, spacing, textSize, sizeUnit)
        x += sizes[i].first
        y -= sizes[i].second
    }

    val pdata = posData(pos, maxHeight, maxWidth, spacing)

    layers += HLayer(
        position = pos,
        xmin = pdata.lxmin, xmax = pdata.lxmax,
        ymin = pdata.lymin, ymax = pdata.lymax,
        feature = feature
    )

    this
}

internal fun legendWidth(
    title: String,
    elements: Map<Any?, Color>,
    sep: Double,
    textSize: Number
) = max(
    title.length.toDouble(),
    elements.maxOf { tileFillWidth + sep + it.key.toString().length }
) * tileFillWidth * textSize.toDouble() / 2.5 / defTextSize

internal fun legendHeight(nElement: Int, sep: Double) = (tileFillHeight + sep) * (nElement + 1)

internal fun Heatmap.mkColorKeyLegend(
    title: String,
    elements: Map<Any?, Color>,
    x: Double,
    y: Double,
    sep: Double,
    textSize: Number?,
    sizeUnit: String?
) = run {

    var feature: Feature = FeatureList(emptyList())

    // title
    feature += geomText(
        x = x,
        y = y,
        label = title,
        size = textSize,
        sizeUnit = sizeUnit,
        fontface = "bold",
        hjust = "left",
        vjust = "top"
    )

    // tiles
    val tx = List(elements.size) { x + tileFillWidth / 2 }
    val ty = (0 until elements.size).map { i ->
        y - tileFillHeight * 0.8 - tileFillHeight / 2 - i * (tileFillHeight + sep)
    }
    val cl = elements.map { it.value }
    var i = 0
    for ((k, _) in elements) {
        feature += geomTile(
            x = tx[i],
            y = ty[i],
            fill = cl[i],
            width = tileFillWidth,
            height = tileFillHeight
        )
        feature += geomText(
            x = tx[i] + tileFillWidth / 2 + sep,
            y = ty[i],
            label = k.toString(),
            size = textSize,
            sizeUnit = sizeUnit,
            hjust = "left",
            vjust = "center"
        )
        i += 1
    }

    feature
}
