package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.dendro.RootPosition.Top
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomSegment
import jetbrains.letsPlot.intern.Plot

internal class DataBuilder {
    val result: Map<String, List<Any?>> get() = accumulator
    private val accumulator = mutableMapOf<String, MutableList<Any?>>()

    fun add(record: Map<String, Any>) {
        val numberOfRecords = accumulator.values.firstOrNull()?.size ?: 0
        val unionKeys = accumulator.keys + record.keys
        unionKeys.forEach { key ->
            accumulator.computeIfAbsent(key) { (0 until numberOfRecords).map { null }.toMutableList() }.add(record[key])
        }
    }

    fun add(vararg pairs: Pair<String, Any>) = run {
        add(pairs.toMap())
        this
    }
}

private object DendroVar {
    // node
    const val x = "x"
    const val y = "y"
    const val depth = "depth"

    // edge
    const val x1 = "x1"
    const val x2 = "x2"
    const val y1 = "y1"
    const val y2 = "y2"
}


internal fun XYNode.addNodesData(db: DataBuilder) {
    db.add(
        DendroVar.x to x,
        DendroVar.y to y,
        DendroVar.depth to depth,
        *node.metadata.toList().toTypedArray()
    )
    children.forEach { it.addNodesData(db) }
}

internal fun XYNode.addEdgesData(
    db: DataBuilder,
    connectionType: ConnectionType,
    edgeMetaInheritance: EdgeMetaInheritance
) {
    children.forEachIndexed { childIndex, child ->
        val metadata = if (edgeMetaInheritance == EdgeMetaInheritance.Down) {
            node.metadata.toList().toTypedArray()
        } else {
            child.node.metadata.toList().toTypedArray()
        }

        when (connectionType) {
            ConnectionType.Triangle -> {
                db.add(
                    DendroVar.x1 to x,
                    DendroVar.y1 to y,
                    DendroVar.x2 to child.x,
                    DendroVar.y2 to child.y,
                    *metadata
                )
            }
            ConnectionType.Rectangle -> {
                if (child.x == this.x) {
                    db
                        .add(
                            DendroVar.x1 to x,
                            DendroVar.y1 to y,
                            DendroVar.x2 to child.x,
                            DendroVar.y2 to child.y,
                            *metadata
                        )
                } else {
                    val x2 =
                        if ((childIndex + 1 <= children.size && childIndex + 1 >= 0) && ((child.x < x && children[childIndex + 1].x >= x) || (child.x > x && children[childIndex - 1].x <= x))) {
                            x
                        } else if (child.x < x) {
                            children[childIndex + 1].x
                        } else if ((child.x > x)) {
                            children[childIndex - 1].x
                        } else {
                            x
                        }

                    db
                        .add(
                            DendroVar.x1 to child.x,
                            DendroVar.y1 to y,
                            DendroVar.x2 to child.x,
                            DendroVar.y2 to child.y,
                            *metadata
                        )
                        .add(
                            DendroVar.x1 to child.x,
                            DendroVar.y1 to y,
                            DendroVar.x2 to x2,
                            DendroVar.y2 to y,
                            *metadata
                        )
                }
            }
        }
        child.addEdgesData(db, connectionType, edgeMetaInheritance)
    }
}

class DendroAes {
    /** Node shape */
    var shape: Any? = null

    /** Edge color */
    var color: Any? = null

    /** Edge linetype */
    var linetype: Any? = null
}

class GeomDendroLayer(
    val aes: DendroAes,
    val edgesData: Map<String, List<Any?>>,
    val pointsData: Map<String, List<Any?>>,
    val rpos: RootPosition
)

operator fun Plot.plus(dendro: GeomDendroLayer) = run {
    var plt = this

    plt += geomSegment(dendro.edgesData) {
        x = "x1"
        y = "y1"
        xend = "x2"
        yend = "y2"
        linetype = dendro.aes.linetype
        color = dendro.aes.color
    }

    plt += geomPoint(
        dendro.pointsData, shape = 19
    ) {
        x = "x"
        y = "y"
        fill = dendro.aes.color
        color = dendro.aes.color
        shape = dendro.aes.shape
    }

    plt
}


enum class ConnectionType { Triangle, Rectangle }
enum class EdgeMetaInheritance { Up, Down }
enum class RootPosition { Top, Right, Bottom, Left }

fun geomDendro(
    tree: Node,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,
    rpos: RootPosition = Top,
    aesMapping: DendroAes.() -> Unit = {}
) = run {
    val aes = DendroAes()
    aes.aesMapping()

    val dbNodes = DataBuilder()
    val dbEdges = DataBuilder()

    val xy = tree.xy().flipY()
    xy.addNodesData(dbNodes)
    xy.addEdgesData(dbEdges, ctype, einh)

    GeomDendroLayer(aes, dbEdges.result, dbNodes.result, rpos)
}

