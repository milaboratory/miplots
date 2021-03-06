/*
 *
 * Copyright (c) 2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/miplots/blob/main/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.MiFonts
import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.sampling.samplingNone
import kotlin.math.sign

private fun XYNode.addLabelsData(
    db: DataBuilder,
    label: Any,
    al: Alignment,
    imposedLeafY: Double?,
    shiftLabelX: Double = 0.0,
    shiftLabelY: Double = 0.0
) {

    if (node.metadata[label] != null) {
        val (lx, ly) = al.apply(Point(this.x + shiftLabelX, this.yImposed(imposedLeafY) + shiftLabelY))
        db.add(
            DendroVar.lx to lx,
            DendroVar.ly to ly,
            DendroVar.label to node.metadata[label]
        )
    }

    children.forEach { it.addLabelsData(db, label, al, imposedLeafY, shiftLabelX, shiftLabelY) }
}

private fun XYNode.addLabelBorder(
    db: DataBuilder,
    al: Alignment,
    imposedLeafY: Double?,
    label: Any,
    sx: Double,
    sy: Double,
    shiftLabelX: Double = 0.0,
    shiftLabelY: Double = 0.0,
    eid: Int = 0
): Int {

    var i = eid
    if (this.node.metadata[label] != null) {
        val ix = this.x + shiftLabelX
        val iy = this.yImposed(imposedLeafY) + shiftLabelY

        val (px1, py1, px2, py2) = al.apply(Line(ix - sx, iy - sy, ix - sx, iy + sy))
        val (px3, py3, px4, py4) = al.apply(Line(ix + sx, iy + sy, ix + sx, iy - sy))

        db.add(
            DendroVar.lex to px1,
            DendroVar.ley to py1,
            DendroVar.lid to i,
            DendroVar.label to node.metadata[label]
        )
        db.add(
            DendroVar.lex to px2,
            DendroVar.ley to py2,
            DendroVar.lid to i,
            DendroVar.label to node.metadata[label]
        )
        db.add(
            DendroVar.lex to px3,
            DendroVar.ley to py3,
            DendroVar.lid to i,
            DendroVar.label to node.metadata[label]
        )
        db.add(
            DendroVar.lex to px4,
            DendroVar.ley to py4,
            DendroVar.lid to i,
            DendroVar.label to node.metadata[label]
        )
        db.add(
            DendroVar.lex to px1,
            DendroVar.ley to py1,
            DendroVar.lid to i,
            DendroVar.label to node.metadata[label]
        )
        i += 1
    }
    children.forEach { i += it.addLabelBorder(db, al, imposedLeafY, label, sx, sy, shiftLabelX, shiftLabelY, i) }
    return i - eid
}

internal fun hjust(rpos: Position, angle: Number) = when (rpos) {
    Top -> if (angle == 0.0) "center" else "right"
    Bottom -> if (angle == 0.0) "center" else "left"
    Left -> if (angle == 0.0) "left" else "center"
    Right -> if (angle == 0.0) "right" else "center"
}

internal fun vjust(rpos: Position, angle: Number) = when (rpos) {
    Bottom -> if (angle == 0.0) "bottom" else "center"
    Top -> if (angle == 0.0) "top" else "center"
    Left -> if (angle == 0.0) "center" else "top"
    Right -> if (angle == 0.0) "center" else "bottom"
}

fun ggDendro.withLabels(
    label: Any,
    labelSize: Double? = null,
    labelAngle: Number = 0.0,
    fillLabels: Boolean = true,
    fillAlpha: Number? = null,
) = run {
    val textSizeUnit = nodeSizeUnit
    val sizeBase = labelSize ?: nodeSize ?: 2.0
    val textSize = 4 * sizeBase
    val textSizeActual = textSize * lwx
    val textWd = sizeBase * lwx
    val textHt = sizeBase * lwy

    val lshiftX = 0.0
    val lshiftY = when (rpos) {
        Top, Right -> -3 * lwy
        Bottom, Left -> 3 * lwy
    }
    val hjust = hjust(rpos, labelAngle)
    val vjust = vjust(rpos, labelAngle)

    val dbLabels = DataBuilder()
    val dbLabelsBorder = DataBuilder()

    xy.addLabelsData(
        dbLabels,
        label,
        rpos.alignment,
        imposedLeafY,
        lshiftX,
        lshiftY
    )

    xy.addLabelBorder(
        dbLabelsBorder,
        rpos.alignment,
        imposedLeafY,
        label,
        textWd,
        textHt,
        lshiftX,
        lshiftY + when (rpos) {
            Top, Right -> -textHt / 2
            Bottom, Left -> +textHt / 2
        }
    )

    yDelta += lshiftY + 2 * sign(lshiftY) * textHt

    val labels = geomText(
        dbLabels.result,
        naText = "",
        angle = labelAngle,
        size = textSizeActual,
        hjust = hjust,
        vjust = vjust,
        sizeUnit = textSizeUnit,
        family = MiFonts.monospace
    ) {
        this.x = DendroVar.lx
        this.y = DendroVar.ly
        this.label = DendroVar.label
    }

    val borders = geomPath(
        dbLabelsBorder.result,
        color = lineColor,
        size = lwx / 5,
        sampling = samplingNone,
    ) {
        this.x = DendroVar.lex
        this.y = DendroVar.ley
        this.group = DendroVar.lid
    }

    val fills = geomPolygon(
        dbLabelsBorder.result,
        alpha = fillAlpha,
        sampling = samplingNone,
        showLegend = false
    ) {
        this.x = DendroVar.lex
        this.y = DendroVar.ley
        this.fill = DendroVar.label
        this.group = DendroVar.lid
    }

    val r = mutableListOf<Feature>()
    r += labels

    if (fillLabels && xy.node.toList()
            .mapNotNull { it.metadata[label] }
            .none { it.toString().length > 1 }
    ) {
        r += borders
        r += fills
    }

    annotationLayers.addAll(r)
}

fun GGDendroPlot.withLabels(
    label: Any,
    labelSize: Double? = null,
    labelAngle: Number = 0.0,
    fillLabels: Boolean = true,
    fillAlpha: Number? = 0.1
) = run {
    this.ggDendro.withLabels(label, labelSize, labelAngle, fillLabels, fillAlpha)
    this
}