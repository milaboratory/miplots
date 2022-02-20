@file:Suppress("FunctionName")

package com.milaboratory.miplots.dendro


interface ChildrenReceiver<T> {
    fun Node(id: T, height: Double, vararg meta: Pair<String, Any>, builder: ChildrenReceiver<T>.() -> Unit)

    fun Node(id: T, height: Double, vararg meta: Pair<String, Any>)

    fun Node(height: Double, vararg meta: Pair<String, Any>, builder: ChildrenReceiver<T>.() -> Unit)

    fun Node(height: Double, vararg meta: Pair<String, Any>)
}

class Children<T> : ChildrenReceiver<T> {
    internal val children = mutableListOf<Node<T>>()

    override fun Node(
        height: Double,
        vararg meta: Pair<String, Any>,
        builder: ChildrenReceiver<T>.() -> Unit
    ) {
        val children = Children<T>()
        children.builder()
        val node = Node(null, height, children.children, meta.toMap())
        this.children.add(node)
    }

    override fun Node(
        id: T,
        height: Double,
        vararg meta: Pair<String, Any>,
        builder: ChildrenReceiver<T>.() -> Unit
    ) {
        val children = Children<T>()
        children.builder()
        val node = Node(id, height, children.children, meta.toMap())
        this.children.add(node)
    }

    override fun Node(height: Double, vararg meta: Pair<String, Any>) {
        val node = Node<T>(null, height, emptyList(), meta.toMap())
        children.add(node)
    }

    override fun Node(id: T, height: Double, vararg meta: Pair<String, Any>) {
        val node = Node(id, height, emptyList(), meta.toMap())
        children.add(node)
    }
}

data class Node<T>(
    val id: T?,
    /** distance to children */
    val height: Double,
    val children: List<Node<T>>,
    val metadata: Map<String, Any>
) {
    val leftmost get() = children.firstOrNull()
    val rightmost get() = children.lastOrNull()
    val isLeaf get() = children.isEmpty()

    val depth: Int by lazy {
        if (children.isEmpty())
            0
        else
            1 + children.maxOf { it.depth }
    }

    val count: Int by lazy {
        1 + children.sumOf { it.count }
    }

    val leafCount: Int by lazy {
        if (children.isEmpty())
            1
        else
            children.sumOf { it.count }
    }

    /** total height */
    val totalHeight: Double by lazy {
        height + (children.maxOfOrNull { it.totalHeight } ?: 0.0)
    }

    companion object {
        operator fun <T> invoke(
            id: T,
            height: Double,
            vararg meta: Pair<String, Any>,
            builder: ChildrenReceiver<T>.() -> Unit = {}
        ): Node<T> =
            run {
                val children = Children<T>()
                children.builder()
                Node(id, height, children.children, meta.toMap())
            }

        operator fun invoke(
            height: Double,
            vararg meta: Pair<String, Any>,
            builder: ChildrenReceiver<Any?>.() -> Unit = {}
        ): Node<Any?> =
            run {
                val children = Children<Any?>()
                children.builder()
                Node(null, height, children.children, meta.toMap())
            }
    }
}

fun <T> Node<T>.adjustHeight(f: (Double) -> Double): Node<T> =
    copy(children = children.map { it.adjustHeight(f) }, height = f(height))

fun <I, O> Node<I>.mapId(f: (I?) -> O?): Node<O> =
    Node(f(id), height, children.map { it.mapId(f) }, metadata)

fun <T : Comparable<T>> Node<T>.normalize(): Node<T> = run {
    val ch = children.sortedWith(
        compareBy<Node<T>> { -it.count }
            .thenComparing(compareBy { it.id })
    )

    Node(id, height, ch.map { it.normalize() }, metadata)
}

fun <T> Node<T>.leaves(): List<Node<T>> = if (this.isLeaf) listOf(this) else children.flatMap { it.leaves() }
