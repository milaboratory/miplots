package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color

/**
 *
 */
class DiscretePallete(val colors: List<Color>) : DiscreteColorMapping {
    val nColors = colors.size
    
    override fun <T> mkMap(objects: List<T?>): Map<T?, Color> {
        if (objects.size > colors.size)
            throw IllegalArgumentException("not enough colors")
        return objects.mapIndexed { i, e -> e to colors[i] }.toMap()
    }

    companion object {
        operator fun invoke(vararg colors: String) = DiscretePallete(colors.map { Color.parseHex(it) })
    }
}
