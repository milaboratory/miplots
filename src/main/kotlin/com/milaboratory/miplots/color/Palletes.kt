package com.milaboratory.miplots.color


/**
 *
 */
object Palletes {
    object Categorical {
        val Triadic9Bright = DiscretePallete(
            "#42b842", "#845cff", "#ff9429",
            "#27c2c2", "#e553e5", "#95c700",
            "#2d93fa", "#f05670", "#929bad"
        )

        val Triadic9Dark = DiscretePallete(
            "#198020", "#5f31cc", "#c36a27",
            "#068a94", "#a324b2", "#659406",
            "#105bcc", "#ad3757", "#5e5e70"
        )
    }

    object Diverging {
        val viridis2magma = GradientBasePallete(
            "#4a005c", "#4A2F7F", "#3F5895",
            "#3181A0", "#28A8A0", "#3ECD8D",
            "#86E67B", "#CEF36C", "#FFEA80",
            "#FED470", "#FDA163", "#F36C5A",
            "#D64470", "#A03080", "#702084",
            "#451777", "#2B125C"
        )

        val lime90rose130 = GradientBasePallete(
            "#2E5C00", "#49850D", "#6FA33B",
            "#8FC758", "#ABDB7B", "#C5EBA0",
            "#DCF5C4", "#FFFFFF", "#FADCF5",
            "#F5C4ED", "#F0A3E3", "#E573D2",
            "#CC49B6", "#991884", "#701260"
        )
    }
}
