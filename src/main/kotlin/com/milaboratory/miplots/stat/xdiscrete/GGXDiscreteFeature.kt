package com.milaboratory.miplots.stat.xdiscrete

import jetbrains.letsPlot.intern.Feature

interface GGXDiscreteFeature {
    val prepend: Boolean
    fun getFeature(base: GGXDiscrete): Feature
}
