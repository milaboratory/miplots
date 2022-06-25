package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color

/**
 *
 */
class DiscretePalette(val colors: List<Color>) : DiscreteColorMapping {
    val nColors = colors.size

    override fun <T> mkMap(objects: List<T?>, loop: Boolean): Map<T?, Color> {
        if (!loop && objects.size > colors.size)
            throw IllegalArgumentException("not enough colors")
        return objects.mapIndexed { i, e -> e to colors[i % colors.size] }.toMap()
    }

    companion object {
        operator fun invoke(vararg colors: String) = DiscretePalette(colors.map { Color.parseHex(it) })
    }

    fun shift(n: Int) = DiscretePalette(colors.drop(n))
}
