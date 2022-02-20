@file:Suppress("FunctionName")

package com.milaboratory.miplots.dendro

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private fun Node<*>.xy(y: Double, depth: Int): XYNode {
    return XYNode(this,
        -1.0,
        y,
        depth,
        children.map { it.xy(y + height, depth + 1) }
    )
}

internal fun Node<*>.xy() = xy(0.0, 0)

internal data class XYNode constructor(
    val node: Node<*>,
    val x: Double,
    val y: Double,
    val depth: Int,
    val children: List<XYNode>
) {
    val leftmost get() = children.firstOrNull()
    val rightmost get() = children.lastOrNull()
    val isLeaf get() = children.isEmpty()
    val height get() = node.totalHeight
    val xmin: Double get() = min(x, children.minOfOrNull { it.xmin } ?: 0.0)
    val xmax: Double get() = max(x, children.maxOfOrNull { it.xmax } ?: 0.0)
    val ymin: Double get() = min(y, children.minOfOrNull { it.ymin } ?: 0.0)
    val ymax: Double get() = max(y, children.maxOfOrNull { it.ymax } ?: 0.0)

    private val leftmostRecursive: XYNode? get() = if (isLeaf) this else leftmost?.leftmostRecursive
    private val righttmostRecursive: XYNode? get() = if (isLeaf) this else rightmost?.righttmostRecursive
    val width get() = abs((leftmostRecursive?.x ?: 0.0) - (righttmostRecursive?.x ?: 0.0))
}

internal val XYNode.leafY: Double
    get() =
        if (isLeaf)
            y
        else {
            if (y < leftmost!!.y)
                children.maxOf { it.leafY }
            else {
                children.minOf { it.leafY }
            }
        }

internal fun XYNode.flipY(): XYNode = copy(y = -y, children = children.map { it.flipY() })
internal fun XYNode.flipX(): XYNode = copy(x = -x, children = children.map { it.flipX() })

internal fun XYNode.imposeX(coord: List<Double>, center: Boolean) = imposeX(coord, center, 0).first
internal fun XYNode.imposeX(coord: List<Double>, center: Boolean, iLeaf: Int): Pair<XYNode, Int> =
    if (isLeaf) {
        copy(x = coord[iLeaf]) to (iLeaf + 1)
    } else {

        val newChildren = mutableListOf<XYNode>()
        var nextLeaf = iLeaf
        for (child in children) {
            val (newChild, i) = child.imposeX(coord, center, nextLeaf)
            newChildren.add(newChild)
            nextLeaf = i
        }
        val newLeft = newChildren.first().x
        val newRight = newChildren.last().x

        val newX = if (center) {
            newLeft + (newRight - newLeft) / 2
        } else {
            val oldLeft = children.first().x
            val oldRight = children.last().x
            newLeft + (x - oldLeft) * (newRight - newLeft) / (oldRight - oldLeft)
        }

        copy(x = newX, children = newChildren) to nextLeaf
    }

internal fun XYNode.scaleHeight(height: Double): XYNode = mulHeight(height / this.height)

private fun XYNode.mulHeight(factor: Double): XYNode = copy(
    y = y * factor,
    children = children.map { it.mulHeight(factor) }
)

internal fun XYNode.shiftY(amount: Double): XYNode = copy(y = y + amount, children = children.map { it.shiftY(amount) })

/**
 * Algorithms to build dendro layout
 */
internal object Layout {

    fun Knuth(xy: XYNode) = Knuth(0, 0, xy).second

    /** for binary trees */
    private fun Knuth2(iSeed: Int, depth: Int, xy: XYNode): Pair<Int, XYNode> {
        if (!xy.children.isEmpty() && xy.children.size != 2)
            throw IllegalStateException("not a binary tree")

        var i = iSeed

        var left = xy.leftmost
        if (left != null) {
            val r = Knuth2(i, depth + 1, left)
            i = r.first
            left = r.second
        }

        val x = i
        i += 1

        var right = xy.rightmost
        if (right != null) {
            val r = Knuth2(i, depth + 1, right)
            i = r.first
            right = r.second
        }

        return i to xy.copy(
            x = x.toDouble(), depth = depth,
            children = if (left == null || right == null) emptyList() else listOf(left, right)
        )
    }

    /** for any kind of trees */
    private fun Knuth(iSeed: Int, depth: Int, xy: XYNode): Pair<Int, XYNode> {
        if (xy.children.isEmpty())
            return (iSeed + 1) to xy.copy(
                x = iSeed.toDouble(),
                depth = depth
            )


        val newChildren = mutableListOf<XYNode>()
        val xdata = mutableListOf<Int>()
        var i = iSeed - 1
        for (child in xy.children) {
            val r = Knuth(i + 1, depth + 1, child)
            i = r.first
            xdata += r.first
            newChildren += r.second
        }

        return i to xy.copy(
            x = xdata[(xdata.size - 1) / 2].toDouble(),
            depth = depth,
            children = newChildren
        )
    }
}
