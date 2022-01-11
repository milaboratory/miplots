package com.milaboratory.statplots.xcontinious

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation
import org.apache.commons.math3.stat.ranking.NaturalRanking
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.junit.jupiter.api.Test
import kotlin.math.abs

/**
 *
 */
internal class statCorTest {
    @Test
    internal fun test1() {
        val x = doubleArrayOf(44.4, 45.9, 41.9, 53.3, 44.7, 44.1, 50.7, 45.2, 60.1)
        val y = doubleArrayOf(2.6, 3.1, 2.5, 5.0, 3.6, 4.0, 5.2, 2.8, 3.8)
        println(SpearmansCorrelation().correlation(x, y))
        println("---")


        val rank = NaturalRanking()
        val regression = SimpleRegression()
        val xr = rank.rank(x)
        val yr = rank.rank(y)
        for (i in x.indices) {
            regression.addData(xr[i], yr[i])
        }

        val n = xr.distinct().size.toDouble()
        val r = regression.r
        println(r)
        println(regression.significance)
        val f = 0.5*Math.log((1+ r)/(1- r))
        val z1 = r*Math.sqrt((n-2)/(1-r*r))
        val p1 = 2*(1-TDistribution(n-2).cumulativeProbability(Math.abs(z1)))
        println(p1)
        val z2 = f*Math.sqrt((n-3)/1.06)
        val p2 = 2*NormalDistribution().cumulativeProbability(-abs(z2))
        println(p2)

    }
}
