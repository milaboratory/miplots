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
package com.milaboratory.miplots.stat.xcontinious

import com.milaboratory.miplots.TestData
import com.milaboratory.miplots.writePDF
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGScatterTest {
    fun plain() = GGScatter(
        TestData.mtcars,
        x = "wt",
        y = "mpg",
        shape = 21,
        size = 3,
    ) + statCor(xLabelPos = 4)

    fun groupBy() = GGScatter(
        TestData.mtcars.convert { column<Double>("cyl") }.to<String>(),
        x = "wt",
        y = "mpg",
        shape = 21,
        size = 3,
    ) {
        color = "cyl"
        fill = "cyl"
    } + statCor(xLabelPos = 4)

    fun facetBy() = GGScatter(
        TestData.mtcars.convert { column<Double>("cyl") }.to<String>(),
        x = "wt",
        y = "mpg",
        shape = 21,
        size = 3,
        facetBy = "cyl",
        facetNRow = 1
    ) + statCor(xLabelPos = 4)

    fun facetByGroupBy() = GGScatter(
        TestData.mtcars
            .convert { column<Double>("cyl") }.to<String>()
            .convert { column<Double>("carb") }.to<String>(),
        x = "wt",
        y = "mpg",
        shape = 21,
        size = 3,
        facetBy = "cyl",
        facetNRow = 1
    ) {
        color = "carb"
    } + statCor(xLabelPos = 4)

    @Test
    internal fun test1() {
        writePDF(
            Paths.get("scratch/bp.pdf"),
            plain(),
            groupBy(),
            facetBy(),
            facetByGroupBy()
        )
    }
}
