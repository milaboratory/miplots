package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.MiFonts
import com.milaboratory.miplots.color.DiscretePalette
import com.milaboratory.miplots.color.Palettes
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import kotlin.math.sign


private fun XYNode.addAlignmentData(
    features: MutableList<Feature>,
    al: Alignment,
    y: Double,
    sx: Double,
    sy: Double,
    showText: Boolean,
    textAlpha: Number?,
    fillAlpha: Number?,
    textSizeUnit: String,
    textSize: Double,
    angle: Number,
    textMeta: String,
    cmap: Map<Char?, Color>
) {
    if (this.node.metadata[textMeta] != null) {
        val chars = this.node.metadata[textMeta].toString().toCharArray()
        for (ci in chars.indices) {
            val char = chars[ci]
            val color = cmap[char]!!

            val iy = y + ci * sy * sign(y)
            val ix = this.x

            val (px1, py1, px2, py2) = al.apply(Line(ix - sx / 2, iy - sy / 2, ix - sx / 2, iy + sy / 2))
            val (px3, py3, px4, py4) = al.apply(Line(ix + sx / 2, iy + sy / 2, ix + sx / 2, iy - sy / 2))

            val map = mapOf(
                "x" to listOf(px1, px2, px3, px4, px1),
                "y" to listOf(py1, py2, py3, py4, py1)
            )
            features += geomPolygon(
                map,
                fill = color,
                alpha = fillAlpha
            ) {
                this.x = "x"
                this.y = "y"
            }
            if (showText) {
                val (plx, ply) = al.apply(Point(ix, iy))

                features += geomText(
                    x = plx,
                    y = ply,
                    size = textSize,
                    sizeUnit = textSizeUnit,
                    angle = angle,
                    label = char.toString(),
                    family = MiFonts.monospace,
                    alpha = textAlpha,
                    color = "black"
                )
            }
        }
    }
    children.forEach {
        it.addAlignmentData(
            features,
            al,
            y,
            sx,
            sy,
            showText,
            textAlpha,
            fillAlpha,
            textSizeUnit,
            textSize,
            angle,
            textMeta,
            cmap
        )
    }
}

fun GGDendroPlot.withAlignmentLayer(
    textMeta: String,
    showText: Boolean = true,
    textAlpha: Number = 1.0,
    fillAlpha: Number = 0.7,
    palette: DiscretePalette? = null
) = run {
    ggDendro.withAlignmentLayer(
        textMeta = textMeta,
        showText = showText,
        textAlpha = textAlpha,
        fillAlpha = fillAlpha,
        palette = palette
    )
    this
}

fun ggDendro.withAlignmentLayer(
    textMeta: String,
    showText: Boolean = true,
    textAlpha: Number = 1.0,
    fillAlpha: Number = 0.7,
    palette: DiscretePalette? = null
) {
    val chars = xy.node.toList()
        .mapNotNull { it.metadata[textMeta] }
        .flatMap { it.toString().toList() }
        .distinct()
    val cmap = (palette ?: Palettes.Categorical.auto(chars.size)).mkMap(chars, loop = true)

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

    xy.addAlignmentData(
        annotationLayers,
        al = rpos.alignment,
        y = xy.leafY + yDelta + shift,
        sx = textWd,
        sy = textHt,
        showText = showText,
        textAlpha = textAlpha,
        fillAlpha = fillAlpha,
        textSizeUnit = textSizeUnit,
        textSize = textSizeActual,
        angle = labelAngle,
        textMeta = textMeta,
        cmap = cmap
    )

    yDelta += shift + maxht * rpos.ysign
}
