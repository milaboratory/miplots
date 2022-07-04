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
            override fun <T> mkMap(objects: List<T?>, loop: Boolean): Map<T?, Color> =
                auto(objects.size).mkMap(objects, loop)
        }
    }

    object Diverging {
        val viridis2magma = GradientBasePallete(
            "#4A005C", "#4A2F7F", "#3F5895",
            "#3181A0", "#28A8A0", "#3ECD8D",
            "#86E67B", "#CEF36C", "#FFF680",
            "#FED470", "#FDA163", "#F36C5A",
            "#D64470", "#A03080", "#702084",
            "#451777", "#2B125C",
            na = "#f0f0f0"
        )

        val limeRose15 = GradientBasePallete(
            "#2E5C00", "#49850D", "#6FA33B",
            "#8FC758", "#ABDB7B", "#C5EBA0",
            "#DCF5C4", "#FFFFFF", "#FADCF5",
            "#F5C4ED", "#F0A3E3", "#E573D2",
            "#CC49B6", "#991884", "#701260",
            na = "#f0f0f0"
        )

        val blueRed15 = GradientBasePallete(
            "#0E0E8F", "#1D23B8", "#3748E5",
            "#647DFA", "#96A7FA", "#C3CCFA",
            "#E1E5FA", "#F0F0F0", "#F9DBDB",
            "#F9BDBD", "#F59393", "#E55C72",
            "#C23665", "#8F1150", "#5C1243",
            na = "#f0f0f0"
        )

        val tealRead15 = GradientBasePallete(
            "#122B5C", "#1A496B", "#1D7C8F",
            "#21A3A3", "#5FC7AB", "#99E0B1",
            "#CEF0CE", "#F0F0F0", "#FAE6D2",
            "#FAC5AA", "#FA9282", "#E55C72",
            "#C23665", "#8F1150", "#5C1243",
            na = "#f0f0f0"
        )

        val softSpectral15 = GradientBasePallete(
            "#43317B", "#3B57A3", "#3390B3",
            "#5DC2B1", "#95DBA5", "#B9EBA0",
            "#DBF5A6", "#F5F5B7", "#FEEA9D",
            "#FFD285", "#FA9B78", "#E55C72",
            "#C23665", "#8F1150", "#5C1243",
            na = "#f0f0f0"
        )
    }

    object Sequential {
        val viridis15 = GradientBasePallete(
            "#FFF680", "#E8F66C", "#C4F16B",
            "#9AEB71", "#70E084", "#43D18A",
            "#2DBD96", "#28A8A0", "#2793A3",
            "#337B9E", "#3B6399", "#424C8F",
            "#4A3584", "#481B70", "#4A005C",
            na = "#f0f0f0"
        )

        val magma15 = GradientBasePallete(
            "#FFF680", "#FFE871", "#FDCD6F",
            "#FEAD66", "#FA935F", "#F57258",
            "#EB555E", "#D64470", "#B83778",
            "#982D82", "#7E2584", "#611B84",
            "#49187A", "#38116B", "#2B125C",
            na = "#f0f0f0"
        )

        val sunset15 = GradientBasePallete(
            "#FFEA80", "#FFD971", "#FFC171",
            "#FFA76C", "#FB8B6F", "#EB7179",
            "#D75F7F", "#C2518D", "#A64392",
            "#8038A4", "#6135A4", "#4735A3",
            "#283A8F", "#013C70", "#003752",
            na = "#f0f0f0"
        )

        val rainbow15 = GradientBasePallete(
            "#FFF780", "#E7FA6F", "#C1FA6A",
            "#9BF56C", "#79F080", "#66E698",
            "#56D7AC", "#50C7C7", "#56B4D7",
            "#6898EB", "#7481FA", "#8769FA",
            "#9450EB", "#9634D6", "#942AAE",
            na = "#f0f0f0"
        )
    }
}
