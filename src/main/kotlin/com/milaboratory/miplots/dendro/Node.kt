package com.milaboratory.miplots.dendro


interface ChildrenReceiver {
    fun Node(distance: Double, vararg meta: Pair<String, Any>, builder: ChildrenReceiver.() -> Unit)

    fun Node(distance: Double, vararg meta: Pair<String, Any>)
}

class Children : ChildrenReceiver {
    internal val children = mutableListOf<Node>()

    override fun Node(distance: Double, vararg meta: Pair<String, Any>, builder: ChildrenReceiver.() -> Unit) {
        val children = Children()
        children.builder()
        val node = Node(meta.toMap(), distance, children.children)
        this.children.add(node)
    }

    override fun Node(distance: Double, vararg meta: Pair<String, Any>) {
        val node = Node(meta.toMap(), distance, emptyList())
        children.add(node)
    }
}

data class Node(
    val metadata: Map<String, Any>,
    val distanceToParent: Double,
    val children: List<Node>
) {
    companion object {
        operator fun invoke(
            distance: Double,
            vararg meta: Pair<String, Any>,
            builder: ChildrenReceiver.() -> Unit
        ): Node =
            run {
                val children = Children()
                children.builder()
                Node(meta.toMap(), distance, children.children)
            }
    }
}


internal fun Node.xy(y: Double, depth: Int): XYNode {
    return XYNode(this,
        -1.0,
        y + distanceToParent,
        depth,
        children.map { it.xy(y + distanceToParent, depth + 1) }
    )
}

internal fun Node.xy() = xy(0.0, 0)
