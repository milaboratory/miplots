package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color

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
}
