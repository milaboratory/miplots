package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.intern.Scale
import jetbrains.letsPlot.scale.scaleColorManual
import jetbrains.letsPlot.scale.scaleFillManual

interface ContinuousColorMapping {
    fun mkMapping(): (Double?) -> Color
}

interface DiscreteColorMapping {
    fun <T> mkMap(objects: List<T>): Map<T, Color>

    fun <T> mkMapping(objects: List<T>): (T) -> Color {
        val map = mkMap(objects)
        return { map[it]!! }
    }

    fun mkMapping(nColors: Int): (Int) -> Color = mkMapping((0 until nColors).toList())

    private fun <T> scale(objects: List<T>, scale: (values: List<String>, limits: List<Any>) -> Scale): Scale = run {
        val map = mkMap(objects)
        scale(
            objects.map { map[it]!!.toHexColor() },
            objects.map { it as Any },
        )
    }

    fun <T> colorScale(objects: List<T>) = scale(objects) { v, l ->
        scaleColorManual(
            values = v,
            limits = l,
        )
    }

    fun <T> fillScale(objects: List<T>) = scale(objects) { v, l ->
        scaleFillManual(
            values = v,
            limits = l,
        )
    }
}
