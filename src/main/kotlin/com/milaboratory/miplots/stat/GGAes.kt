package com.milaboratory.miplots.stat

import jetbrains.datalore.plot.base.Aes

/**
 * @param fill fill color
 * @param color outline color
 * @param shape shape of the points
 * @param linetype lone type
 * @param size size or the points
 * @param width line width
 * @param alpha color alpha
 * */
open class GGAes(
    var fill: String? = null,
    var color: String? = null,
    var shape: String? = null,
    var linetype: String? = null,
    var size: String? = null,
    var width: String? = null,
    var alpha: String? = null
) {

    fun inherit(oth: GGAes) {
        if (fill == null) fill = oth.fill
        if (color == null) color = oth.color
        if (shape == null) shape = oth.shape
        if (linetype == null) linetype = oth.linetype
        if (size == null) size = oth.size
        if (width == null) width = oth.width
        if (alpha == null) alpha = oth.alpha
    }

    fun inheritColors(oth: GGAes) {
        if (fill == null) fill = oth.fill
        if (color == null) color = oth.color
    }
}

abstract class WithAes(
    color: String? = null,
    fill: String? = null,
    shape: Any? = null,
    linetype: String? = null,
    size: Number? = null,
    width: Double? = null,
    alpha: Double? = null,
    aesMapping: GGAes.() -> Unit
) {
    val aes = GGAes().apply(aesMapping)
    var color = if (aes.color != null) null else color
    var fill = if (aes.fill != null) null else fill
    var shape = if (aes.shape != null) null else shape
    var linetype = if (aes.linetype != null) null else linetype
    var size = if (aes.size != null) null else size
    var width = if (aes.width != null) null else width
    var alpha = if (aes.alpha != null) null else alpha

    fun inherit(other: WithAes) {
        aes.inherit(other.aes)
        if (color == null) color = other.color
        if (fill == null) fill = other.fill
        if (shape == null) shape = other.shape
        if (linetype == null) linetype = other.linetype
        if (size == null) size = other.size
        if (width == null) width = other.width
        if (alpha == null) alpha = other.alpha
    }

    fun inheritColors(other: WithAes) {
        aes.inheritColors(other.aes)
        if (color == null) color = other.color
        if (fill == null) fill = other.fill
    }
}
