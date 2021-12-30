package com.milaboratory.statplots.util

import org.apache.commons.math3.stat.inference.MannWhitneyUTest
import org.apache.commons.math3.stat.inference.OneWayAnova
import org.apache.commons.math3.stat.inference.TTest
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*

/**
 * Reference group
 **/
data class RefGroup internal constructor(val value: String?) {
    companion object {
        /** All dataset */
        val all = RefGroup(null)

        /** Select part of dataset for specific values of columns */
        fun of(value: String): RefGroup = RefGroup(value)
    }
}

interface CompareMeansParameters {
    /**
     * A dataframe containing the variables in the formula.
     */
    val data: AnyFrame

    /**
     * Y value
     */
    val y: String

    /**
     * X category
     */
    val x: String

    /**
     * The type of test. Default is Wilcox
     */
    val method: TestMethod

    /**
     * The type of test for multiple groups. Default is Kruskal-Wallis
     */
    val multipleGroupsMethod: TestMethod

    /**
     * Method for adjusting p values. Default is Holm.
     */
    val pAdjustMethod: PValueCorrection.Method?

    /**
     * The reference group. If specified, for a given grouping variable, each of the
     * group levels will be compared to the reference group (i.e. control group).
     * refGroup can be also “all”. In this case, each of the grouping variable levels
     * is compared to all (i.e. base-mean).
     */
    val refGroup: RefGroup?
}

class CompareMeans(
    override val data: AnyFrame,
    override val x: String,
    override val y: String,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = null,
    override val refGroup: RefGroup? = null,
) : CompareMeansParameters {

    /** all data array */
    private val allData by lazy {
        data[y].cast<Double>().toDoubleArray()
    }

    /** data array for each group*/
    private val groups: Map<Any, DoubleArray> by lazy {
        data.groupBy(x)
            .groups.toList()
            .map {
                getGroup(it) to it[y].cast<Double>().toDoubleArray()
            }
            .filter { it.second.size > 2 }
            .toMap()
    }

    /** maximal y value */
    val yMax by lazy { allData.maxOrNull() ?: 0.0 }

    /** minimal y value */
    val yMin by lazy { allData.minOrNull() ?: 0.0 }

    /** Method used to compute overall p-value */
    val overallPValueMethod =
        if (method.pairedOnly && groups.size > 2)
            multipleGroupsMethod
        else method

    /** Overall p-value computed with one way ANOVA */
    val overallPValue by lazy {
        val datum = groups.values
        if (datum.size < 2)
            -1.0
        else
            overallPValueMethod.pValue(*datum.toTypedArray())
    }

    /** Formatted [overallPValue] */
    val overallPValueFmt by lazy {
        formatPValue(overallPValue)
    }

    /** Significance for [overallPValue] */
    val overallPValueSign by lazy {
        SignificanceLevel.of(overallPValue)
    }

    /** List of all "compare means" with no p-Value adjustment */
    private val compareMeansRaw: List<CompareMeansRow> by lazy {
        if (refGroup != null) {
            val refGroup = this@CompareMeans.refGroup.value

            //  get reference data
            val refData = (
                    if (refGroup == null)
                        allData
                    else
                        groups[refGroup]
                            ?: throw IllegalArgumentException("reference group not found")
                    )

            groups.map { (group, data) ->
                if (group == refGroup)
                    return@map null

                if (refData.size < 2 || data.size < 2)
                    return@map null

                val pValue = method.pValue(refData, data)

                CompareMeansRow(
                    y, method,
                    refGroup, group,
                    pValue, pValue,
                    "", SignificanceLevel.of(pValue)
                );
            }.filterNotNull()
        } else {
            val cmpList = mutableListOf<CompareMeansRow>()

            val gKeys = groups.keys.toList()
            for (i in gKeys.indices) {
                for (j in 0 until i) {
                    val iGroup = gKeys[i]
                    val jGroup = gKeys[j]

                    val pValue = method.pValue(
                        groups[iGroup]!!,
                        groups[jGroup]!!
                    )

                    cmpList += CompareMeansRow(
                        y, method,
                        iGroup, jGroup,
                        pValue, pValue,
                        "", SignificanceLevel.of(pValue)
                    )
                }
            }

            cmpList
        }
    }

    /** Compare means with adjusted p-values and significance */
    private val compareMeansAdj by lazy {
        if (pAdjustMethod == null || compareMeansRaw.isEmpty())
            compareMeansRaw
        else {
            val adjusted =
                PValueCorrection.adjustPValues(compareMeansRaw.map { it.pValue }.toDoubleArray(), pAdjustMethod)

            compareMeansRaw.mapIndexed { i, cmp ->
                cmp.copy(
                    pValueAdj = adjusted[i],
                    pSignif = SignificanceLevel.of(adjusted[i])
                )
            }
        }
    }

    private val compareMeansFmt by lazy {
        compareMeansAdj.map {
            it.copy(
                pValueFmt = formatPValue(it.pValueAdj)
            )
        }
    }

    /** Compare means statistics */
    val stat by lazy { compareMeansFmt.toDataFrame() }

    private fun getGroup(df: AnyFrame) = df.first()[x] ?: throw RuntimeException()
}

enum class SignificanceLevel(val string: String) {
    NS("ns"),
    One("*"),
    Two("**"),
    Three("***");

    companion object {
        fun of(pValue: Double) =
            if (pValue >= 0.05) NS
            else if (pValue < 0.0001) Three
            else if (pValue < 0.001) Two
            else One
    }
}

/** Method for calculation of p-value */
enum class TestMethod(val pairedOnly: Boolean, val str: String) {
    TTest(true, "T-test") {
        override fun pValue(vararg arr: DoubleArray): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return TTest().tTest(a, b)
        }
    },
    Wilcoxon(true, "Wilcoxon") {
        override fun pValue(vararg arr: DoubleArray): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return if (a.size != b.size)
                MannWhitneyUTest().mannWhitneyUTest(a, b)
            else
                WilcoxonSignedRankTest().wilcoxonSignedRankTest(a, b, false)
        }
    },
    ANOVA(false, "Anova") {
        override fun pValue(vararg arr: DoubleArray) =
            OneWayAnova().anovaPValue(arr.toList())

    },
    KruskalWallis(false, "Kruskal-Wallis") {
        override fun pValue(vararg arr: DoubleArray) =
            KruskalWallis().kruskalWallisTest(arr.toList())
    };

    override fun toString(): String {
        return str;
    }

    abstract fun pValue(vararg arr: DoubleArray): Double
}

/**
 * DataFrame row for CompareMeans result
 */
@DataSchema
@Suppress("UNCHECKED_CAST")
data class CompareMeansRow(
    /** The variable used in test */
    val y: String,

    /** Method used */
    val method: TestMethod,

    /** First group (null for comparison against all) */
    val group1: Any?,

    /** Second group */
    val group2: Any,

    /** The p-value */
    val pValue: Double,

    /** The adjusted p-value */
    val pValueAdj: Double,

    /** Formatted string for adjusted p-value */
    val pValueFmt: String,

    /** The significance level */
    val pSignif: SignificanceLevel
)
