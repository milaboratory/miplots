package com.milaboratory.miplots

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
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

    val toothGrowth by lazy {
        DataFrame.readCSV(TestData::class.java.getResource("/ToothGrowth.csv")!!)
            .convert { column<Double>("dose") }.to<String>()
    }

    val mtcars by lazy {
        DataFrame.readCSV(
            File(TestData::class.java.getResource("/Mtcars.csv")!!.toURI())
        )
    }

    val spinrates by lazy {
        DataFrame.readCSV(
            File(TestData::class.java.getResource("/Spinrates.csv")!!.toURI())
        )
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
