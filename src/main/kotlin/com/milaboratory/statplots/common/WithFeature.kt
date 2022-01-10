package com.milaboratory.statplots.common

import com.milaboratory.statplots.xdiscrete.GGBase
import jetbrains.letsPlot.intern.Feature

interface WithFeature {
    fun getFeature(base: GGBase): Feature
}
