package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.util.PValueCorrection
import com.milaboratory.statplots.util.RefGroup
import com.milaboratory.statplots.util.TestMethod
import jetbrains.letsPlot.GGBunch
import jetbrains.letsPlot.elementText
import jetbrains.letsPlot.ggsize
import jetbrains.letsPlot.label.ggtitle
import jetbrains.letsPlot.scale.scaleFillDiscrete
import jetbrains.letsPlot.scale.scaleXContinuous
import jetbrains.letsPlot.scale.xlim
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.*

/**
 *
 */
class BoxPlotFacets(
    override val data: AnyFrame,
    override val x: String,
    override val y: String,
    val facet: String,
    val ncol: Int? = null,
    override val group: String? = null,
    override val showOverallPValue: Boolean? = null,
    override val comparisons: List<Pair<String, String>>? = null,
    override val allComparisons: Boolean? = null,
    override val refGroup: RefGroup? = null,
    override val hideNS: Boolean = false,
    override val labelFormat: LabelFormat = LabelFormat.Significance,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = null,
) : BoxPlotParameters {

    val plot = run {
        val width = 400
        val height = 400

        val xlim = data[x].distinct().toList().filterNotNull()
        val ycol = data[y].convertToDouble()
        val ymin = ycol.min()
        val ymax = ycol.max()

        val plots = this@BoxPlotFacets.data
            .groupBy(facet)
            .groups.toList()
            .map {
                it.first()[facet] to it
            }
            .map { (gName, gData) ->
                BoxPlot(
                    gData, x, y,
                    _yMin = ymin,
                    _yMax = ymax,
                    _xLim = xlim,
                    showOverallPValue = showOverallPValue,
                    comparisons = comparisons,
                    allComparisons = allComparisons,
                    refGroup = refGroup,
                    hideNS = hideNS,
                    labelFormat = labelFormat,
                    method = method,
                    multipleGroupsMethod = multipleGroupsMethod,
                    pAdjustMethod = pAdjustMethod,
                ).plot +
                        xlim(xlim) +
                        scaleFillDiscrete(breaks = xlim, limits = xlim) +
                        ggsize(width, height) +
                        ggtitle("$facet = $gName")
            }.toList()


        var bunch = GGBunch()
        val ncol = this.ncol ?: plots.size
        for (i in plots.indices) {
            val col = i % ncol
            val row = i / ncol
            bunch = bunch.addPlot(plots[i], col * width, row * height)
        }

        bunch
    }
}
