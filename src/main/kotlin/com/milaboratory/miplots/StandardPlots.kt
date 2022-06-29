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
package com.milaboratory.miplots

import com.milaboratory.miplots.stat.GGBase
import com.milaboratory.miplots.stat.util.StatFun
import com.milaboratory.miplots.stat.xcontinious.GGScatter
import com.milaboratory.miplots.stat.xdiscrete.*
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.xlab
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.toMap

object StandardPlots {
    enum class PlotType(val cliName: String) {
        BoxPlot("boxplot") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?): Plot = (ggplot(data.toMap()) {
                this.y = y
            } + geomBoxplot() + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                GGBoxPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    facetBy = facetBy,
                    width = 0.8
                ) {
                    fill = secondaryGroup ?: primaryGroup
                }
            }
        },

        BoxPlotBinDot("boxplot-bindot") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomBoxplot() + geomYDotplot(
                fill = "black",
                color = "black"
            ) + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGBoxPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8
                ) {
                    color = secondaryGroup ?: primaryGroup
                }
                plt.append(ggDot(
                    position = positionDodge(width = 0.8)
                ) {
                    fill = secondaryGroup ?: primaryGroup
                })

                plt
            }
        },

        BoxPlotJitter("boxplot-jitter") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomBoxplot() + geomPoint(
                fill = "black",
                color = "black",
                position = positionJitter(width = 0.2)
            ) + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGBoxPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8,
                    outlierSize = 0,
                ) {
                    color = secondaryGroup ?: primaryGroup
                }
                plt.append(ggStrip(
                    position = positionJitterDodge(dodgeWidth = 0.8, jitterWidth = 0.2),
                ) {
                    color = secondaryGroup ?: primaryGroup
                    shape = secondaryGroup ?: primaryGroup
                })
                plt
            }
        },


        Violin("violin") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomViolin() + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGViolinPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8
                ) {
                    fill = secondaryGroup ?: primaryGroup
                }
                plt
            }
        },


        ViolinBinDot("violin-bindot") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomViolin() + geomYDotplot(
                fill = "black",
                color = "black"
            ) + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGViolinPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8
                ) {
                    color = secondaryGroup ?: primaryGroup
                }
                plt.append(ggDot(
                    position = positionDodge(width = 0.8)
                ) {
                    fill = secondaryGroup ?: primaryGroup
                })
                plt
            }
        },

        BarPlot("barplot") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomBar() + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGBarPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8,
                    position = positionDodge()
                ) {
                    fill = secondaryGroup ?: primaryGroup
                }
                plt
            }
        },

        StackedBarPlot("barplot-stacked") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) = (ggplot(data.toMap()) {
                this.y = y
            } + geomBar() + theme(axisTextX = elementBlank()) + xlab(""))

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGBarPlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    width = 0.8
                ) {
                    fill = secondaryGroup ?: primaryGroup
                }
                plt
            }
        },

        Line("lineplot") {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, facetBy: String?) =
                throw IllegalArgumentException("primary group must be not null")

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {

                val plt = GGLinePlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                )
                plt
            }
        },

        LineJitter("lineplot-jitter") {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, facetBy: String?) =
                throw IllegalArgumentException("primary group must be not null")

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {


                val plt = GGLinePlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                ) + ggStrip(position = positionJitter(width = 0.2)) {
                    color = secondaryGroup
                }
                plt
            }
        },

        LineBinDot("lineplot-bindot") {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, facetBy: String?) =
                throw IllegalArgumentException("primary group must be not null")

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGLinePlot(
                    data,
                    x = primaryGroup,
                    y = y,
                    xValues = primaryGroupValues,
                    groupByValues = secondaryGroupValues,
                    facetBy = facetBy,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                ) + ggDot(stackGroups = true) {
                    fill = secondaryGroup ?: primaryGroup
                }
                plt
            }
        },

        Scatter("scatter") {
            override fun plot(data: AnyFrame, y: String, facetBy: String?) =
                throw IllegalArgumentException("primary group must be not null")

            override fun plot(
                data: AnyFrame,
                y: String,
                primaryGroup: String,
                secondaryGroup: String?,
                primaryGroupValues: List<String>?,
                secondaryGroupValues: List<String>?,
                facetBy: String?
            ) = run {
                val plt = GGScatter(
                    data,
                    x = primaryGroup,
                    y = y,
                    facetBy = facetBy
                )

                plt
            }
        }

        ;

        internal open val noGroupingAllowed = true
        abstract fun plot(
            data: AnyFrame,
            y: String,
            facetBy: String? = null
        ): Plot

        abstract fun plot(
            data: AnyFrame,
            y: String,
            primaryGroup: String,
            secondaryGroup: String? = null,
            primaryGroupValues: List<String>? = null,
            secondaryGroupValues: List<String>? = null,
            facetBy: String? = null
        ): GGBase
    }
}