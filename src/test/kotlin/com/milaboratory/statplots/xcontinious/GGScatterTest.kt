package com.milaboratory.statplots.xcontinious

import com.milaboratory.statplots.common.plus
import com.milaboratory.statplots.util.TestData
import com.milaboratory.statplots.util.writePDF
import jetbrains.letsPlot.geom.geomSmooth
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class GGScatterTest {

    @Test
    internal fun test1() {

        var gg = geomSmooth()
        val plt = GGScatter(
            TestData.mtcars,
            x = "wt",
            y = "mpg",
            color = "black",
            shape = 21,
            size = 3,
            fill = "white"
        ) + gg



        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt
        )
    }
}
