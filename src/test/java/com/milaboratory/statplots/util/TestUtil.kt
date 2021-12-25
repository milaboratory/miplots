package com.milaboratory.statplots.util

import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import kotlin.random.Random
import kotlin.random.asJavaRandom

fun randomDataset(
    vararg cols: Pair<String, Distribution>,
    len: Int = 100,
    random: Random = Random.Default
) = run {
    val datum = cols.map {
        val d = it.second
        it.first to when (d) {
            Normal -> (0 until len).map { 10 * random.nextDouble() }
            Gaussian -> (0 until len).map { 10 * random.asJavaRandom().nextGaussian() }
            is Category -> (0 until len).map { (65 + random.nextInt(d.n)).toChar().toString() }
        }
    }
    datum.toMap().toDataFrame()
}

sealed interface Distribution

data class Category(val n: Int) : Distribution
object Gaussian : Distribution
object Normal : Distribution


//data class
