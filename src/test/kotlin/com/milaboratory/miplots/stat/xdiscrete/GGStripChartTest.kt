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

package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.stat.xdiscrete.ErrorPlotType.*
import com.milaboratory.miplots.writePDF
import jetbrains.letsPlot.ggsize
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGStripChartTest {
    @Test
    internal fun examples() {
        val plt1 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
        } + addSummary()

        val plt2 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
        ) {
            shape = "supp"
            color = "supp"
            fill = "supp"
        } + statCompareMeans() + addSummary()

        val plt3 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0,
        ) {
            color = "dose"
        } + statCompareMeans() + addSummary(errorPlotType = Crossbar)

        val plt4 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "dose"
        } + addSummary(errorPlotType = ErrorBar)

        val plt5 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "dose"
        } + addSummary(errorPlotType = BoxPlot)


        val plt6 = GGStripChart(
            TestData.toothGrowth,
            x = "dose",
            y = "len",
            size = 3.0
        ) {
            shape = "dose"
            color = "supp"
        } + addSummary(errorPlotType = BoxPlot, color = "black")

        val plt7 = GGStripChart(
            TestData.toothGrowth,
            x = "supp",
            y = "len",
            facetBy = "dose",
            facetNRow = 1,
            size = 3.0
        ) {
            color = "supp"
        } + addSummary(errorPlotType = BoxPlot, color = "black") + ggsize(500, 500)

        val plt8 = GGStripChart(
            TestData.myeloma,
            x = "molecular_group",
            y = "IRF4",
        ) {
            color = "molecular_group"
        } + addSummary(errorPlotType = BoxPlot, color = "black")

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt1,
            plt2,
            plt3,
            plt4,
            plt5,
            plt6,
            plt7,
            plt8
        )
    }
}
