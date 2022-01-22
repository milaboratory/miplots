package com.milaboratory.miplots.stat

import com.milaboratory.miplots.PlotWrapper
import com.milaboratory.miplots.stat.xdiscrete.Orientation
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.api.firstOrNull

/**
 *
 */
abstract class GGBase(
    /** x series (discrete) */
    val x: String,
    /** y series (continuous) */
    val y: String,

    /** Organize data in facets */
    val facetBy: String? = null,
    /** Number of columns in facet view */
    val facetNCol: Int? = null,
    /** Number of rows in facet view */
    val facetNrow: Int? = null,
    /** Outline color */
    val color: String? = null,
    /** Fill color */
    val fill: String? = null,
    /** Plot orientation */
    val orientation: Orientation = Orientation.Vertical,
    /** Aesthetics mapping */
    aesMapping: GGAes.() -> Unit = {}
) : PlotWrapper {
    val aes = GGAes().apply(aesMapping)

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
