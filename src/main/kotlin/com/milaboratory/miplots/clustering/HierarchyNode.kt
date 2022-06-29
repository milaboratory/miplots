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
package com.milaboratory.miplots.clustering

import com.milaboratory.miplots.dendro.Node

/**
 *
 */
data class HierarchyNode(
    val id: Int,
    val children: List<Int>,
    /** distance to children! */
    val height: Double
) {
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
    hNode2tNode(root, 0.0, id2h)
}

private fun hNode2tNode(
    id: Int,
    height: Double,
    id2h: Map<Int, HierarchyNode>
): Node<Int> = run {
    val h = id2h[id]!!
    Node(
        id,
        height,
        h.children.map { child ->
            val ch = id2h[child]
            if (ch == null)
                Node(child, h.height, emptyList(), emptyMap())
            else
                hNode2tNode(ch.id, h.height, id2h)
        },
        emptyMap()
    )
}

