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

import jetbrains.datalore.base.interval.DoubleSpan
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.plot.base.scale.transform.Transforms
import jetbrains.datalore.plot.builder.scale.provider.ColorGradientnMapperProvider
import jetbrains.letsPlot.intern.Scale
import jetbrains.letsPlot.scale.scaleColorGradientN
import jetbrains.letsPlot.scale.scaleColorManual
import jetbrains.letsPlot.scale.scaleFillGradientN
import jetbrains.letsPlot.scale.scaleFillManual

/**
 *
 */
interface UniversalPalette : DiscreteColorMapping, ContinuousColorMapping {
    /// ggplot stuff

    fun scaleFillDiscrete(
        n: Int,
        name: String? = null,
        breaks: List<Any>? = null,
        labels: List<String>? = null,
        limits: List<Any>? = null,
        naValue: Any? = null,
        format: String? = null,
        guide: Any? = null
    ): Scale

    fun scaleColorDiscrete(
        n: Int,
        name: String? = null,
        breaks: List<Any>? = null,
        labels: List<String>? = null,
        limits: List<Any>? = null,
        naValue: Any? = null,
        format: String? = null,
        guide: Any? = null
    ): Scale

    fun scaleFillContinuous(
        name: String? = null,
        breaks: List<Number>? = null,
        labels: List<String>? = null,
        limits: Pair<Number?, Number?>? = null,
        naValue: Any? = null,
        format: String? = null,
        guide: Any? = null
    ): Scale

    fun scaleColorContinuous(
        name: String? = null,
        breaks: List<Number>? = null,
        labels: List<String>? = null,
        limits: Pair<Number?, Number?>? = null,
        naValue: Any? = null,
        format: String? = null,
        guide: Any? = null
    ): Scale
}


class GradientBasePallete(
    val base: List<Color>,
    val na: Color
) : UniversalPalette {
    private val mapper = ColorGradientnMapperProvider(
        base,
        na
    ).createContinuousMapper(
        DoubleSpan(0.0, 1.0),
        Transforms.IDENTITY
    )

    override fun mkMapping(): (Double?) -> Color = { mapper(it)!! }

    override fun <T> mkMap(objects: List<T?>, loop: Boolean): Map<T?, Color> {
        val non = objects.filterNot { it == null }
        val colors = mkColors(non.size)
        val m = non.mapIndexed { i, e -> e to colors[i] }.toMap().toMutableMap()
        if (non.size != objects.size)
            m += (null to na)
        return m
    }

    fun mkColors(n: Int): List<Color> {
        if (n < 2)
            throw IllegalArgumentException("n < 2")

        val first = 1.0 / base.size
        val last = 1.0 - first

        return if (n == 2)
            listOf(mapper(first), mapper(last))
        else {
            val delta = (last - first) / (n - 2)
            listOf(mapper(first)!!) +
                    (0 until n - 2).map {
                        mapper(first + (1 + it) * delta)
                    } +
                    mapper(last)!!
        }.map { it!! }
    }

    override fun scaleFillDiscrete(
        n: Int,
        name: String?,
        breaks: List<Any>?,
        labels: List<String>?,
        limits: List<Any>?,
        naValue: Any?,
        format: String?,
        guide: Any?
    ) = scaleFillManual(
        mkColors(n),
        name = name,
        breaks = breaks,
        labels = labels,
        limits = limits,
        naValue = na,
        format = format,
        guide = guide
    )

    override fun scaleColorDiscrete(
        n: Int,
        name: String?,
        breaks: List<Any>?,
        labels: List<String>?,
        limits: List<Any>?,
        naValue: Any?,
        format: String?,
        guide: Any?
    ) = scaleColorManual(
        mkColors(n),
        name = name,
        breaks = breaks,
        labels = labels,
        limits = limits,
        naValue = na,
        format = format,
        guide = guide
    )

    override fun scaleFillContinuous(
        name: String?,
        breaks: List<Number>?,
        labels: List<String>?,
        limits: Pair<Number?, Number?>?,
        naValue: Any?,
        format: String?,
        guide: Any?
    ) = scaleFillGradientN(
        colors = base.map { it.toHexColor() },
        name = name,
        breaks = breaks,
        labels = labels,
        limits = limits,
        naValue = na,
        format = format,
        guide = guide
    )

    override fun scaleColorContinuous(
        name: String?,
        breaks: List<Number>?,
        labels: List<String>?,
        limits: Pair<Number?, Number?>?,
        naValue: Any?,
        format: String?,
        guide: Any?
    ) = scaleColorGradientN(
        colors = base.map { it.toHexColor() },
        name = name,
        breaks = breaks,
        labels = labels,
        limits = limits,
        naValue = na,
        format = format,
        guide = guide
    )

    companion object {
        operator fun invoke(vararg base: String, na: String) =
            GradientBasePallete(base.map { Color.parseHex(it) }, Color.parseHex(na))
    }
}
