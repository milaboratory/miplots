@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xcontinious

import com.milaboratory.miplots.MiFonts
import com.milaboratory.miplots.formatPValue
import com.milaboratory.miplots.stat.util.KendallsCorrelation
import jetbrains.letsPlot.geom.geomSmooth
import jetbrains.letsPlot.geom.geomText
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

data class CorrelationResult(val rValue: Double, val pValue: Double)

enum class CorrelationMethod {
    Pearson {
        override operator fun invoke(x: DoubleArray, y: DoubleArray): CorrelationResult {
            val matrix = mutableListOf<DoubleArray>()
            for (i in x.indices) {
                matrix += doubleArrayOf(x[i], y[i])
            }
            val corr = PearsonsCorrelation(matrix.toTypedArray())
            return CorrelationResult(
                corr.correlationMatrix.getEntry(0, 1),
                corr.correlationPValues.getEntry(0, 1)
            )
        }
    },
    Kendall {
        override operator fun invoke(x: DoubleArray, y: DoubleArray): CorrelationResult {
            val corr = KendallsCorrelation(x, y)
            return CorrelationResult(
                corr.tau,
                corr.pValue
            )
        }
    },
    Spearman {
        override operator fun invoke(x: DoubleArray, y: DoubleArray): CorrelationResult {
            val r = SpearmansCorrelation().correlation(x, y)
            val n = x.distinct().size.toDouble()
            val f = 0.5 * ln((1 + r) / (1 - r))

//            val z1 = r*Math.sqrt((n-2)/(1-r*r))
//            val p1 = 2*(1- TDistribution(n-2).cumulativeProbability(Math.abs(z1)))

            val z2 = f * sqrt((n - 3) / 1.06)
            val p2 = 2 * NormalDistribution().cumulativeProbability(-abs(z2))

            return CorrelationResult(r, p2)
        }
    };

    abstract operator fun invoke(x: DoubleArray, y: DoubleArray): CorrelationResult

    companion object {
        fun parse(str: String): CorrelationMethod =
            values().find { it.name.lowercase().equals(str.lowercase()) }
                ?: throw IllegalArgumentException("unknown: $str")
    }
}

/**
 *
 */
class statCor(
    val method: CorrelationMethod = CorrelationMethod.Pearson,
    val xLabelPos: Number? = null,
    val yLabelPos: Number? = null,
) {
    fun getFeature(base: GGScatter) = run {
        val xpos = xLabelPos ?: base.xMinMax.first
        val ypos = (yLabelPos ?: base.yMinMax.second).toDouble()
        val data = mutableMapOf<String, MutableList<Any?>>(
            base.x to mutableListOf(),
            base.y to mutableListOf(),
            "label" to mutableListOf()
        )
        if (base.groupBy != null)
            data += base.groupBy to mutableListOf()
        if (base.facetBy != null)
            data += base.facetBy to mutableListOf()


        val yDeltaBase = abs(base.yMinMax.second - base.yMinMax.first) * 0.1
        if (base.groupBy == null && base.facetBy == null) {
            val corr = corr(base, base.data)
            if (corr != null) {
                data[base.x]!!.add(xpos)
                data[base.y]!!.add(ypos)
                data["label"]!!.add("R = ${formatPValue(corr.rValue)}, p = ${formatPValue(corr.pValue)}")
            }
        } else {
            if ((base.groupBy != null) != (base.facetBy != null)) {
                val groupBy = (base.groupBy ?: base.facetBy)!!

                val yDelta = if (base.facetBy != null) 0.0 else yDeltaBase
                val groups = base.data.groupBy(groupBy).groups
                groups.forEachIndexed { idx, g ->
                    val group = g.first()[groupBy]
                    val corr = corr(base, g)
                    if (corr != null) {
                        data[base.x]!!.add(xpos)
                        data[base.y]!!.add(ypos + (groups.size() - idx - 1) * yDelta)
                        data[groupBy]!!.add(group)
                        data["label"]!!.add("R = ${formatPValue(corr.rValue)}, p = ${formatPValue(corr.pValue)}")
                    }
                }
            } else {
                base.data.groupBy(base.facetBy!!).groups.forEach { facetDf ->
                    val facet = facetDf.first()[base.facetBy]
                    val yDelta = yDeltaBase
                    val groups = facetDf.groupBy(base.groupBy!!).groups
                    groups.forEachIndexed { idx, g ->
                        val group = g.first()[base.groupBy]
                        val corr = corr(base, g)
                        if (corr != null) {
                            data[base.x]!!.add(xpos)
                            data[base.y]!!.add(ypos + (groups.size() - idx - 1) * yDelta)
                            data[base.facetBy]!!.add(facet)
                            data[base.groupBy]!!.add(group)
                            data["label"]!!.add("R = ${formatPValue(corr.rValue)}, p = ${formatPValue(corr.pValue)}")
                        }
                    }
                }
            }
        }

        geomSmooth(method = "lm") {
            color = base.aes.color
            linetype = base.aes.linetype
        } + geomText(
            data,
            hjust = 1.0,
            vjust = 1.0,
            showLegend = false,
            family = MiFonts.monospace
        ) {
            label = "label"
            color = base.aes.color
        }
    }

    private fun corr(base: GGScatter, data: AnyFrame) = run {
        val xd = data[base.x].convertToDouble().castToNotNullable().toDoubleArray()
        val yd = data[base.y].convertToDouble().castToNotNullable().toDoubleArray()
        if (xd.size <= 2 || yd.size <= 2)
            null
        else
            method(xd, yd)
    }
}

operator fun GGScatter.plus(s: statCor) = run {
    this.plot += s.getFeature(this)
    this
}

operator fun GGScatter.plusAssign(s: statCor) {
    this.plot += s.getFeature(this)
}
