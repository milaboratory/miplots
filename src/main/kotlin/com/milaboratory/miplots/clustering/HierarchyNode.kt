package com.milaboratory.miplots.clustering

import com.milaboratory.miplots.dendro.Node

/**
 *
 */
data class HierarchyNode(val id: Int, val children: List<Int>, val height: Double) {
    init {
        if (!height.isFinite())
            throw IllegalArgumentException("Height can't be infinite or NaN")
    }
}

fun List<HierarchyNode>.asTree(): Node<Int> = run {
    // id -> hnode
    val id2h: Map<Int, HierarchyNode> = this.associateBy { it.id }
    // root node
    val allWithParent = this.flatMap { it.children }.toSet()
    val all = this.flatMap { it.children + it.id }.toSet()
    val root = all.find { !allWithParent.contains(it) } ?: throw RuntimeException()
    hNode2tNode(root, id2h)
}

private fun hNode2tNode(
    id: Int,
    id2h: Map<Int, HierarchyNode>
): Node<Int> = run {
    val h = id2h[id]!!
    Node(
        id,
        h.height,
        h.children.map { child ->
            val ch = id2h[child]
            if (ch == null)
                Node(child, 0.0, emptyList(), emptyMap())
            else
                hNode2tNode(ch.id, id2h)
        },
        emptyMap()
    )
}

