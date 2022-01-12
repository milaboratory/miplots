package com.milaboratory.statplots.common

import com.milaboratory.statplots.xdiscrete.GGXDiscrete
import jetbrains.letsPlot.intern.Feature

interface WithFeature {
    fun getFeature(base: GGXDiscrete): Feature
}
