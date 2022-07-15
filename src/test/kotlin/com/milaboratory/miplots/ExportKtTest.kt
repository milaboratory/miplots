/*
 * Copyright (c) 2014-2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.miplots

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class ExportKtTest {
    @Test
    internal fun name() {
        val fontRegex = """(font:\d+\.\d+E-(\d{2}|[2-9])px)""".toRegex()

        var str = """fill:#000000;font:7.172040739078512E-15px IBM Plex Mono;"""
        Assertions.assertEquals(
            ("fill:#000000;font:0px IBM Plex Mono;"),
            fontRegex.replace(str, "font:0px"),

            )

        str = """fill:#000000;font:7.172040739078512E-2px IBM Plex Mono;"""
        Assertions.assertEquals(
            ("fill:#000000;font:0px IBM Plex Mono;"),
            fontRegex.replace(str, "font:0px"),

            )

        str = """("fill:#000000;font:7px IBM Plex Mono;")."""
        Assertions.assertEquals(
            str,
            fontRegex.replace(str, "font:0px"),
        )

        str = """("fill:#000000;font:7.12E-1px IBM Plex Mono;")."""
        Assertions.assertEquals(
            str,
            fontRegex.replace(str, "font:0px"),
        )
    }
}