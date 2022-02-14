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
