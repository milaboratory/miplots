package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.MiFonts
import com.milaboratory.miplots.Position
import jetbrains.letsPlot.geom.geomText

fun Heatmap.withLabels(
    pos: Position,
    labels: List<String>? = null,
    angle: Number? = null,
    sep: Double = 0.0,
    width: Double? = null,
    height: Double? = null,
    textSize: Number? = defTextSize,
    sizeUnit: String = defSizeUnit
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
        hjust = pdata.hjust,
        size = textSize,
        sizeUnit = sizeUnit,
        family = MiFonts.monospace
    ) {
        this.x = "x"
        this.y = "y"
        this.label = "l"
    }

    layers += HLayer(
        pos,
        xmin = pdata.lxmin, xmax = pdata.lxmax,
        ymin = pdata.lymin, ymax = pdata.lymax,
        feature = feature
    )

    this
}
