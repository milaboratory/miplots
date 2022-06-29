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
package com.milaboratory.miplots.color

import jetbrains.datalore.base.values.Color


/**
 *
 */
object Palettes {
    object Categorical {
        val Triadic27 = DiscretePalette(
            "#99E099", "#42B842", "#198020",
            "#C1ADFF", "#845CFF", "#5F31CC",
            "#FFCB8F", "#FF9429", "#C26A27",
            "#90E0E0", "#27C2C2", "#068A94",
            "#FAAAFA", "#E553E5", "#A324B2",
            "#CBEB67", "#95C700", "#659406",
            "#99CCFF", "#2D93FA", "#105BCC",
            "#FFADBA", "#F05670", "#AD3757",
            "#D3D7E0", "#929BAD", "#5E5E70"
        )

        val Triadic18 = DiscretePalette(
            "#99E099", "#42B842", "#C1ADFF",
            "#845CFF", "#FFCB8F", "#FF9429",
            "#90E0E0", "#27C2C2", "#FAAAFA",
            "#E553E5", "#CBEB67", "#95C700",
            "#99CCFF", "#2D93FA", "#FFADBA",
            "#F05670", "#D3D7E0", "#929BAD"
        )

        val Triadic9Light = DiscretePalette(
            "#99e099", "#c1adff", "#ffcb8f",
            "#99e0e0", "#faaafa", "#cbeb67",
            "#99ccff", "#ffadba", "#d3d7e0"
        )

        val Triadic9Bright = DiscretePalette(
            "#42b842", "#845cff", "#ff9429",
            "#27c2c2", "#e553e5", "#95c700",
            "#2d93fa", "#f05670", "#929bad"
        )

        val Triadic9Dark = DiscretePalette(
            "#198020", "#5f31cc", "#c36a27",
            "#068a94", "#a324b2", "#659406",
            "#105bcc", "#ad3757", "#5e5e70"
        )

        fun auto(ncats: Int) = if (ncats <= 9) {
            Triadic9Bright
        } else if (ncats <= 18)
            Triadic18
        else
            Triadic27

        val auto = object : DiscreteColorMapping {
            override fun <T> mkMap(objects: List<T?>, loop: Boolean): Map<T?, Color> = auto(objects.size).mkMap(objects, loop)
        }
    }

    object Diverging {
        val viridis2magma = GradientBasePallete(
            "#4a005c", "#4A2F7F", "#3F5895",
            "#3181A0", "#28A8A0", "#3ECD8D",
            "#86E67B", "#CEF36C", "#FFEA80",
            "#FED470", "#FDA163", "#F36C5A",
            "#D64470", "#A03080", "#702084",
            "#451777", "#2B125C",
            na = "#f0f0f0"
        )

        val lime90rose130 = GradientBasePallete(
            "#2E5C00", "#49850D", "#6FA33B",
            "#8FC758", "#ABDB7B", "#C5EBA0",
            "#DCF5C4", "#FFFFFF", "#FADCF5",
            "#F5C4ED", "#F0A3E3", "#E573D2",
            "#CC49B6", "#991884", "#701260",
            na = "#f0f0f0"
        )
    }
}
