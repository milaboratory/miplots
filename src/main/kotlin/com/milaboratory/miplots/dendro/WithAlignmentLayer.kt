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

import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.sampling.samplingNone
import kotlin.math.sign

private fun XYNode.addTextData(
    textDb: DataBuilder,
    al: Alignment,
    leafsOnly: Boolean,
    y: Double,
    sx: Double,
    sy: Double,
    textMeta: String,
) {
    if ((!leafsOnly || isLeaf) && node.metadata[textMeta] != null) {
        val chars = this.node.metadata[textMeta].toString().toCharArray()
        for (ci in chars.indices) {
            val char = chars[ci]
            val ix = this.x
            val iy = y + ci * sy * sign(y)
            val (plx, ply) = al.apply(Point(ix, iy))
            textDb.add(
                DendroVar.lx to plx,
                DendroVar.ly to ply,
                DendroVar.label to char
            )
        }
    }
    children.forEach {
        it.addTextData(textDb, al, leafsOnly, y, sx, sy, textMeta)
    }
}

private fun XYNode.addFillData(
    fillDb: DataBuilder,
    al: Alignment,
    leafsOnly: Boolean,
    y: Double,
    sx: Double,
    sy: Double,
    textMeta: String,
    eid: Int = 0
): Int {
    var i = eid
    if ((!leafsOnly || isLeaf) && node.metadata[textMeta] != null) {
        val chars = this.node.metadata[textMeta].toString().toCharArray()
        for (ci in chars.indices) {
            val char = chars[ci]

            val iy = y + ci * sy * sign(y)
            val ix = this.x

            val (px1, py1, px2, py2) = al.apply(Line(ix - sx / 2, iy - sy / 2, ix - sx / 2, iy + sy / 2))
            val (px3, py3, px4, py4) = al.apply(Line(ix + sx / 2, iy + sy / 2, ix + sx / 2, iy - sy / 2))

            val xs = listOf(px1, px2, px3, px4, px1)
            val ys = listOf(py1, py2, py3, py4, py1)
            for (p in 0..4) {
                fillDb.add(
                    DendroVar.lex to xs[p],
                    DendroVar.ley to ys[p],
                    DendroVar.lid to i,
                    DendroVar.label to char
                )
            }
            i += 1
        }
    }
    children.forEach {
        i += it.addFillData(
            fillDb,
            al,
            leafsOnly,
            y,
            sx,
            sy,
            textMeta,
            i
        )
    }
    return i - eid
}

fun GGDendroPlot.withAlignmentLayer(
    textMeta: String,
    showText: Boolean = true,
    leafsOnly: Boolean = false,
    textAlpha: Number = 1.0,
    fillAlpha: Number = 0.7,
) = run {
    ggDendro.withAlignmentLayer(
        textMeta = textMeta,
        showText = showText,
        textAlpha = textAlpha,
        fillAlpha = fillAlpha,
        leafsOnly = leafsOnly,
    )
    this
}

private const val letsPlotGroupLimit = 900

fun ggDendro.withAlignmentLayer(
    textMeta: String,
    showText: Boolean = true,
    leafsOnly: Boolean = false,
    textAlpha: Number = 1.0,
    fillAlpha: Number = 0.7
) {
    val textSizeUnit = nodeSizeUnit
    val sizeBase = nodeSize ?: 2.0
    val textSize = 4 * sizeBase
    val textSizeActual = textSize * lwx
    val labelAngle = when (rpos.alignment) {
        Alignment.Horizontal -> 0.0
        Alignment.Vertical -> 90.0
    }
    val textWd = sizeBase * lwx
    val textHt = sizeBase * lwy
    val maxht = sizeBase * lwy * (
            xy.node.toList()
                .mapNotNull { it.metadata[textMeta] }
                .maxOfOrNull { it.toString().length.toDouble() }
                ?: 1.0) * 0.65
    val shift = lwy * 2 * rpos.ysign

    val fillDb = DataBuilder()
    xy.addFillData(
        fillDb,
        al = rpos.alignment,
        leafsOnly = leafsOnly,
        y = xy.leafY + yDelta + shift,
        sx = textWd,
        sy = textHt,
        textMeta = textMeta
    )

    if (fillDb.result.isEmpty())
        return

    // tweak lets-plot group limit
    val nGroups = fillDb.result[DendroVar.lid]!!.size
    assert(nGroups % 5 == 0)
    if (nGroups / 5 > letsPlotGroupLimit) {

        val baseDelta = 5 * letsPlotGroupLimit
        val data = fillDb.result
        var i = 0
        while (true) {
            val delta = if (i + baseDelta > nGroups)
                nGroups - i
            else
                baseDelta

            annotationLayers += geomPolygon(
                mapOf(
                    DendroVar.lex to data[DendroVar.lex]!!.subList(i, i + delta),
                    DendroVar.ley to data[DendroVar.ley]!!.subList(i, i + delta),
                    DendroVar.lid to data[DendroVar.lid]!!.subList(i, i + delta),
                    DendroVar.label to data[DendroVar.label]!!.subList(i, i + delta)
                ),
                alpha = fillAlpha,
                showLegend = false,
                sampling = samplingNone
            ) {
                x = DendroVar.lex
                y = DendroVar.ley
                group = DendroVar.lid
                fill = DendroVar.label
            }
            if (delta != baseDelta)
                break

            i += delta
        }
    } else {
        annotationLayers += geomPolygon(
            fillDb.result,
            alpha = fillAlpha,
            showLegend = false,
            sampling = samplingNone
        ) {
            x = DendroVar.lex
            y = DendroVar.ley
            group = DendroVar.lid
            fill = DendroVar.label
        }
    }

    if (showText) {
        val textDb = DataBuilder()
        xy.addTextData(
            textDb,
            rpos.alignment,
            leafsOnly = leafsOnly,
            y = xy.leafY + yDelta + shift,
            sx = textWd,
            sy = textHt,
            textMeta = textMeta,
        )

        annotationLayers += geomText(
            textDb.result,
            color = "black",
            alpha = textAlpha,
            size = textSizeActual,
            sizeUnit = textSizeUnit,
            angle = labelAngle,
            showLegend = false,
            sampling = samplingNone
        ) {
            this.x = DendroVar.lx
            this.y = DendroVar.ly
            this.label = DendroVar.label
        }
    }

    yDelta += shift + maxht * rpos.ysign
}
