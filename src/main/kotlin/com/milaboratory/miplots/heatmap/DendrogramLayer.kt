package com.milaboratory.miplots.heatmap

import com.milaboratory.miplots.Position
import com.milaboratory.miplots.dendro.geomDendro
import com.milaboratory.miplots.isLeftRight
import com.milaboratory.miplots.isTopBottom

fun Heatmap.withDendrogram(
    pos: Position,
    sep: Double = 0.0,
    linewidth: Double? = 1.5 * defBorderWidth,
) = run {
    if ((pos == Position.Left || pos == Position.Right) && yclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")
    if ((pos == Position.Top || pos == Position.Bottom) && xclust == null)
        throw IllegalArgumentException("Should use hierarchical ordering for adding dendro layer")

    if (
        (pos.isTopBottom && xax.size <= 1)
        || (pos.isLeftRight && yax.size <= 1)
    )
        return@run this

    val h = heigh / 5
    val w = width / 5
    val pdata = posData(pos, h, w, sep)
    val clust = clust(pos.ax)!!
    val dheight = if (pos.isTopBottom) h else w
    val rshift = when (pos) {
        Position.Top -> ymax + dheight
        Position.Right -> xmax + dheight
        Position.Bottom -> ymin - dheight
        Position.Left -> xmin - dheight
    }

    val feature = geomDendro(
        clust,
        rpos = pos,
        showNodes = false,
        balanced = true,
        rshift = rshift,
        coord = axcoord(pos.ax),
        height = dheight,
        color = "black",
        fill = "black",
        linetype = 1,
        linewidth = linewidth,
        linewidthY = linewidth,
        linecolor = "black",
    )

    layers += HLayer(
        position = pos,
        xmin = pdata.lxmin, xmax = pdata.lxmax,
        ymin = pdata.lymin, ymax = pdata.lymax,
        feature = feature.feature
    )

    this
}
