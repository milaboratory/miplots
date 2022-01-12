package com.milaboratory.statplots.xcontinious

import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.writePDF
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
        facetNrow = 1
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
        facetNrow = 1
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
