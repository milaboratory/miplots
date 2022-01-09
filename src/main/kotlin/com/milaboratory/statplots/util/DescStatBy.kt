package com.milaboratory.statplots.util

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.math.sqrt


/**
 *
 */
fun descStatBy(
    data: AnyFrame,
    y: String,
    by: List<String>,
) = run {
    val map = mutableMapOf<String, MutableList<Any?>>(
        DescStatByRow::key.name to mutableListOf(),
        DescStatByRow::count.name to mutableListOf(),
        DescStatByRow::q1.name to mutableListOf(),
        DescStatByRow::median.name to mutableListOf(),
        DescStatByRow::q3.name to mutableListOf(),
        DescStatByRow::min.name to mutableListOf(),
        DescStatByRow::max.name to mutableListOf(),
        DescStatByRow::mean.name to mutableListOf(),
        DescStatByRow::std.name to mutableListOf(),
        DescStatByRow::err.name to mutableListOf()
    )
    for (s in by) {
        map.put(s, mutableListOf())
    }

    for (df in data.groupBy(*by.toTypedArray()).groups.toList()) {
        val row = df.first()
        for (s in by) {
            map[s]!!.add(row[s]!!)
        }

        val key = by.map { row[it].toString() }.dfKey()
        val stat = DescriptiveStatistics()
        df[y].convertToDouble().castNotNull().forEach { stat.addValue(it) }

        map[DescStatByRow::key.name]!!.add(key)
        map[DescStatByRow::count.name]!!.add(stat.n)
        map[DescStatByRow::q1.name]!!.add(stat.getPercentile(25.0))
        map[DescStatByRow::median.name]!!.add(stat.getPercentile(50.0))
        map[DescStatByRow::q3.name]!!.add(stat.getPercentile(75.0))
        map[DescStatByRow::min.name]!!.add(stat.min)
        map[DescStatByRow::max.name]!!.add(stat.max)
        map[DescStatByRow::mean.name]!!.add(stat.mean)
        map[DescStatByRow::std.name]!!.add(stat.standardDeviation)
        map[DescStatByRow::err.name]!!.add(stat.standardDeviation / sqrt(stat.n.toDouble()))
    }

    map.toDataFrame().cast<DescStatByRow>()
}

@DataSchema
data class DescStatByRow(
    val key: String,
    val count: Long,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val min: Double,
    val max: Double,
    val mean: Double,
    val std: Double,
    val err: Double,
)


private fun List<String>.dfKey() = this.joinToString("_")

fun DataRow<DescStatByRow>.meanStdErr() = run {
    ErrorPoint(this.mean, this.mean - this.err, this.mean + this.err)
}

fun DataRow<DescStatByRow>.meanStdDev() = run {
    ErrorPoint(this.mean, this.mean - this.std, this.mean + this.std)
}

fun DataRow<DescStatByRow>.meanRange() = run {
    ErrorPoint(this.mean, this.min, this.max)
}

fun DataRow<DescStatByRow>.medIRQ() = run {
    ErrorPoint(this.median, this.q1, this.q3)
}

@DataSchema
data class ErrorPoint(val mid: Double, val lower: Double?, val upper: Double?)

/**
 *
 */
enum class ErrorFun(val fn: DataRow<DescStatByRow>.() -> ErrorPoint) {
    MeanStdErr(DataRow<DescStatByRow>::meanStdErr),
    MeanStdDev(DataRow<DescStatByRow>::meanStdDev),
    MeanRange(DataRow<DescStatByRow>::meanRange),
    MedIRQ(DataRow<DescStatByRow>::medIRQ);

    fun apply(df: DataFrame<DescStatByRow>) = run {
        val m = mutableMapOf<String, MutableList<Any?>>(
            ErrorPoint::mid.name to mutableListOf(),
            ErrorPoint::lower.name to mutableListOf(),
            ErrorPoint::upper.name to mutableListOf(),
        )

        df.rows().forEach { row ->
            val p = row.fn()

            m[ErrorPoint::mid.name]!!.add(p.mid)
            m[ErrorPoint::lower.name]!!.add(p.lower)
            m[ErrorPoint::upper.name]!!.add(p.upper)

            for ((k, v) in row.toMap()) {
                m.computeIfAbsent(k) { mutableListOf() }.add(v)
            }
        }

        m.toDataFrame().cast<ErrorPoint>()
    }
}
