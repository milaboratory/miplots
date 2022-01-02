package com.milaboratory.statplots.boxplot

import com.milaboratory.statplots.util.*
import jetbrains.letsPlot.facet.facetWrap
import jetbrains.letsPlot.geom.geomBoxplot
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.abs

/**
 *
 */
class BoxPlotFacets2(
    _data: AnyFrame,
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

    override val data = _data.fillNA { cols(x, *listOfNotNull(group, facet).toTypedArray()) }.with { NA }
    private val xNumeric = x + "__Numeric"
    private val innerData: AnyFrame
    private val xdis: List<Any?>
    private val xord: Map<Any?, Int>
    private val xnum: Map<Any?, Double>

    init {
        xdis = data[x].distinct().toList()
        xord = xdis.mapIndexed { i, v -> v to i }.toMap()
        xnum = xord.mapValues { (_, v) -> v.toDouble() }
        innerData = data.add(xNumeric) { xnum[it[x]]!! }
    }

    val plot = run {
        var plt = letsPlot(innerData.toMap()) {
            x = this@BoxPlotFacets2.xNumeric
            y = this@BoxPlotFacets2.y
        }

        plt += geomBoxplot {
            fill = this@BoxPlotFacets2.x
        }

        for (group in innerData.groupBy(facet).groups) {
            if (group.isEmpty())
                continue
            val facetValue = group.first()[facet]
            println(facetValue)
            val compareMeans = CompareMeans(
                data = group, x = x, y = y,
                method = method, multipleGroupsMethod = multipleGroupsMethod, pAdjustMethod = pAdjustMethod,
                refGroup = refGroup
            )
            val stat = if (allComparisons == true)
                compareMeans.stat
            else {
                val set = comparisons!!.toSet()
                compareMeans.stat
                    .filter { (group1 to group2) in set }
            }.filter { pSignif != SignificanceLevel.NS }

            /** minmax y */
            val yMin = compareMeans.yMin
            var yMax = compareMeans.yMax
            val yDelta = abs(yMax - yMin) * 0.1
            fun adjustAndGetYMax() = run {
                yMax += yDelta
                yMax
            }

            val mustach = yDelta * 0.2
            val rows = stat.rows().sortedBy { abs(xord[it.group1!!]!! - xord[it.group2]!!) }
            for (row in rows) {
                val yValue = adjustAndGetYMax()
                val gr1 = xnum[row.group1!!]!!
                val gr2 = xnum[row.group2]!!

                plt += geomPath(
                    mapOf(
                        xNumeric to listOf(gr1, gr1, gr2, gr2),
                        y to listOf(yValue - mustach, yValue, yValue, yValue - mustach),
                        facet to listOf(facetValue, facetValue, facetValue, facetValue)
                    ),
                    color = "black"
                ) {
                    x = xNumeric
                    y = this@BoxPlotFacets2.y
                }

                println((gr1 + gr2) / 2.0)
                println(yValue + yDelta / 2)
                println(row.pValueFmt)
                println("--")
                plt += geomText(
                    data = mapOf(
                        xNumeric to listOf((gr1 + gr2) / 2.0),
                        y to listOf(yValue + yDelta / 2),
                        facet to listOf(facetValue),
                        "ll" to listOf(row.pValueFmt)
                    ),
                    //   size = 7
                ) {
                    x = xNumeric
                    y = this@BoxPlotFacets2.y
                    label = "ll"
                }

            }
        }


        plt += facetWrap(facets = facet, nrow = 1)

        plt += scaleXContinuous(breaks = xnum.values.toList(), labels = xnum.keys.toList().map { it.toString() })

        plt
    }
}
