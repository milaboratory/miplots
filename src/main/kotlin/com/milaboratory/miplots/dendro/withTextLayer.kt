package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.MiFonts
import com.milaboratory.miplots.dendro.Alignment.Horizontal
import com.milaboratory.miplots.dendro.Alignment.Vertical
import jetbrains.letsPlot.geom.geomText

private fun XYNode.addTextLayer(
    db: DataBuilder,
    al: Alignment,
    leafsOnly: Boolean,
    y: Double,
) {
    if (!leafsOnly || isLeaf) {
        val (lx, ly) = al.apply(Point(this.x, y))
        db.add(
            DendroVar.lx to lx,
            DendroVar.ly to ly,
            *node.metadata.toList().toTypedArray()
        )
    }

    children.forEach { it.addTextLayer(db, al, leafsOnly, y) }
}

fun GGDendroPlot.withTextLayer(
    textMeta: String,
    leafsOnly: Boolean = false
) = run {
    ggDendro.withTextLayer(textMeta, leafsOnly)
    this
}

fun ggDendro.withTextLayer(
    textMeta: String,
    leafsOnly: Boolean = false
) {
    val textSizeUnit = nodeSizeUnit
    val sizeBase = nodeSize ?: 2.0
    val textSize = 4 * sizeBase
    val textSizeActual = textSize * lwx
    val labelAngle = when (rpos.alignment) {
        Horizontal -> 0.0
        Vertical -> 90.0
    }
    val hjust = hjust(rpos, labelAngle)
    val vjust = vjust(rpos, labelAngle)
    val maxht = sizeBase * lwy * (
            xy.node.toList()
                .mapNotNull { it.metadata[textMeta] }
                .maxOfOrNull { it.toString().length.toDouble() }
                ?: 1.0) * 0.65
    val shift = lwy * 2 * rpos.ysign

    val db = DataBuilder()
    xy.addTextLayer(
        db,
        rpos.alignment,
        leafsOnly,
        xy.leafY + yDelta + shift
    )
    annotationLayers += geomText(
        db.result,
        hjust = hjust,
        vjust = vjust,
        angle = labelAngle,
        size = textSizeActual,
        sizeUnit = textSizeUnit,
        naText = "",
        family = MiFonts.monospace
    ) {
        x = DendroVar.lx
        y = DendroVar.ly
        label = textMeta
    }

    yDelta += shift + maxht * rpos.ysign
}

