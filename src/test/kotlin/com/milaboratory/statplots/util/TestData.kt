package com.milaboratory.statplots.util

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readTSV
import java.io.File

/**
 *
 */
class TestData {
    companion object {
        val mieloma by lazy {
            DataFrame.readTSV(
                File(javaClass.getResource("/Mieloma.tsv")!!.toURI())
            )
        }

        val toothGrowth by lazy {
            DataFrame.readCSV(javaClass.getResource("/ToothGrowth.csv")!!)
                .convert { column<Double>("dose") }.to<String>()
        }
    }
}
