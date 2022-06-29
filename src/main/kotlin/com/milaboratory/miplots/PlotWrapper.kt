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
