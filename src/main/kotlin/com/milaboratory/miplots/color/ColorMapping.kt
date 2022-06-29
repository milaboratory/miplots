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
package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.intern.Scale
import jetbrains.letsPlot.scale.scaleColorManual
import jetbrains.letsPlot.scale.scaleFillManual

interface ContinuousColorMapping {
    fun mkMapping(): (Double?) -> Color
}

interface DiscreteColorMapping {
    fun <T> mkMap(objects: List<T?>, loop: Boolean = false): Map<T?, Color>

    fun <T> mkMapping(objects: List<T?>): (T?) -> Color {
        val map = mkMap(objects)
        return { map[it]!! }
    }

    fun mkMapping(nColors: Int): (Int) -> Color = mkMapping((0 until nColors).toList())

    private fun <T> scale(objects: List<T?>, scale: (values: List<String>, limits: List<Any>?) -> Scale): Scale = run {
        val map = mkMap(objects)
        scale(
            objects.map { map[it]!!.toHexColor() },
            objects.map { it ?: "null" },
        )
    }

    fun <T> colorScale(objects: List<T?>) = scale(objects) { v, l ->
        scaleColorManual(
            values = v,
            limits = l,
        )
    }

    fun <T> fillScale(objects: List<T?>) = scale(objects) { v, l ->
        scaleFillManual(
            values = v,
            limits = l,
        )
    }
}
