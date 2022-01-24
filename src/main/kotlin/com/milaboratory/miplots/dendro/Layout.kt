@file:Suppress("FunctionName")

package com.milaboratory.miplots.dendro

private fun Node<*>.xy(y: Double, depth: Int): XYNode {
    return XYNode(this,
        -1.0,
        y + height,
        depth,
        children.map { it.xy(y + height, depth + 1) }
    )
}

internal fun Node<*>.xy() = xy(0.0, 0)

internal data class XYNode(
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
}

internal fun XYNode.flipY(): XYNode = copy(y = -y, children = children.map { it.flipY() })
internal fun XYNode.flipX(): XYNode = copy(x = -x, children = children.map { it.flipX() })

internal fun XYNode.imposeX(coord: List<Double>) = imposeX(coord, 0).first
internal fun XYNode.imposeX(coord: List<Double>, iLeaf: Int): Pair<XYNode, Int> =
    if (isLeaf) {
        copy(x = coord[iLeaf]) to (iLeaf + 1)
    } else {
        val oldLeft = children.first().x
        val oldRight = children.last().x

        val newChildren = mutableListOf<XYNode>()
        var nextLeaf = iLeaf
        for (child in children) {
            val (newChild, i) = child.imposeX(coord, nextLeaf)
            newChildren.add(newChild)
            nextLeaf = i
        }
        val newLeft = newChildren.first().x
        val newRight = newChildren.last().x

        val newX = newLeft + (x - oldLeft) * (newRight - newLeft) / (oldRight - oldLeft)

        copy(x = newX, children = newChildren) to nextLeaf
    }

internal fun XYNode.scaleHeight(height: Double): XYNode = mulHeight(height / this.height)

private fun XYNode.mulHeight(factor: Double): XYNode = copy(
    y = y * factor,
    children = children.map { it.mulHeight(factor) }
)


/**
 * Algorithms to build dendro layout
 */
internal object Layout {

    fun Knuth(xy: XYNode) = Knuth(0, 0, xy).second

    /** for binary trees */
    private fun Knuth(iSeed: Int, depth: Int, xy: XYNode): Pair<Int, XYNode> {
//        if (!xy.children.isEmpty() && xy.children.size != 2)
//            throw IllegalStateException("not a binary tree")

        var i = iSeed

        var left = xy.leftmost
        if (left != null) {
            val r = Knuth(i, depth + 1, left)
            i = r.first
            left = r.second
        }

        val x = i
        i += 1

        var right = xy.rightmost
        if (right != null) {
            val r = Knuth(i, depth + 1, right)
            i = r.first
            right = r.second
        }

        return i to xy.copy(
            x = x.toDouble(), depth = depth,
            children = if (left == null || right == null) emptyList() else listOf(left, right)
        )
    }
}
