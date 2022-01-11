package com.milaboratory.statplots.xcontinious

import com.milaboratory.statplots.util.KendallsCorrelation
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation
import org.jetbrains.kotlinx.dataframe.api.castNotNull
import org.jetbrains.kotlinx.dataframe.api.convertToDouble
import org.jetbrains.kotlinx.dataframe.api.toDoubleArray
import kotlin.math.abs


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
            val f = 0.5 * Math.log((1 + r) / (1 - r))

//            val z1 = r*Math.sqrt((n-2)/(1-r*r))
//            val p1 = 2*(1- TDistribution(n-2).cumulativeProbability(Math.abs(z1)))

            val z2 = f * Math.sqrt((n - 3) / 1.06)
            val p2 = 2 * NormalDistribution().cumulativeProbability(-abs(z2))

            return CorrelationResult(r, p2)
        }
    };

    abstract operator fun invoke(x: DoubleArray, y: DoubleArray): CorrelationResult
}

/**
 *
 */
class statCor(
    val method: CorrelationMethod = CorrelationMethod.Pearson,
    val xLabelPos: Number? = null,
    val yLabelPos: Number? = null,
) {
    fun apply(base: GGScatter) {
        val x = base.data[base.x].convertToDouble().castNotNull().toDoubleArray()
        val y = base.data[base.y].convertToDouble().castNotNull().toDoubleArray()

        val corr = method(x, y)

        val xpos = xLabelPos ?: 0.0
        val ypos = yLabelPos ?: y.maxOrNull() ?: 0.0

        geomText(x = xpos, y = yLabelPos){

        }

    }
}
