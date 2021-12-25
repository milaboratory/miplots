package com.milaboratory.statplots.util

import org.apache.commons.math3.stat.inference.MannWhitneyUTest
import org.apache.commons.math3.stat.inference.OneWayAnova
import org.apache.commons.math3.stat.inference.TTest
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*

/**
 * A formula containing
 *   y - a numeric variable
 *   factor -  a factor with one or multiple levels
 */
data class Formula(
    /** A numeric variable */
    val y: String,
    /** A factor */
    val factor: Factor
)

/**
 * A group of columns representing a factor for grouping
 */
data class Factor(val columnNames: List<String>) {
    internal val arr = columnNames.toTypedArray()
}

fun Factor(vararg columnNames: String) = Factor(columnNames.toList())

/**
 * Reference group
 **/
sealed interface RefGroup {
    fun value(): Any

    companion object {
        internal object All : RefGroup {
            override fun value(): Any = ""
            override fun toString() = "all"
        }

        /** All dataset */
        val all: RefGroup = All

        internal data class RefGroupImpl(val colValues: List<Any?>) : RefGroup {
            override fun value(): Any = colValues.joinToString(",")
            override fun toString() = colValues.joinToString("+")
        }

        /** Select part of dataset for specific values of columns */
        fun of(vararg columnValues: Any): RefGroup = RefGroupImpl(columnValues.toList())

        /** Select part of dataset for specific values of columns */
        fun of(columnValues: List<Any>): RefGroup = RefGroupImpl(columnValues)
    }
}

interface CompareMeansParameters {
    /**
     * A dataframe containing the variables in the formula.
     */
    val data: AnyFrame

    /**
     * The formula
     */
    val formula: Formula

    /**
     * The type of test. Default is Wilcox
     */
    val method: TestMethod

    /**
     * Variables used to group the data set before applying the test.
     * When specified the mean comparisons will be performed in each
     * subset of the data formed by the different levels of the groupBy variables.
     */
    val groupBy: Factor?

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
    override val formula: Formula,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val groupBy: Factor? = null,
    override val pAdjustMethod: PValueCorrection.Method? = null,
    override val refGroup: RefGroup? = null,
) : CompareMeansParameters {

    /** all data array */
    private val allData by lazy {
        data[formula.y].cast<Double>().toDoubleArray()
    }

    /** data array for each group*/
    private val groups: List<Pair<RefGroup, DoubleArray>> by lazy {
        data.groupBy(*formula.factor.arr).groups.toList().map {
            getRefGroup(it, formula.factor.arr) to it[formula.y].cast<Double>().toDoubleArray()
        }
    }

    /** maximal y value */
    val yMax by lazy { allData.maxOrNull() ?: 0.0 }

    /** minimal y value */
    val yMin by lazy { allData.minOrNull() ?: 0.0 }

    /** Method used to compute overall p-value */
    val overallPValueMethod =
        if (method.pairedOnly && groups.map { it.second }.filter { it.size > 2 }.size > 2)
            TestMethod.ANOVA
        else method

    /** Overall p-value computed with one way ANOVA */
    val overallPValue by lazy {
        val datum = groups.map { it.second }.filter { it.size > 2 }
        if (datum.size < 2)
            -1.0
        else
            overallPValueMethod.pValue(*datum.toTypedArray())
    }

    /** Formatted [overallPValue] */
    val overallPValueFmt by lazy {
        formatPValue(overallPValue)
    }

    /** List of all "compare means" with no p-Value adjustment */
    private val compareMeansRaw: List<CompareMeansRow> by lazy {
        if (refGroup != null) {
            val groups = this.groups.toMap()

            //  get reference data
            val refData = (
                    if (refGroup == RefGroup.all)
                        allData
                    else
                        groups[refGroup] ?: throw IllegalArgumentException("reference group not found")
                    )

            groups.map { (group, data) ->
                if (group == refGroup)
                    return@map null

                if (refData.size < 2 || data.size < 2)
                    return@map null

                val pValue = method.pValue(refData, data)

                CompareMeansRow(
                    formula.y, method,
                    refGroup, group,
                    pValue, pValue,
                    "", SignificanceLevel.of(pValue)
                );
            }.filterNotNull()
        } else {
            val cmpList = mutableListOf<CompareMeansRow>()

            for (i in groups.indices) {
                for (j in 0 until i) {
                    val iGroup = groups[i]
                    val jGroup = groups[j]

                    if (iGroup.second.size < 2 || jGroup.second.size < 2)
                        continue

                    val pValue = method.pValue(
                        iGroup.second,
                        jGroup.second
                    )

                    cmpList += CompareMeansRow(
                        formula.y, method,
                        iGroup.first, jGroup.first,
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
        val n = nfmt(compareMeansAdj.map { it.pValue }.toList())
        compareMeansAdj.map {
            it.copy(
                pValueFmt = formatPValue(it.pValueAdj)
            )
        }
    }

    /** Compare means statistics */
    val stat by lazy { compareMeansFmt.toDataFrame() }

    private fun getRefGroup(df: AnyFrame, group: Array<String>) = run {
        val f = df.first()
        RefGroup.Companion.RefGroupImpl(group.map { f[it] })
    }
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

private fun nfmt(dl: List<Double>) =
    dl.map { d ->
        d.toString()
            .replace(".", "")
            .toCharArray()
            .indexOfFirst { it != '0' }
    }.maxOrNull() ?: 3


/** Method for calculation of p-value */
enum class TestMethod(val pairedOnly: Boolean, val str: String) {
    TTest(true, "T-test") {
        override fun pValue(vararg arr: DoubleArray): Double {
            if (arr.size != 2)
                throw IllegalArgumentException("more than 2 datasets passed")
            val a = arr[0]
            val b = arr[1]
            return if (a.size != b.size)
                TTest().tTest(a, b)
            else
                TTest().pairedTTest(a, b)
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
    Kruskal(false, "Kruskal-Wallis") {
        override fun pValue(vararg arr: DoubleArray): Double {
            TODO("Not yet implemented")
        }
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

    /** First group */
    val group1: RefGroup,

    /** Second group */
    val group2: RefGroup,

    /** The p-value */
    val pValue: Double,

    /** The adjusted p-value */
    val pValueAdj: Double,

    /** Formatted string for adjusted p-value */
    val pValueFmt: String,

    /** The significance level */
    val pSignif: SignificanceLevel
)
