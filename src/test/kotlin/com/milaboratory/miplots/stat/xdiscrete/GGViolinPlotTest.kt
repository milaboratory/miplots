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
package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.positionDodge
import jetbrains.letsPlot.positionJitterDodge
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class GGViolinPlotTest {
    @Test
    internal fun examples() {
        val plt1 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
        ) {
            shape = "dose"
        }

        val plt2 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            drawQuantiles = listOf(0.5)
        ) {
            shape = "dose"
        }

        val plt3 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
            drawQuantiles = listOf(0.5)
        ) {
            shape = "dose"
        }.append(ggBox(width = 0.1, outlierShape = "8", outlierColor = "black", outlierSize = 4) {
            fill = "dose"
        })

        val plt4 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
            drawQuantiles = listOf(0.5)
        ) {
            shape = "dose"
        } + ggStrip(position = positionJitterDodge(jitterWidth = 0.1))

        val plt5 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
        ) {
            shape = "dose"
        } + ggStrip(position = positionJitterDodge(jitterWidth = 0.1)) + addSummary(errorPlotType = ErrorPlotType.PointRange)

        val plt6 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
        ) {
            shape = "dose"
        }.append(
            addSummary(
                statFun = StatFun.MeanStdErr,
                errorPlotType = ErrorPlotType.Crossbar, width = 0.1
            )
        )

        val plt7 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
        ) {
            shape = "dose"
            color = "dose"
        }.append(ggBox(width = 0.1))

        val plt8 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
        ) {
            shape = "dose"
            fill = "dose"
        }.append(ggBox(width = 0.1, fill = "white"))

        val plt9 = GGViolinPlot(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            trim = false,
        ) {
//            color = "supp"
        }.append(ggBox(width = 0.1, position = positionDodge(width = 1.0)))

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
            plt3,
            plt4,
            plt5,
            plt6,
            plt7,
            plt8,
            plt9
        )
    }
}