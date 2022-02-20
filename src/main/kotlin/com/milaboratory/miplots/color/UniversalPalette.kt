package com.milaboratory.miplots.color

import jetbrains.datalore.base.gcommon.collect.ClosedRange
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.plot.base.scale.transform.Transforms
import jetbrains.datalore.plot.builder.scale.provider.ColorGradient2MapperProvider
import jetbrains.letsPlot.intern.Scale
import jetbrains.letsPlot.scale.scaleColorGradient2
import jetbrains.letsPlot.scale.scaleColorManual
import jetbrains.letsPlot.scale.scaleFillGradient2
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
        midpoint: Double,
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
    private val mapper = ColorGradient2MapperProvider(
        base[0],
        base[base.size / 2],
        base[base.size - 1],
        null,
        na
    ).createContinuousMapper(
        ClosedRange(0.0, 1.0),
        0.0, 1.0, Transforms.IDENTITY
    )

    override fun mkMapping(): (Double?) -> Color = { mapper.apply(it)!! }

    override fun <T> mkMap(objects: List<T?>): Map<T?, Color> {
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
            listOf(mapper.apply(first), mapper.apply(last))
        else {
            val delta = (last - first) / (n - 2)
            listOf(mapper.apply(first)!!) +
                    (0 until n - 2).map {
                        mapper.apply(first + (1 + it) * delta)
                    } +
                    mapper.apply(last)!!
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
        midpoint: Double,
        breaks: List<Number>?,
        labels: List<String>?,
        limits: Pair<Number?, Number?>?,
        naValue: Any?,
        format: String?,
        guide: Any?
    ) = scaleFillGradient2(
        low = base[0].toHexColor(),
        mid = base[base.size - 1].toHexColor(),
        high = base[base.size / 2].toHexColor(),
        midpoint = midpoint,
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
    ) = scaleColorGradient2(
        low = base[0].toHexColor(),
        mid = base[base.size - 1].toHexColor(),
        high = base[base.size / 2].toHexColor(),
        midpoint = 0.5,
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
