package com.milaboratory.miplots.stat.xdiscrete

import jetbrains.letsPlot.intern.Feature

interface GGXDiscreteFeature {
    fun getFeature(base: GGXDiscrete): Feature
}
