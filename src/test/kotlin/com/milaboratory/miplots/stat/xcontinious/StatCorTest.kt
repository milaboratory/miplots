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
@file:Suppress("ClassName")

package com.milaboratory.miplots.stat.xcontinious

import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.positionNudge
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation
import org.apache.commons.math3.stat.ranking.NaturalRanking
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.junit.jupiter.api.Test
import java.nio.file.Paths
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
        val f = 0.5 * Math.log((1 + r) / (1 - r))
        val z1 = r * Math.sqrt((n - 2) / (1 - r * r))
        val p1 = 2 * (1 - TDistribution(n - 2).cumulativeProbability(Math.abs(z1)))
        println(p1)
        val z2 = f * Math.sqrt((n - 3) / 1.06)
        val p2 = 2 * NormalDistribution().cumulativeProbability(-abs(z2))
        println(p2)

    }

    @Test
    internal fun etet() {

        var plt = ggplot(
            data = mapOf(
                "x" to listOf(1, 2, 3, 1, 2, 3),
                "y" to listOf(1, 2, 3, 3, 5, 7),
                "g" to listOf("a", "a", "a", "b", "b", "b")
            )
        ) {
            x = "x"
            y = "y"
        } + geomLine {
            color = "g"
        }
//        + geomText(
//            data = mapOf(
//                "x" to listOf(2, 2),
//                "y" to listOf(7, 7),
//                "g" to listOf("a", "b")
//            ),
//            position = Pos.nudge
//        ) {
//            label = "g"
//        }
        plt += geomText(x = 2, y = 7, position = positionNudge(y = 0.1), label = "xxx")
        plt += geomText(x = 2, y = 7, position = positionNudge(y = 0.1), label = "yyy")
        plt += geomText(x = 2, y = 7, position = positionNudge(y = 0.1), label = "zzz")

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
