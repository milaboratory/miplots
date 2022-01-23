package com.milaboratory.miplots.dendro


internal data class XYNode(
    val node: Node,
    val x: Double,
    val y: Double,
    val depth: Int,
    val children: List<XYNode>
) {
    val leftmost get() = children.firstOrNull()
    val rightmost get() = children.lastOrNull()
    val isLeaf get() = children.isEmpty()
}

internal fun XYNode.flipY(): XYNode = copy(y = -y, children = children.map { it.flipY() })
internal fun XYNode.flipX(): XYNode = copy(x = -x, children = children.map { it.flipX() })

/**
 * Algorithms to build dendro layout
 */
internal object Layout {

    fun Knuth(xy: XYNode) = Knuth(0, 0, xy).second

    /** for binary trees */
    private fun Knuth(iSeed: Int, depth: Int, xy: XYNode): Pair<Int, XYNode> {
        if (!xy.children.isEmpty() && xy.children.size != 2)
            throw IllegalStateException("not a binary tree")

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
