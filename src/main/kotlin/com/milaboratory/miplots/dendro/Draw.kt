package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.FeatureWrapper
import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.dendro.Alignment.Horizontal
import com.milaboratory.miplots.dendro.Alignment.Vertical
import com.milaboratory.miplots.plus
import com.milaboratory.miplots.themeBlank
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomSegment
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import jetbrains.letsPlot.letsPlot

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

//internal fun Map<String, List<Any?>>.map(mapper: (Map<String, Any?>) -> Map<String, Any?>): Map<String, List<Any?>> =
//    run {
//        val keys = this.keys
//        val values = values
//        val n = values.firstOrNull()?.size ?: 0
//        val newRows = (0 until n).map { iRow ->
//            mapper(keys.associateWith { key -> this[key]!![iRow] })
//        }
//        keys.associateWith { key -> newRows.map { it[key] } }
//    }

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

data class Point(val x: Any, val y: Any)
data class Line(val x1: Any, val y1: Any, val x2: Any, val y2: Any)

fun Point.flip() = Point(y, x)
fun Line.flip() = Line(y1, x1, y2, x2)

internal enum class Alignment {
    Horizontal {
        override fun apply(p: Point) = p.flip()
        override fun apply(l: Line) = l.flip()
    },
    Vertical;

    open fun apply(p: Point): Point = p
    open fun apply(l: Line): Line = l
}

private fun XYNode.yImposed(imposedLeafY: Double?) = if (isLeaf) imposedLeafY ?: y else y

internal fun XYNode.addNodesData(
    db: DataBuilder,
    al: Alignment,
    imposedLeafY: Double?
) {

    val (x, y) = al.apply(Point(this.x, this.yImposed(imposedLeafY)))
    db.add(
        DendroVar.x to x,
        DendroVar.y to y,
        DendroVar.depth to depth,
        *node.metadata.toList().toTypedArray()
    )
    children.forEach { it.addNodesData(db, al, imposedLeafY) }
}

internal fun XYNode.addEdgesData(
    db: DataBuilder,
    ctype: ConnectionType,
    einh: EdgeMetaInheritance,
    al: Alignment,
    imposedLeafY: Double?
) {
    children.forEachIndexed { childIndex, child ->
        val metadata = if (einh == EdgeMetaInheritance.Down) {
            node.metadata.toList().toTypedArray()
        } else {
            child.node.metadata.toList().toTypedArray()
        }

        when (ctype) {
            ConnectionType.Triangle -> {
                val (x1, y1, x2, y2) = al.apply(Line(this.x, this.y, child.x, child.yImposed(imposedLeafY)))
                db.add(
                    DendroVar.x1 to x1,
                    DendroVar.y1 to y1,
                    DendroVar.x2 to x2,
                    DendroVar.y2 to y2,
                    *metadata
                )
            }
            ConnectionType.Rectangle -> {
                if (child.x == this.x) {
                    val (x1, y1, x2, y2) = al.apply(
                        Line(
                            this.x,
                            this.yImposed(imposedLeafY),
                            child.x,
                            child.yImposed(imposedLeafY)
                        )
                    )
                    db
                        .add(
                            DendroVar.x1 to x1,
                            DendroVar.y1 to y1,
                            DendroVar.x2 to x2,
                            DendroVar.y2 to y2,
                            *metadata
                        )
                } else {
                    val x2 =
                        if ((childIndex + 1 <= children.size && childIndex + 1 >= 0)
                            && ((child.x < x && children[childIndex + 1].x >= x)
                                    || (child.x > x && children[childIndex - 1].x <= x))
                        ) {
                            x
                        } else if (child.x < x) {
                            children[childIndex + 1].x
                        } else if ((child.x > x)) {
                            children[childIndex - 1].x
                        } else {
                            x
                        }

                    val (l1x1, l1y1, l1x2, l1y2) = al.apply(
                        Line(
                            child.x,
                            this.yImposed(imposedLeafY),
                            child.x,
                            child.yImposed(imposedLeafY)
                        )
                    )
                    db.add(
                        DendroVar.x1 to l1x1,
                        DendroVar.y1 to l1y1,
                        DendroVar.x2 to l1x2,
                        DendroVar.y2 to l1y2,
                        *metadata
                    )

                    val (l2x1, l2y1, l2x2, l2y2) = al.apply(
                        Line(
                            child.x,
                            this.yImposed(imposedLeafY),
                            x2,
                            yImposed(imposedLeafY)
                        )
                    )
                    db.add(
                        DendroVar.x1 to l2x1,
                        DendroVar.y1 to l2y1,
                        DendroVar.x2 to l2x2,
                        DendroVar.y2 to l2y2,
                        *metadata
                    )
                }
            }
        }
        child.addEdgesData(db, ctype, einh, al, imposedLeafY)
    }
}

