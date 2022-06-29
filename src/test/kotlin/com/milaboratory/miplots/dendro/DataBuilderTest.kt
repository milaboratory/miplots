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
package com.milaboratory.miplots.dendro

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DataBuilderTest {
    @Test
    internal fun addRecord() {
        val dataframe = DataBuilder()
        dataframe.add(("A" to 4), ("B" to 4), ("C" to 6), ("D" to 4), ("E" to 4))
        dataframe.add(("A" to 20))
        dataframe.add(("C" to 7), ("D" to 5), ("F" to 4))

        assertEquals(dataframe.result["D"]!![2], 5)
    }
}
