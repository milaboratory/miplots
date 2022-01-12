package com.milaboratory.statplots.util

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
    if (abs(value) < 1e-2)
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