class DendroAes {
    /** Node shape */
    var shape: Any? = null

    /** Edge/dot color */
    var color: Any? = null

    /** Dot fill */
    var fill: Any? = null

    /** Edge linetype */
    var linetype: Any? = null
}

class GeomDendroLayer(
    val points: Boolean,
    val lines: Boolean,
    val color: Any?,
    val fill: Any?,
    val linetype: Any?,
    val shape: Any?,
    val aes: DendroAes,
    val edgesData: Map<String, List<Any?>>,
    val pointsData: Map<String, List<Any?>>
) : FeatureWrapper {

    val segmentLayer = geomSegment(
        edgesData,
        color = color,
        linetype = linetype,
    ) {
        x = DendroVar.x1
        y = DendroVar.y1
        xend = DendroVar.x2
        yend = DendroVar.y2
        linetype = aes.linetype
        color = aes.color
    }

    val pointLayer = geomPoint(
        pointsData,
        shape = shape,
        color = color,
        fill = fill
    ) {
        x = DendroVar.x
        y = DendroVar.y
        fill = aes.fill
        color = aes.color
        shape = aes.shape
    }

    override val feature = run {
        var f: Feature = FeatureList(emptyList())
        if (lines)
            f += segmentLayer
        if (points)
            f += pointLayer
        f
    }
}

enum class ConnectionType { Triangle, Rectangle }
enum class EdgeMetaInheritance { Up, Down }

internal val Position.alignment
    get() = when (this) {
        Top, Bottom -> Vertical
        else -> Horizontal
    }

fun geomDendro(
    tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,
    points: Boolean = true,
    lines: Boolean = true,
    center: Boolean = true,
    color: Any? = null,
    fill: Any? = null,
    linetype: Any? = null,
    shape: Any? = null,
    rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,
    aesMapping: DendroAes.() -> Unit = {}
) = run {
    val aes = DendroAes()
    aes.aesMapping()

    val dbNodes = DataBuilder()
    val dbEdges = DataBuilder()

    var xy = Layout.Knuth(tree.xy())
    if (rpos == Top || rpos == Right)
        xy = xy.flipY()
    if (coord != null)
        xy = xy.imposeX(coord, center)
    if (height != null)
        xy = xy.scaleHeight(height)
    if (rshift != null)
        xy = xy.shiftY(rshift)

    val imposedLeafY = if (balanced) xy.leafY else null

    xy.addNodesData(dbNodes, rpos.alignment, imposedLeafY)
    xy.addEdgesData(dbEdges, ctype, einh, rpos.alignment, imposedLeafY)

    GeomDendroLayer(
        points = points,
        lines = lines,
        color = color,
        fill = fill,
        linetype = linetype,
        shape = shape,
        aes = aes,
        edgesData = dbEdges.result,
        pointsData = dbNodes.result
    )
}

fun ggDendro(
    tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,
    points: Boolean = true,
    lines: Boolean = true,
    center: Boolean = true,
    color: Any? = null,
    fill: Any? = null,
    linetype: Any? = null,
    shape: Any? = null,
    rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,
    aesMapping: DendroAes.() -> Unit = {}
) = run {
    var plt = letsPlot()
    plt += geomDendro(
        tree = tree,
        ctype = ctype,
        einh = einh,
        points = points,
        lines = lines,
        center = center,
        color = color,
        fill = fill,
        linetype = linetype,
        shape = shape,
        rpos = rpos,
        rshift = rshift,
        balanced = balanced,
        coord = coord,
        height = height,
        aesMapping = aesMapping,
    )
    plt += themeBlank()
    plt
}

