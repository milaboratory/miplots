package com.milaboratory.miplots

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

interface FeatureWrapper {
    val feature: Feature
}

operator fun Plot.plus(feature: FeatureWrapper) = this + feature.feature

operator fun PlotWrapper.plusAssign(feature: FeatureWrapper) {
    this.plot += feature
}

operator fun PlotWrapper.plus(feature: FeatureWrapper) = run {
    this.plot += feature
    this
}
