package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.common.GGAes
import com.milaboratory.statplots.common.GGBase
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.elementBlank
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.scale.scaleYDiscrete
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toMap


typealias Order = Comparator<DataRow<*>>

object Ord {
    fun groupBy(col: String): Order = Comparator.comparing { it[col].toString() }
    fun sortBy(col: String): Order = Comparator.comparing { it[col].toString() }
}

/** */
class Heatmap(
    _data: AnyFrame,
    x: String,
    y: String,
    val z: String,
    val xOrder: Order? = null,
    val yOrder: Order? = null,
    facetBy: String? = null,
    facetNCol: Int? = null,
    facetNrow: Int? = null,
    color: String? = null,
    fill: String? = null,
    val shape: Any? = null,
    val size: Number? = null,
    val alpha: Double? = null,
    aesMapping: GGAes.() -> Unit = {}
) : GGBase(
    x = x,
    y = y,
    facetBy = facetBy,
    facetNCol = facetNCol,
    facetNrow = facetNrow,
    color = color,
    fill = fill,
    aesMapping = aesMapping
) {
    override val groupBy = null
    override val data = _data

    internal val xax: List<Any>
    internal val yax: List<Any>

    init {
        xax = if (xOrder != null)
            data.rows()
                .asSequence()
                .distinctBy { it[x] }
                .sortedWith(xOrder)
                .mapNotNull { it[x] }
                .toList()
        else
            data[x].distinct().toList().filterNotNull()

        yax = if (yOrder != null)
            data.rows()
                .distinctBy { it[y] }
                .sortedWith(yOrder)
                .mapNotNull { it[y] }
                .toList()
        else
            data[y].distinct().toList().filterNotNull()
    }

    override var plot = run {
        var plt = letsPlot(data.toMap()) {
            this.x = x
            this.y = y
            this.fill = z
        }

        plt += geomTile(width = 0.9, height = 0.9)
        plt += coordFixed()

        plt += scaleXDiscrete(
            breaks = xax, limits = xax,
            labels = xax.map { it.toString() },
            expand = listOf(0.0, 0.1),
        )
        plt += scaleYDiscrete(
            breaks = yax, limits = yax,
            labels = yax.map { it.toString() },
            expand = listOf(0.0, 0.1)
        )

//        plt += xlab("") + ylab("")

        plt += theme(
            axisLineY = elementBlank(),
            axisLineX = elementBlank(),
//            axisLine = elementLine(),
            panelGrid = elementBlank(),
            axisTicksX = elementBlank(),
            axisTicksY = elementBlank(),
            axisTitleX = elementBlank(),
            axisTitleY = elementBlank()
        ).legendPositionBottom()

        plt
    }
}
