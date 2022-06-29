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
package com.milaboratory.miplots.heatmap

import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.geom.geomRect
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList


internal fun borderLayer(
    width: Double = defBorderWidth,
    xmin: Double, xmax: Double,
    ymin: Double, ymax: Double,
    color: Any? = defBorderColor
) = run {
    var feature: Feature = FeatureList(emptyList())

    // top
    feature += geomRect(
        xmin = xmin,
        ymin = ymax - width,
        xmax = xmax,
        ymax = ymax,
        color = color,
        fill = color,
        size = 0.0
    )

    // right
    feature += geomRect(
        xmin = xmax - width,
        ymin = ymin,
        xmax = xmax,
        ymax = ymax,
        color = color,
        fill = color,
        size = 0.0
    )

    // bottom
    feature += geomRect(
        xmin = xmin,
        ymin = ymin,
        xmax = xmax,
        ymax = ymin + width,
        color = color,
        fill = color,
        size = 0.0
    )

    // left
    feature += geomRect(
        xmin = xmin,
        ymin = ymin,
        xmax = xmin + width,
        ymax = ymax,
        color = color,
        fill = color,
        size = 0.0
    )

    feature
}

fun Heatmap.withBorder(
    width: Double = defBorderWidth,
    color: Any = Color.BLACK
) = run {
    if (layers.isNotEmpty())
        throw IllegalStateException("border should be added before any other layers")

    layers += HLayer(
        position = null,
        xmin = xminBase, xmax = xmaxBase,
        ymin = yminBase, ymax = ymaxBase,
        feature = borderLayer(width, xminBase, xmaxBase, yminBase, ymaxBase, color)
    )

    this
}
