package com.milaboratory.miplots.clustering

/**
 *
 */
data class HierarchyNode(val id: Int, val children: List<Int>, val height: Double)

data class TreeNode<T>(val id: T, val children: List<TreeNode<T>>, val height: Double) {
    inline val isLeaf get() = children.isEmpty()

    val depth: Int by lazy {
        if (children.isEmpty())
            0
        else
            1 + children.maxOf { it.depth }
    }

    val count: Int by lazy {
        if (children.isEmpty())
            0
        else
            children.sumOf { it.count }
    }
}

/** from left to right */
fun <T> TreeNode<T>.leafsOrdered(): List<TreeNode<T>> =
    if (this.children.isEmpty())
        listOf(this)
    else
        this.children.flatMap { it.leafsOrdered() }

fun <I, O> TreeNode<I>.map(f: (I) -> O): TreeNode<O> =
    TreeNode(f(id), children.map { it.map(f) }, height)

fun <T> TreeNode<T>.adjustHeight(f: (Double) -> Double): TreeNode<T> =
    TreeNode(id, children.map { it.adjustHeight(f) }, f(height))

fun <T : Comparable<T>> TreeNode<T>.normalize(): TreeNode<T> = run {
    val ch = children.sortedWith(Comparator
        .comparing<TreeNode<T>, Int> { it.count }
        .thenComparing { f -> f.id }
    )

    TreeNode(id, ch.map { it.normalize() }, height)
}

fun List<HierarchyNode>.asTree(): TreeNode<Int> = run {
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
): TreeNode<Int> = run {
    val h = id2h[id]!!
    TreeNode(
        id,
        h.children.map { child ->
            val ch = id2h[child]
            if (ch == null)
                TreeNode(child, emptyList(), 0.0)
            else
                hNode2tNode(ch.id, id2h)
        },
        h.height
    )
}

