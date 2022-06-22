@file:Suppress("LocalVariableName")

package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.*
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.color.Palletes
import com.milaboratory.miplots.dendro.Alignment.Horizontal
import com.milaboratory.miplots.dendro.Alignment.Vertical
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomPath
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.sampling.samplingNone
import jetbrains.letsPlot.scale.xlim
import jetbrains.letsPlot.scale.ylim
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

internal class DataBuilder {
    val result: Map<Any, List<Any?>> get() = accumulator
    private val accumulator = mutableMapOf<Any, MutableList<Any?>>()

    fun add(record: Map<Any, Any?>) {
        val numberOfRecords = accumulator.values.firstOrNull()?.size ?: 0
        val unionKeys = accumulator.keys + record.keys
        unionKeys.forEach { key ->
            accumulator.computeIfAbsent(key) { (0 until numberOfRecords).map { null }.toMutableList() }.add(record[key])
        }
    }

    fun add(vararg pairs: Pair<Any, Any?>) = run {
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

    // label
    const val lx = "lx"
    const val ly = "ly"
    const val label = "label"

    // label box
    const val lex = "lex"
    const val ley = "ley"
    const val lid = "lid"

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
    imposedLeafY: Double?,
    shiftLabelX: Double = 0.0,
    shiftLabelY: Double = 0.0
) {

    val (x, y) = al.apply(Point(this.x, this.yImposed(imposedLeafY)))
    val (lx, ly) = al.apply(Point(this.x + shiftLabelX, this.yImposed(imposedLeafY) + shiftLabelY))
    db.add(
        DendroVar.nx to x,
        DendroVar.ny to y,
        DendroVar.lx to lx,
        DendroVar.ly to ly,
        DendroVar.depth to depth,
        *node.metadata.toList().toTypedArray()
    )

    children.forEach { it.addNodesData(db, al, imposedLeafY, shiftLabelX, shiftLabelY) }
}

internal fun XYNode.addLabelsData(
    db: DataBuilder,
    al: Alignment,
    imposedLeafY: Double?,
    shiftLabelX: Double = 0.0,
    shiftLabelY: Double = 0.0
) {

    val (lx, ly) = al.apply(Point(this.x + shiftLabelX, this.yImposed(imposedLeafY) + shiftLabelY))
    db.add(
        DendroVar.lx to lx,
        DendroVar.ly to ly,
        DendroVar.depth to depth,
        *node.metadata.toList().toTypedArray()
    )

    children.forEach { it.addLabelsData(db, al, imposedLeafY, shiftLabelX, shiftLabelY) }
}

internal fun XYNode.addLabelBorder(
    db: DataBuilder,
    al: Alignment,
    imposedLeafY: Double?,
    label: Any,
    sx: Double,
    sy: Double,
    shiftLabelX: Double = 0.0,
    shiftLabelY: Double = 0.0,
    eid: Int = 0
): Int {

    var i = eid
    if (this.node.metadata[label] != null) {
        val ix = this.x + shiftLabelX
        val iy = this.yImposed(imposedLeafY) + shiftLabelY

        val (px1, py1, px2, py2) = al.apply(Line(ix - sx, iy - sy, ix - sx, iy + sy))
        val (px3, py3, px4, py4) = al.apply(Line(ix + sx, iy + sy, ix + sx, iy - sy))

        db.add(
            DendroVar.lex to px1,
            DendroVar.ley to py1,
            DendroVar.lid to i,
            *node.metadata.toList().toTypedArray()
        )
        db.add(
            DendroVar.lex to px2,
            DendroVar.ley to py2,
            DendroVar.lid to i,
            *node.metadata.toList().toTypedArray()
        )
        db.add(
            DendroVar.lex to px3,
            DendroVar.ley to py3,
            DendroVar.lid to i,
            *node.metadata.toList().toTypedArray()
        )
        db.add(
            DendroVar.lex to px4,
            DendroVar.ley to py4,
            DendroVar.lid to i,
            *node.metadata.toList().toTypedArray()
        )
        db.add(
            DendroVar.lex to px1,
            DendroVar.ley to py1,
            DendroVar.lid to i,
            *node.metadata.toList().toTypedArray()
        )
        i += 1
    }
    children.forEach { i += it.addLabelBorder(db, al, imposedLeafY, label, sx, sy, shiftLabelX, shiftLabelY, i) }
    return i - eid
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

    /** Labels layers*/
    var label: Any? = null
}

enum class ConnectionType { Triangle, Rectangle }
enum class EdgeMetaInheritance { Up, Down }

internal val Position.alignment
    get() = when (this) {
        Top, Bottom -> Vertical
        else -> Horizontal
    }

class geomDendro(
    tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,
    val showNodes: Boolean = true,
    val showEdges: Boolean = true,
    center: Boolean = true,
    val rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,
    // nodes
    val shape: Any? = null,
    val color: Any? = null,
    val alpha: Number? = null,
    val fill: Any? = null,
    val nodeSize: Double? = null,
    val labelSize: Double? = null,
    val labelAngle: Number? = null,
    val fillLabels: Boolean = true,
    // edges
    val linetype: Any? = null,
    val linewidth: Double? = null,
    val linewidthY: Double? = null,
    val linecolor: Any? = null,
    // aes
    val aes: DendroAes = DendroAes()
) : FeatureWrapper {
    private val xy: XYNode
    private val lwx: Double
    private val lwy: Double
    private val imposedLeafY: Double?
    private val xlim: Pair<Double, Double>?
    private val ylim: Pair<Double, Double>?
    private val nodeSizeActual: Double
    private val nodeSizeUnit: String

    private val nodes: Map<Any, List<Any?>>
    private val edges: Map<Any, List<Any?>>

    init {
        var xy = Layout.Knuth(tree.xy())
        if (rpos == Top || rpos == Right)
            xy = xy.flipY()
        if (coord != null)
            xy = xy.imposeX(coord, center)
        if (height != null)
            xy = xy.scaleHeight(height)
        if (rshift != null)
            xy = xy.shiftY(rshift)

        val lwx = linewidth ?: xy.width.let {
            if (it != 0.0)
                xy.width / xy.node.leafCount / 5
            else
                xy.height / 10
        }

        val lwy = linewidthY ?: xy.width.let {
            if (it != 0.0)
                abs(lwx * xy.height / it)
            else
                lwx
        }

        val xlim = if (xy.width == 0.0 && rpos.isTopBottom) -10 * lwx to 10 * lwx else null
        val ylim = if (xy.width == 0.0 && rpos.isLeftRight) -10 * lwy to 10 * lwy else null

        this.xy = xy
        this.lwx = lwx
        this.lwy = lwy
        this.xlim = xlim
        this.ylim = ylim

        imposedLeafY = if (balanced) xy.leafY else null
        nodeSizeUnit = if (rpos.isTopBottom) "x" else "y"
        nodeSizeActual = (nodeSize ?: 2.0) * lwx

        val dbNodes = DataBuilder()
        val dbEdges = DataBuilder()
        xy.addNodesData(dbNodes, rpos.alignment, imposedLeafY)
        xy.addEdgesData(dbEdges, ctype, einh, rpos, imposedLeafY, lwx, lwy, 0)
        this.nodes = dbNodes.result
        this.edges = dbEdges.result
    }

    private fun edgesLayer() = geomPolygon(
        edges,
        fill = linecolor,
        linetype = linetype,
        sampling = samplingNone
    ) {
        this.x = DendroVar.ex
        this.y = DendroVar.ey
        this.fill = aes.color
        this.linetype = aes.linetype
        this.group = DendroVar.eid
    }

    private fun nodesLayer() = geomPoint(
        nodes,
        shape = shape,
        color = color,
        alpha = alpha,
        fill = fill,
        size = if (aes.size == null) nodeSizeActual else null,
        sizeUnit = if (aes.size == null) nodeSizeUnit else null,
        sampling = samplingNone
    ) {
        x = DendroVar.nx
        y = DendroVar.ny
        this.fill = aes.fill
        this.color = aes.color
        this.shape = aes.shape
        this.size = aes.size
    }

    private fun labelsLayer() = run {
        val textSizeUnit = nodeSizeUnit
        val sizeBase = labelSize ?: nodeSize ?: 2.0
        val textSize = 4 * sizeBase
        val textSizeActual = textSize * lwx
        val textWd = sizeBase * lwx
        val textHt = sizeBase * lwy

        val lshiftX = 0.0
        val lshiftY = when (rpos) {
            Top, Right -> -3 * lwy
            Bottom, Left -> 3 * lwy
        }
        val hjust = when (rpos) {
            Top -> if (labelAngle == 0.0) "center" else "right"
            Bottom -> if (labelAngle == 0.0) "center" else "left"
            Left -> if (labelAngle == 0.0) "left" else "center"
            Right -> if (labelAngle == 0.0) "right" else "center"
        }
        val vjust = when (rpos) {
            Bottom -> if (labelAngle == 0.0) "bottom" else "center"
            Top -> if (labelAngle == 0.0) "top" else "center"
            Left -> if (labelAngle == 0.0) "center" else "top"
            Right -> if (labelAngle == 0.0) "center" else "bottom"
        }

        val dbLabels = DataBuilder()
        val dbLabelsBorder = DataBuilder()

        xy.addLabelsData(
            dbLabels,
            rpos.alignment,
            imposedLeafY,
            lshiftX,
            lshiftY
        )

        xy.addLabelBorder(
            dbLabelsBorder,
            rpos.alignment,
            imposedLeafY,
            aes.label!!,
            textWd,
            textHt,
            lshiftX,
            lshiftY + when (rpos) {
                Top, Right -> -textHt / 2
                Bottom, Left -> +textHt / 2
            }
        )

        val labels = geomText(
            dbLabels.result,
            naText = "",
            angle = labelAngle,
            size = textSizeActual,
            hjust = hjust,
            vjust = vjust,
            sizeUnit = textSizeUnit,
        ) {
            this.x = DendroVar.lx
            this.y = DendroVar.ly
            this.label = DendroVar.label
        }

        val borders = geomPath(
            dbLabelsBorder.result,
            color = linecolor,
            size = lwx / 5,
            sampling = samplingNone,
        ) {
            this.x = DendroVar.lex
            this.y = DendroVar.ley
            this.group = DendroVar.lid
        }

        val fills = geomPolygon(
            dbLabelsBorder.result,
            alpha = 0.1,
            sampling = samplingNone,
            showLegend = false
        ) {
            this.x = DendroVar.lex
            this.y = DendroVar.ley
            this.fill = aes.label
            this.group = DendroVar.lid
        }

        val r = mutableListOf<Feature>()
        r += labels

        if (fillLabels && xy.node.toList()
                .mapNotNull { it.metadata[aes.label] }
                .none { it.toString().length > 1 }
        ) {
            r += borders
            r += fills
        }

        r.toList()
    }

    override val feature: Feature
        get() = run {
            val ratio = if (rpos.isTopBottom) lwx / lwy else lwy / lwx
            var f: Feature = FeatureList(emptyList())
            if (showEdges)
                f += edgesLayer()
            if (showNodes)
                f += nodesLayer()
            if (aes.label != null) {
                labelsLayer().forEach { f += it }
            }
            if (xlim != null)
                f += xlim(xlim)
            if (ylim != null)
                f += ylim(ylim)
            f += coordFixed(ratio = ratio)
            f
        }
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
    alpha: Number? = null,
    fill: Any? = null,
    size: Double? = null,
    angle: Number? = null,
    fillLabels: Boolean = true,
    // edges
    linetype: Any? = null,
    linewidth: Double? = null,
    linewidthY: Double? = null,
    linecolor: Any? = null,
    // aes
    aesMapping: DendroAes.() -> Unit = {}
) = run {
    val aes = DendroAes().apply(aesMapping)
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
        alpha = alpha,
        fill = fill,
        nodeSize = size,
        labelAngle = angle,
        fillLabels = fillLabels,

        linetype = linetype,
        linewidth = linewidth,
        linewidthY = linewidthY,
        linecolor = linecolor,

        rpos = rpos,
        rshift = rshift,
        balanced = balanced,
        coord = coord,
        height = height,
        aes = aes,
    )
    plt += themeBlank().legendPositionRight()
    if (aes.color != null)
        plt += Palletes.Categorical.auto.colorScale(
            tree.toList().map { it.metadata[aes.color] }.distinct().filterNotNull()
        )
    plt
}

