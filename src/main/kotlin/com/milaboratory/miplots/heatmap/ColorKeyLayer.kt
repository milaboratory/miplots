package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import org.jetbrains.kotlinx.dataframe.api.rows
import kotlin.math.max

fun Heatmap.withColorKey(
    key: String,
    pos: Position = Bottom,
    sep: Double = 0.0,
    label: String? = null,
    labelPos: Position? = null,
    labelSep: Double = 0.0,
    labelSize: Double? = 0.0,
    labelAngle: Number? = 0.0,
    pallete: DiscreteColorMapping = Palletes.Diverging.lime90rose130,
    textSize: Number? = defTextSize,
    sizeUnit: String = defSizeUnit
) = run {
    val pdata = posData(pos, tileHeight, tileWidth, sep)

    val ck = data.rows().map { it[axCol(pos.ax)] to it[key] }.distinct().toMap()
    val valuesDistinct = data[key].distinct().toList()
    val cMap = pallete.mkMap(valuesDistinct)

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
            fill = cMap[ck[el]],
            width = tileFillWidth,
            height = tileFillHeight
        )

        if (feature == null)
            feature = f
        else
            feature += f
    }

    var (lxminadd, lxmaxadd) = 0.0 to 0.0
    var (lyminadd, lymaxadd) = 0.0 to 0.0

    labelPos?.apply {
        val (llx, lly) = when (pos) {
            Top, Bottom -> (if (labelPos == Left) xminBase - labelSep else xmaxBase + labelSep) to pdata.ly
            Left, Right -> pdata.lx to (if (labelPos == Top) ymaxBase + labelSep else yminBase - labelSep)
        }

        labelSize?.apply {
            when (labelPos) {
                Top -> lxminadd = this
                Right -> lxmaxadd = this
                Bottom -> lyminadd = this
                Left -> lxminadd = this
            }
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
                angle = labelAngle,
                vjust = vjust,
                hjust = hjust,
                size = textSize,
                sizeUnit = sizeUnit
            )
        )
    }

    layers += HLayer(
        position = pos,
        xmin = pdata.lxmin, xminadd = lxminadd,
        xmax = pdata.lxmax, xmaxadd = lxmaxadd,
        ymin = pdata.lymin, yminadd = lyminadd,
        ymax = pdata.lymax, ymaxadd = lymaxadd,
        feature = feature!!,
        colorMap = cMap,
        title = label ?: key
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
