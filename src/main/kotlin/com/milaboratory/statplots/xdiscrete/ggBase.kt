@file:Suppress("ClassName")

package com.milaboratory.statplots.xdiscrete

import com.milaboratory.statplots.common.WithFeature
import com.milaboratory.statplots.util.DescStatByRow
import com.milaboratory.statplots.util.NA
import com.milaboratory.statplots.util.descStatBy
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.xlab
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import java.nio.file.Path
import kotlin.math.abs

open class ggBaseAes(
    var color: String? = null,
    var fill: String? = null,
    var shape: String? = null,
    var size: String? = null,
    var linetype: String? = null,
    var width: String? = null
)

enum class Orientation {
    Vertical,
    Horizontal
}

/**
 *
 */
open class ggBase(
    _data: AnyFrame,
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
    aesMapping: ggBaseAes.() -> Unit = {}
) {
    init {
        if (orientation == Orientation.Horizontal)
            throw UnsupportedOperationException("horizontal orientation is not supported yet")
    }

    val aes = ggBaseAes().apply(aesMapping)

    val data: AnyFrame

    /** whether grouping was applied */
    open val groupBy: String? = null
    protected fun distinctGroupBy(g: String?) = if (g == x) null else g

    // numeric x axis name
    internal val xNumeric = x + "__Numeric"

    // x ordinals
    internal val xord: Map<Any?, Int>

    // x numeric values
    internal val xnum: Map<Any?, Double>

    init {
        if (_data[x].all { it is Double })
            throw IllegalArgumentException("x must be categorical")

        val xdist = _data[x].distinct().toList()
        xord = xdist.mapIndexed { i, v -> v to i }.toMap()
        xnum = xord.mapValues { (_, v) -> v.toDouble() }
        data = _data
            .fillNA { cols(x, *listOfNotNull(facetBy).toTypedArray()) }
            .with { NA }
            .add(xNumeric) { xnum[it[x]]!! }
    }

    /** y column */
    internal val ydata = data[y].convertToDouble()

    /** minmax y */
    internal var yMin = ydata.min()
    internal var yMax = ydata.max()
    internal val yDelta = abs(yMax - yMin) * 0.1

    /** */
    internal fun adjustAndGetYMax() = run {
        yMax += yDelta
        yMax
    }

    /** base plot */
    open var plot = run {
        var plt = letsPlot(data.toMap()) {
            x = this@ggBase.xNumeric
            y = this@ggBase.y
        }

        plt += xlab(x)

        plt += scaleXContinuous(
            breaks = xnum.values.toList(),
            labels = xnum.keys.toList().map { it.toString() })

        if (facetBy != null)
            plt += facetWrap(facets = facetBy, ncol = facetNCol, nrow = facetNrow)

        plt
    }

    /** general cache */
    internal val cache = mutableMapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    val descStat by lazy {
        cache.computeIfAbsent("__descriptiveStatistics__") {
            descStatBy(data, y, listOfNotNull(x, facetBy, groupBy)).add(xNumeric) { xnum[it[x]] }
        } as DataFrame<DescStatByRow>
    }
}

operator fun ggBase.plusAssign(feature: Feature) {
    this.plot += feature
}

operator fun ggBase.plusAssign(feature: WithFeature) {
    this.plot += feature.getFeature(this)
}

operator fun ggBase.plus(feature: Feature) = run {
    this.plot += feature
    this
}

operator fun ggBase.plus(feature: WithFeature) = run {
    this.plot += feature.getFeature(this)
    this
}

fun ggBase.toSpec() = this.plot.toSpec()
fun ggBase.toSvg() = PlotSvgExport.buildSvgImageFromRawSpecs(toSpec())
fun ggBase.toPDF() = com.milaboratory.statplots.util.toPDF(toSvg())
fun ggBase.toEPS() = com.milaboratory.statplots.util.toEPS(this.toSvg())
fun writePDF(destination: Path, vararg plots: ggBase) {
    com.milaboratory.statplots.util.writePDF(destination, plots.toList().map { it.toPDF() })
}
