package com.milaboratory.statplots.heatmap

import com.milaboratory.statplots.util.toPDF
import com.milaboratory.statplots.util.writePDF
import jetbrains.datalore.plot.base.scale.ScaleUtil
import jetbrains.letsPlot.scale.scaleColorContinuous
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Paths

/**
 *
 */
internal class Heatmap2Test {

    @Test
    internal fun test1() {

        val data = mapOf(
            "s" to listOf("C", "A", "A", "C", "A", "A", "C", "A", "A"),
            "x" to listOf("C", "BASASASASASASASASAS", "A", "C", "B", "A", "C", "B", "A"),

            "y" to listOf("Z", "Y", "X", "Y", "X", "Z", "X", "Y", "Z"),
            "z" to listOf(2, 3, 4, 5, 6, 7, 8, 1, 2),
        ).toDataFrame()


        val plt = Heatmap2(
            data, "x", "y", "z",
            xOrder = Order.comparing { it["s"].toString() },
            yOrder = Order.comparing { it["y"].toString() }
        )

        plt.data.print()

        writePDF(
            Paths.get("scratch/bp.pdf"),
            plt.toPDF()
        )
    }
}
