package com.milaboratory.miplots

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readTSV
import java.io.File

/**
 *
 */
object TestData {
    val myeloma by lazy {
        DataFrame.readTSV(
            File(TestData::class.java.getResource("/Myeloma.tsv")!!.toURI())
        )
    }

    val myelomaMatrix by lazy {
        myeloma.asMatrix(
            "molecular_group", "z", "option",
            "event", "time", "CCND1", "CRIM1", "DEPDC1", "IRF4", "TP53", "WHSC1"
        )
    }

    val toothGrowth by lazy {
        DataFrame.readCSV(TestData::class.java.getResource("/ToothGrowth.csv")!!)
            .convert { column<Double>("dose") }.to<String>()
    }

    val mtcars by lazy {
        DataFrame.readCSV(
            File(TestData::class.java.getResource("/Mtcars.csv")!!.toURI())
        )
    }

    val mtcarsMatrix by lazy {
        mtcars.asMatrix(
            "model", "z", "option",
            "mpg", "cyl", "disp", "hp", "drat", "wt", "qsec", "vs", "am", "gear", "carb"
        )
    }

    val spinrates by lazy {
        DataFrame.readCSV(
            File(TestData::class.java.getResource("/Spinrates.csv")!!.toURI())
        )
    }

    @DataSchema
    data class SampleMatrixRow(
        val x: String,
        val y: String,
        val z: Double,
        val xcat: String,
        val ycat: String,
    )

    fun sampleMatrix(nRow: Int, nCol: Int, xCat: Int = 5, yCat: Int = 5): AnyFrame = run {
        val l = mutableListOf<SampleMatrixRow>()
        val xcat = (0 until nRow).associate { it to "x${(it % xCat)}" }
        val ycat = (0 until nRow).associate { it to "y${(it % xCat)}" }
        for (x in 0 until nRow)
            for (y in 0 until nCol)
                l += SampleMatrixRow("x - $x", "y - $y", 1.0 * x * y, xcat[x]!!, ycat[y]!!)

        l.toDataFrame()
    }

//    val geneUsage by lazy {
//        var data = DataFrame.readCSV(
//            File(TestData::class.java.getResource("/VUsage.csv")!!.toURI())
//        )
//
//        data = data.add("cell_type") {
//            if (it["sample"].toString().contains("CD4"))
//                "CD4"
//            else
//                "CD8"
//        }
//
//        data = data.add("tissue") {
//            if (it["sample"].toString().contains("Spleen"))
//                "Spleen"
//            else
//                "PC"
//        }
//
//        data
//    }
}
