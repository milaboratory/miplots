package com.milaboratory.statplots.common

import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.Plot

/**
 *
 */
interface PlotWrapper {
    var plot: Plot
}


operator fun PlotWrapper.plusAssign(feature: Feature) {
    this.plot += feature
}

operator fun PlotWrapper.plus(feature: Feature) = run {
    this.plot += feature
    this
}
