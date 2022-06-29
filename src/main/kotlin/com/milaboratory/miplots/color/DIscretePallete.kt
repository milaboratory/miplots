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
