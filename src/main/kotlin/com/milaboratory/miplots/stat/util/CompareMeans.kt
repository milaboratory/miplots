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
package com.milaboratory.miplots.stat.util

import com.milaboratory.miplots.formatPValue
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

interface CompareMeansOptions {
    /**
     * The type of test. Default is Wilcox
     */
    val method: TestMethod

    /**
     * A logical indicating whether a paired test should be performed. Used only in T-test and in Wilcox
     */
    val paired: Boolean

    /**
     * The type of test for multiple groups. Default is Kruskal-Wallis
     */
    val multipleGroupsMethod: TestMethod

    /**
     * Method for adjusting p values (null for no adjustment). Default is Bonferroni.
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

data class CompareMeansOptionsCapsule(
    override val method: TestMethod,
    override val paired: Boolean,
    override val multipleGroupsMethod: TestMethod,
    override val pAdjustMethod: PValueCorrection.Method?,
    override val refGroup: RefGroup?
) : CompareMeansOptions {
    constructor(oth: CompareMeansOptions) : this(
        oth.method,
        oth.paired,
        oth.multipleGroupsMethod,
        oth.pAdjustMethod,
        oth.refGroup
    )
}

class CompareMeans(
    /** A dataframe containing the variables in the formula. */
    val data: AnyFrame,
    /** Y value */
    val y: String,
    /** X category */
    val x: String,
    override val method: TestMethod = TestMethod.Wilcoxon,
    override val paired: Boolean = false,
    override val multipleGroupsMethod: TestMethod = TestMethod.KruskalWallis,
    override val pAdjustMethod: PValueCorrection.Method? = PValueCorrection.Method.Bonferroni,
    override val refGroup: RefGroup? = null,
) : CompareMeansOptions {

    init {
        if (paired && !method.supportPaired) {
            throw IllegalArgumentException("${method.str} does not support paired test")
        }
    }

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

    /** Method used to compute overall p-value */
    val overallPValueMethod =
        if (!method.multipleGroups && groups.size > 2)
            multipleGroupsMethod
        else method

    /** Overall p-value computed with one way ANOVA */
    val overallPValue by lazy {
        val datum = groups.values
        if (datum.size < 2)
            -1.0
        else
            overallPValueMethod.pValue(*datum.toTypedArray(), paired = false)
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

                val pValue = method.pValue(refData, data, paired = paired)

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
                        groups[jGroup]!!,
                        paired = paired
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

    private fun getGroup(df: AnyFrame) = df.first()[x] ?: NA
}

const val NA = "NA"

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
