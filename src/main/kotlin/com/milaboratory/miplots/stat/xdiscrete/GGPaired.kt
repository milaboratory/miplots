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
@file:Suppress("LocalVariableName", "ClassName")

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.Orientation
import com.milaboratory.miplots.color.DiscreteColorMapping
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.stat.GGAes
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.Feature
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.*

fun GGPaired(
    data: AnyFrame,
    x: String? = null,
    y: String? = null,
    cond1: String? = null,
    cond2: String? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNRow: Int? = null,
    color: String? = null,
    fill: String? = null,
    pointSize: Double? = 1.2,
    lineSize: Double? = 0.5,
    lineColor: Any? = "black",
    linetype: String? = "solid",
    orientation: Orientation = Orientation.Vertical,
    colorScale: DiscreteColorMapping = Palettes.Diverging.viridis2magma,
    fillScale: DiscreteColorMapping = Palettes.Diverging.viridis2magma,
    aesMapping: GGAes.() -> Unit = {}
) = run {
    val _data: AnyFrame
    val _x: String
    val _y: String
    val _cond1: String
    val _cond2: String
    if (cond1 != null) {
        if (cond2 == null || x != null || y != null)
            throw IllegalArgumentException()

        _cond1 = cond1
        _cond2 = cond2
        _x = "condition"
        _y = "y"
        val len = data.rowsCount()
        val ycol = data[cond1].concat(data[cond2]) named _y
        val xcol = (List(len) { cond1 } + List(len) { cond2 }).toColumn(_x)
        _data = dataFrameOf(xcol, ycol)
    } else {
        if (x == null || y == null || cond2 != null)
            throw IllegalArgumentException()
        val xd = data[x].distinct()
        if (xd.size() != 2)
            throw IllegalArgumentException()
        _data = data
        _x = x
        _y = y
        _cond1 = xd[0] as String
        _cond2 = xd[1] as String
    }

    GGPaired(
        _cond1,
        _cond2,
        _data,
        _x,
        _y,
        facetBy,
        facetNCol,
        facetNRow,
        color,
        fill,
        pointSize,
        lineSize,
        lineColor,
        linetype,
        orientation,
        colorScale,
        fillScale,
        aesMapping
    )
}

/**
 *
 */
class GGPaired internal constructor(
    val cond1: String, val cond2: String,
    data: AnyFrame, x: String, y: String,
    facetBy: String?,
    facetNCol: Int?,
    facetNRow: Int?,
    color: String?,
    fill: String?,
    val pointSize: Double?,
    val lineSize: Double?,
    val lineColor: Any?,
    linetype: String?,
    orientation: Orientation,
    colorScale: DiscreteColorMapping = Palettes.Diverging.viridis2magma,
    fillScale: DiscreteColorMapping = Palettes.Diverging.viridis2magma,
    aesMapping: GGAes.() -> Unit = {}
) : GGXDiscrete(
    _data = data,
    x = x,
    y = y,
    xValues = null,
    groupByValues = null,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNRow = facetNRow,
    linetype = linetype,
    color = color,
    fill = fill,
    orientation = orientation,
    colorScale = colorScale,
    fillScale = fillScale,
    aesMapping = aesMapping
) {

    override val groupBy = filterGroupBy(aes.fill, aes.color)

    private fun linesLayer(a: List<Any?>, b: List<Any?>, facet: Any?): Feature = run {
        val pathData = mutableMapOf<String, MutableList<Any?>>(
            this.xNumeric to mutableListOf(),
            this.y to mutableListOf(),
            "__group" to mutableListOf()
        )
        if (facet != null) {
            pathData += this.facetBy!! to mutableListOf()
        }

        val x1 = xnum[cond1]
        val x2 = xnum[cond2]
        for (i in a.indices) {
            pathData[this.xNumeric]!!.addAll(listOf(x1, x2))
            pathData[this.y]!!.addAll(listOf(a[i], b[i]))
            pathData["__group"]!!.addAll(listOf(i, i))
            if (facet != null) {
                pathData[this.facetBy]!!.addAll(listOf(facet, facet))
            }
        }

        geomPath(pathData, color = lineColor, linetype = this.linetype, size = lineSize) {
            group = "__group"
        }
    }

    /** base box plot */
    override fun basePlot() = run {
        var plt = super.basePlot()

        plt += geomBoxplot(color = this.color, fill = this.fill) {
            this.color = aes.color
            this.fill = aes.fill
        }

        plt += geomPoint(color = this.color, fill = this.fill, size = pointSize) {
            this.color = aes.color
            this.fill = aes.fill
        }

        if (this.facetBy == null) {
            val ab = data.groupBy(x).groups.toList()
            if (ab.size != 2)
                throw IllegalArgumentException()
            val a = ab[0][this.y].toList()
            val b = ab[1][this.y].toList()
            plt += linesLayer(a, b, null)
        } else {
            val ff = data.groupBy(this.facetBy).groups.toList()
            for (frame in ff) {
                val facet = frame.first()[this.facetBy]!!
                val ab = frame.groupBy(x).groups.toList()
                if (ab.size != 2)
                    throw IllegalArgumentException()
                val a = ab[0][this.y].toList()
                val b = ab[1][this.y].toList()
                plt += linesLayer(a, b, facet)
            }
        }

        plt
    }
}
