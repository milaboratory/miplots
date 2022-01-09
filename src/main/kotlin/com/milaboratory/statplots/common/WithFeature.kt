package com.milaboratory.statplots.common

import com.milaboratory.statplots.xdiscrete.ggBase
import jetbrains.letsPlot.intern.Feature

interface WithFeature {
    fun getFeature(base: ggBase): Feature
}
