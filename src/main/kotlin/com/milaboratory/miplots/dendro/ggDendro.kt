package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.FeatureWrapper
import com.milaboratory.miplots.Position
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.dendro.Alignment.Horizontal
import com.milaboratory.miplots.dendro.Alignment.Vertical
import com.milaboratory.miplots.plus
import com.milaboratory.miplots.themeBlank
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.sampling.samplingNone
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

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
    const val nx = "x"
    const val ny = "y"
    const val depth = "depth"

    // edge
    const val ex = "ex"
    const val ey = "ey"

    // edge id
    const val eid = "eid"
}

data class Point(val x: Double, val y: Double)
data class Line(val x1: Double, val y1: Double, val x2: Double, val y2: Double)

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
        DendroVar.nx to x,
        DendroVar.ny to y,
        DendroVar.depth to depth,
        *node.metadata.toList().toTypedArray()
    )
    children.forEach { it.addNodesData(db, al, imposedLeafY) }
}

internal fun XYNode.addEdgesData(
    edgesDb: DataBuilder,
    ctype: ConnectionType,
    einh: EdgeMetaInheritance,
    rpos: Position,
    imposedLeafY: Double?,
    linewidthX: Double,
    linewidthY: Double,
    initialEid: Int
): Int {
    val al = rpos.alignment
    var eid = initialEid
    children.forEachIndexed { childIndex, child ->
        val metadata = if (einh == EdgeMetaInheritance.Down) {
            node.metadata.toList().toTypedArray()
        } else {
            child.node.metadata.toList().toTypedArray()
        }

        when (ctype) {
            ConnectionType.Triangle -> {
                // initial coordinates
                val (ix1, iy1, ix2, iy2) = Line(this.x, this.y, child.x, child.yImposed(imposedLeafY))

                // add polygon
                val tlw =
                    0.5 * linewidthY * sqrt((ix2 - ix1).pow(2.0) + ((iy2 - iy1) * linewidthX / linewidthY).pow(2.0)) / abs(
                        ix2 - ix1
                    )
                val (px1, py1, px2, py2) = al.apply(Line(ix1, iy1 + tlw, ix2, iy2 + tlw))
                val (px3, py3, px4, py4) = al.apply(Line(ix2, iy2 - tlw, ix1, iy1 - tlw))
                edgesDb.add(
                    DendroVar.ex to px1,
                    DendroVar.ey to py1,
                    DendroVar.eid to eid,
                    *metadata
                )
                edgesDb.add(
                    DendroVar.ex to px2,
                    DendroVar.ey to py2,
                    DendroVar.eid to eid,
                    *metadata
                )
                edgesDb.add(
                    DendroVar.ex to px3,
                    DendroVar.ey to py3,
                    DendroVar.eid to eid,
                    *metadata
                )
                edgesDb.add(
                    DendroVar.ex to px4,
                    DendroVar.ey to py4,
                    DendroVar.eid to eid,
                    *metadata
                )

                ++eid
            }
            ConnectionType.Rectangle -> {
                if (child.x == this.x) {
                    // initial coordinates
                    val (ix1, iy1, ix2, iy2) = Line(
                        this.x,
                        this.y,
                        child.x,
                        child.yImposed(imposedLeafY)
                    )

                    // add vertical polygon
                    val (px1, py1, px2, py2) = al.apply(Line(ix1 - linewidthX / 2, iy1, ix1 + linewidthX / 2, iy1))
                    val (px3, py3, px4, py4) = al.apply(Line(ix1 + linewidthX / 2, iy2, ix1 - linewidthX / 2, iy2))
                    edgesDb.add(
                        DendroVar.ex to px1,
                        DendroVar.ey to py1,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to px2,
                        DendroVar.ey to py2,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to px3,
                        DendroVar.ey to py3,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to px4,
                        DendroVar.ey to py4,
                        DendroVar.eid to eid,
                        *metadata
                    )

                    ++eid
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

                    // initial coordinates (vertical line)
                    val (vx1, vy1, vx2, vy2) = Line(
                        child.x,
                        this.y,
                        child.x,
                        child.yImposed(imposedLeafY)
                    )

                    // add vertical polygon
                    val sign = when (rpos) {
                        Top, Right -> 1.0
                        Bottom, Left -> -1.0
                    }

                    val (vpx1, vpy1, vpx2, vpy2) = al.apply(
                        Line(
                            vx1 - linewidthX / 2,
                            vy1 + sign * linewidthY / 2,
                            vx1 + linewidthX / 2,
                            vy1 + sign * linewidthY / 2
                        )
                    )
                    val (vpx3, vpy3, vpx4, vpy4) = al.apply(Line(vx1 + linewidthX / 2, vy2, vx1 - linewidthX / 2, vy2))
                    edgesDb.add(
                        DendroVar.ex to vpx1,
                        DendroVar.ey to vpy1,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to vpx2,
                        DendroVar.ey to vpy2,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to vpx3,
                        DendroVar.ey to vpy3,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to vpx4,
                        DendroVar.ey to vpy4,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    ++eid

                    // initial coordinates (horizontal line)
                    val (hx1, hy1, hx2, hy2) = Line(
                        child.x,
                        this.y,
                        x2,
                        this.y
                    )

                    // add horizontal polygon
                    val (hpx1, hpy1, hpx2, hpy2) = al.apply(Line(hx1, hy1 - linewidthY / 2, hx2, hy1 - linewidthY / 2))
                    val (hpx3, hpy3, hpx4, hpy4) = al.apply(Line(hx2, hy1 + linewidthY / 2, hx1, hy1 + linewidthY / 2))
                    edgesDb.add(
                        DendroVar.ex to hpx1,
                        DendroVar.ey to hpy1,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to hpx2,
                        DendroVar.ey to hpy2,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to hpx3,
                        DendroVar.ey to hpy3,
                        DendroVar.eid to eid,
                        *metadata
                    )
                    edgesDb.add(
                        DendroVar.ex to hpx4,
                        DendroVar.ey to hpy4,
                        DendroVar.eid to eid,
                        *metadata
                    )

                    ++eid
                }
            }
        }
        eid += child.addEdgesData(edgesDb, ctype, einh, rpos, imposedLeafY, linewidthX, linewidthY, eid)
    }
    return eid - initialEid
}

class DendroAes {
    /** Node shape */
    var shape: Any? = null

    /** Edge/node color */
    var color: Any? = null

    /** Node fill */
    var fill: Any? = null

    /** Node size */
    var size: Any? = null

    /** Edge linetype */
    var linetype: Any? = null

    /** Edge width */
    var linewidth: Any? = null
}

internal class GeomDendroLayer(
    // data
    val showNodes: Boolean,
    val showEdges: Boolean,
    val nodesData: Map<String, List<Any?>>,
    val edgesData: Map<String, List<Any?>>,
    // nodes
    val shape: Any?,
    val color: Any?,
    val fill: Any?,
    val size: Double?,
    //edges
    val linetype: Any?,
    val linewidth: Double?,
    val linecolor: Any?,
    //aes
    val aes: DendroAes,
) : FeatureWrapper {

    val edgesLayer by lazy {
        geomPolygon(
            edgesData,
            fill = linecolor,
            linetype = linetype,
            sampling = samplingNone
        ) {
            x = DendroVar.ex
            y = DendroVar.ey
            fill = aes.color
            linetype = aes.linetype
            group = DendroVar.eid
        }
    }

    val nodesLayer = geomPoint(
        nodesData,
        shape = shape,
        color = color,
        fill = fill,
        size = size,
        sizeUnit = "x",
        sampling = samplingNone
    ) {
        x = DendroVar.nx
        y = DendroVar.ny
        fill = aes.fill
        color = aes.color
        shape = aes.shape
        size = aes.size
    }

    override val feature = run {
        var f: Feature = FeatureList(emptyList())
        if (showEdges)
            f += edgesLayer
        if (showNodes)
            f += nodesLayer
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
    showNodes: Boolean = true,
    showEdges: Boolean = true,
    center: Boolean = true,
    rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,
    // nodes
    shape: Any? = null,
    color: Any? = null,
    fill: Any? = null,
    size: Double? = null,
    // edges
    linetype: Any? = null,
    linewidth: Double? = null,
    linewidthY: Double? = null,
    linecolor: Any? = null,
    // aes
    aesMapping: DendroAes.() -> Unit = {}
): FeatureWrapper = run {
    val aes = DendroAes().apply(aesMapping)

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

    val _linewidthX = linewidth ?: (xy.width / xy.node.leafCount / 10)
    val _linewidthY = linewidthY ?: abs(_linewidthX * xy.height / xy.width)
    val sizeActual = size ?: (1.1 * _linewidthX)

    val dbNodes = DataBuilder()
    val dbEdges = DataBuilder()

    xy.addNodesData(dbNodes, rpos.alignment, imposedLeafY)
    xy.addEdgesData(dbEdges, ctype, einh, rpos, imposedLeafY, _linewidthX, _linewidthY, 0)

    GeomDendroLayer(
        showNodes = showNodes,
        showEdges = showEdges,
        nodesData = dbNodes.result,
        edgesData = dbEdges.result,

        shape = shape,
        color = color,
        fill = fill,
        size = sizeActual,

        linetype = linetype,
        linewidth = linewidth,
        linecolor = linecolor,

        aes = aes,
    )
}

fun ggDendro(
    tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,
    showNodes: Boolean = true,
    showEdges: Boolean = true,
    center: Boolean = true,
    rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,
    // nodes
    shape: Any? = null,
    color: Any? = null,
    fill: Any? = null,
    size: Double? = null,
    // edges
    linetype: Any? = null,
    linewidth: Double? = null,
    linewidthY: Double? = null,
    linecolor: Any? = null,
    // aes
    aesMapping: DendroAes.() -> Unit = {}
) = run {
    var plt = letsPlot()
    plt += geomDendro(
        tree = tree,
        ctype = ctype,
        einh = einh,
        showNodes = showNodes,
        showEdges = showEdges,
        center = center,

        shape = shape,
        color = color,
        fill = fill,
        size = size,

        linetype = linetype,
        linewidth = linewidth,
        linewidthY = linewidthY,
        linecolor = linecolor,

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

