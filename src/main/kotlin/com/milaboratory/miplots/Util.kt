package com.milaboratory.miplots

import jetbrains.letsPlot.elementBlank
import jetbrains.letsPlot.theme
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

private val smallNumberFormat = run {
    val df = DecimalFormat("0.##E0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    df.maximumFractionDigits = 1
    df
}

/**
 * Default formatting for p-value
 */
fun formatPValue(value: Double) =
    if (value.isNaN())
        "NaN"
    else if (abs(value) < 1e-2)
        smallNumberFormat.format(value).replace("E", "e")
    else
        "%.2f".format((1000 * value).roundToInt() / 1000.0)

fun <K, V> Map<K?, V?>.filterNotNull(): Map<K, V> = this.mapNotNull {
    it.key?.let { key ->
        it.value?.let { value ->
            key to value
        }
    }
}.toMap()

fun themeBlank() = theme(
    axisLineY = elementBlank(),
    axisLineX = elementBlank(),
    axisTextX = elementBlank(),
    axisTextY = elementBlank(),
    panelGrid = elementBlank(),
    axisTicksX = elementBlank(),
    axisTicksY = elementBlank(),
    axisTitleX = elementBlank(),
    axisTitleY = elementBlank()
).legendPositionNone()

fun AnyFrame.asMatrix(
    x: List<String>, xTo: String, xOpName: String,
    y: List<String>, yTo: String, yOpName: String
): AnyFrame = run {

    val result = mapOf(
        xOpName to mutableListOf(),
        xTo to mutableListOf(),
        yOpName to mutableListOf(),
        yTo to mutableListOf<Any?>(),
    )

    for (row in this.rows()) {
        for (xCol in x) {
            for (yCol in y) {
                if (xTo != xOpName)
                    result[xOpName]!!.add(xCol)
                result[xTo]!!.add(row[xCol])
                if (yTo != yOpName)
                    result[yOpName]!!.add(yCol)
                result[yTo]!!.add(row[yCol])
            }
        }
    }
    result.toDataFrame()
}

fun AnyFrame.asMatrix(
    x: String,
    to: String,
    option: String,
    vararg columns: String
): AnyFrame = this.asMatrix(listOf(x), x, x, columns.asList(), to, option)
