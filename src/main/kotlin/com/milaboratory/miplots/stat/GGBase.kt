package com.milaboratory.miplots.stat

import com.milaboratory.miplots.PlotWrapper
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.Orientation
import jetbrains.letsPlot.intern.layer.PosOptions
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.api.firstOrNull

/** Parent class for stat plots */
abstract class GGBase(
    /** x series (discrete) */
    val x: String,
    /** y series (continuous) */
    val y: String,

    /** Organize data in facets */
    val facetBy: String?,
    /** Number of columns in facet view */
    val facetNCol: Int?,
    /** Number of rows in facet view */
    val facetNRow: Int?,
    color: String?,
    fill: String?,
    shape: Any?,
    linetype: String?,
    size: Number?,
    width: Double?,
    alpha: Double?,
    /** Data position */
    val position: PosOptions? = null,
    /** Plot orientation */
    val orientation: Orientation = Orientation.Vertical,
    /** Color scale */
    val colorScale: DiscreteColorMapping = Palletes.Categorical.auto,
    /** Fill scale */
    val fillScale: DiscreteColorMapping = Palletes.Categorical.auto,
    /** Aesthetics mapping */
    aesMapping: GGAes.() -> Unit = {}
) : WithAes(color, fill, shape, linetype, size, width, alpha, aesMapping), PlotWrapper {
    abstract val groupBy: String?
    protected fun filterGroupBy(vararg g: String?) = g
        .filterNotNull()
        .filter { it != x }
        .filter { data[it].isCategorial() }
        .firstOrNull()

    /** actual data */
    abstract val data: AnyFrame

    /** generic cache */
    internal val cache = mutableMapOf<Any, Any>()
}

fun DataColumn<*>.isCategorial() = this.firstOrNull { it !is Number } != null
fun DataColumn<*>.isNumerica() = !this.isCategorial()
