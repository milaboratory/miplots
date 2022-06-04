package com.milaboratory.miplots.stat.xdiscrete

import com.milaboratory.miplots.stat.util.StatFun
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.xlab
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.toMap

object StandardPlots {
    enum class PlotType {
        BoxPlot {
            override fun plot(
                data: AnyFrame, y: String,
                primaryGroup: String?,
                secondaryGroup: String?
            ): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    GGBoxPlot(data, x = primaryGroup, y = y, width = 0.8) {
                        fill = secondaryGroup ?: primaryGroup
                    }.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomBoxplot() + theme(axisTextX = elementBlank()) + xlab(""))
            }
        },

        BoxPlotBinDot {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGBoxPlot(data, x = primaryGroup, y = y, width = 0.8) {
                        color = secondaryGroup ?: primaryGroup
                    }
                    plt.append(ggDot(
                        position = positionDodge(width = 0.8)
                    ) {
                        fill = secondaryGroup ?: primaryGroup
                    })
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomBoxplot() + geomYDotplot(
                    fill = "black",
                    color = "black"
                ) + theme(axisTextX = elementBlank()) + xlab(""))
            }
        },

        BoxPlotJitter {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGBoxPlot(data, x = primaryGroup, y = y, width = 0.8, outlierSize = 0) {
                        color = secondaryGroup ?: primaryGroup
                    }
                    plt.append(ggStrip(
                        position = positionJitterDodge(dodgeWidth = 0.8, jitterWidth = 0.2),
                    ) {
                        color = secondaryGroup ?: primaryGroup
                        shape = secondaryGroup ?: primaryGroup
                    })
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomBoxplot() + geomPoint(
                    fill = "black",
                    color = "black",
                    position = positionJitter(width = 0.2)
                ) + theme(axisTextX = elementBlank()) + xlab(""))

            }
        },


        Violin {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGViolinPlot(data, x = primaryGroup, y = y, width = 0.8) {
                        fill = secondaryGroup ?: primaryGroup
                    }
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomViolin() + theme(axisTextX = elementBlank()) + xlab(""))

            }
        },


        ViolinBinDot {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGViolinPlot(data, x = primaryGroup, y = y, width = 0.8) {
                        color = secondaryGroup ?: primaryGroup
                    }
                    plt.append(ggDot(
                        position = positionDodge(width = 0.8)
                    ) {
                        fill = secondaryGroup ?: primaryGroup
                    })
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomViolin() + geomYDotplot(
                    fill = "black",
                    color = "black"
                ) + theme(axisTextX = elementBlank()) + xlab(""))

            }
        },

        BarPlot {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGBarPlot(data, x = primaryGroup, y = y, width = 0.8, position = positionDodge()) {
                        fill = secondaryGroup ?: primaryGroup
                    }
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomBar() + theme(axisTextX = elementBlank()) + xlab(""))
            }
        },

        StackedBarPlot {
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup?.let { primaryGroup ->
                    val plt = GGBarPlot(data, x = primaryGroup, y = y, width = 0.8) {
                        fill = secondaryGroup ?: primaryGroup
                    }
                    plt.plot
                } ?: (ggplot(data.toMap()) {
                    this.y = y
                } + geomBar() + theme(axisTextX = elementBlank()) + xlab(""))
            }
        },

        Line {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup ?: throw IllegalArgumentException("primary group must be not null")

                val plt = GGLinePlot(
                    data, x = primaryGroup,
                    y = y,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                )
                plt.plot
            }
        },

        LineJitter {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup ?: throw IllegalArgumentException("primary group must be not null")

                val plt = GGLinePlot(
                    data, x = primaryGroup,
                    y = y,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                ) + ggStrip(position = positionJitter(width = 0.2)) {
                    color = secondaryGroup
                }
                plt.plot
            }
        },

        LineBinDot {
            override val noGroupingAllowed = false
            override fun plot(data: AnyFrame, y: String, primaryGroup: String?, secondaryGroup: String?): Plot = run {
                primaryGroup ?: throw IllegalArgumentException("primary group must be not null")

                val plt = GGLinePlot(
                    data, x = primaryGroup,
                    y = y,
                    statFun = StatFun.MeanStdErr
                ) {
                    color = secondaryGroup
                } + addSummary(
                    statFun = StatFun.MeanStdErr,
                    errorPlotType = ErrorPlotType.ErrorBar
                ) + ggDot(stackGroups = true) {
                    fill = secondaryGroup ?: primaryGroup
                }
                plt.plot
            }
        },

        ;

        internal open val noGroupingAllowed = true
        abstract fun plot(
            data: AnyFrame, y: String,
            primaryGroup: String? = null,
            secondaryGroup: String? = null
        ): Plot
    }
}