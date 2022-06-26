@file:Suppress("LocalVariableName", "ClassName")

package com.milaboratory.miplots.dendro

import com.milaboratory.miplots.*
import com.milaboratory.miplots.Position.*
import com.milaboratory.miplots.color.DiscretePalette
import com.milaboratory.miplots.color.Palettes
import com.milaboratory.miplots.dendro.Alignment.Horizontal
import com.milaboratory.miplots.dendro.Alignment.Vertical
import jetbrains.letsPlot.coordFixed
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomPolygon
import jetbrains.letsPlot.geom.geomText
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.FeatureList
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.sampling.samplingNone
import jetbrains.letsPlot.scale.scaleColorManual
import jetbrains.letsPlot.scale.scaleFillManual
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
//        val values = valuese
//        val n = values.firstOrNull()?.size ?: 0
//        val newRows = (0 until n).map { iRow ->
//            mapper(keys.associateWith { key -> this[key]!![iRow] })
//        }
//        keys.associateWith { key -> newRows.map { it[key] } }
//    }

internal object DendroVar {
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

internal fun XYNode.yImposed(imposedLeafY: Double?) = if (isLeaf) imposedLeafY ?: y else y

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

    /** Node size */
    var size: Any? = null

    /** Edge linetype */
    var linetype: Any? = null
}

enum class ConnectionType { Triangle, Rectangle }
enum class EdgeMetaInheritance { Up, Down }

internal val Position.alignment
    get() = when (this) {
        Top, Bottom -> Vertical
        else -> Horizontal
    }

internal val Position.ysign
    get() = when (this) {
        Top, Right -> -1.0
        Bottom, Left -> 1.0
    }

class ggDendro(
    tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,

    // tree layout
    val showNodes: Boolean = true,
    val showEdges: Boolean = true,
    val center: Boolean = true,
    val rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,

    // nodes
    val nodeShape: Any? = null,
    val nodeColor: Any? = null,
    val nodeAlpha: Number? = null,
    val nodeFill: Any? = null,
    val nodeSize: Double? = null,

    // edges
    val lineType: Any? = null,
    val lineWidth: Double? = null,
    val lineWidthY: Double? = null,
    val lineColor: Any? = null,

    // colors
    val colorPalette: DiscretePalette? = null,

    // aes
    val aes: DendroAes = DendroAes()
) : FeatureWrapper {
    internal val xy: XYNode
    internal val lwx: Double
    internal val lwy: Double
    internal val imposedLeafY: Double?
    internal val xlim: Pair<Double, Double>?
    internal val ylim: Pair<Double, Double>?
    internal val nodeSizeActual: Double
    internal val nodeSizeUnit: String
    internal val nodes: Map<Any, List<Any?>>
    internal val edges: Map<Any, List<Any?>>

    var yDelta = 0.0

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

        val lwx = lineWidth ?: xy.width.let {
            if (it != 0.0)
                xy.width / xy.node.leafCount / 5
            else
                xy.height / 10
        }

        val lwy = lineWidthY ?: xy.width.let {
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
        fill = lineColor,
        linetype = lineType,
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
        shape = nodeShape,
        color = nodeColor,
        alpha = nodeAlpha,
        fill = nodeFill,
        size = if (aes.size == null) nodeSizeActual else null,
        sizeUnit = if (aes.size == null) nodeSizeUnit else null,
        sampling = samplingNone
    ) {
        x = DendroVar.nx
        y = DendroVar.ny
        this.color = aes.color
        this.shape = aes.shape
        this.size = aes.size
    }

    val annotationLayers = mutableListOf<Feature>()

    override val feature: Feature
        get() = run {
            val ratio = if (rpos.isTopBottom) lwx / lwy else lwy / lwx
            var f: Feature = FeatureList(emptyList())
            if (showEdges)
                f += edgesLayer()
            if (showNodes)
                f += nodesLayer()

            annotationLayers.forEach { f += it }

            if (xlim != null)
                f += xlim(xlim)
            if (ylim != null)
                f += ylim(ylim)
            if (yDelta != 0.0) {
                // add empty element to fix layout
                val (x, y) = rpos.alignment.apply(Point(0.0, xy.leafY + yDelta))
                f += geomText(
                    x = x,
                    y = y,
                    size = nodeSize,
                    sizeUnit = nodeSizeUnit,
                    label = "",
                    color = "red"
                )
            }

            if (aes.color != null) {
                val breaks = xy.node.toList().mapNotNull { it.metadata[aes.color] }.distinct()
                val colors = (colorPalette ?: Palettes.Categorical.auto(breaks.size))
                f += scaleFillManual(
                    values = colors.colors,
                    breaks = breaks,
                    name = aes.color.toString()
                )
                f += scaleColorManual(
                    values = colors.colors,
                    breaks = breaks,
                    name = aes.color.toString()
                )
            }

            f += coordFixed(ratio = ratio)
            f
        }
}

class GGDendroPlot constructor(
    val tree: Node<*>,
    ctype: ConnectionType = ConnectionType.Rectangle,
    einh: EdgeMetaInheritance = EdgeMetaInheritance.Up,

    // tree layout
    showNodes: Boolean = true,
    showEdges: Boolean = true,
    center: Boolean = true,
    rpos: Position = Top,
    balanced: Boolean = false,
    coord: List<Double>? = null,
    rshift: Double? = null,
    height: Double? = null,

    // nodes
    nodeShape: Any? = null,
    nodeColor: Any? = null,
    nodeAlpha: Number? = null,
    nodeFill: Any? = null,
    nodeSize: Double? = null,

    // edges
    lineType: Any? = null,
    lineWidth: Double? = null,
    lineWidthY: Double? = null,
    lineColor: Any? = null,

    // colors
    colorPalette: DiscretePalette? = null,

    // aes
    aesMapping: DendroAes.() -> Unit = {}
) : PlotWrapper {
    val aes = DendroAes().apply(aesMapping)

    val ggDendro = ggDendro(
        tree = tree,
        ctype = ctype,
        einh = einh,
        showNodes = showNodes,
        showEdges = showEdges,
        center = center,

        nodeShape = nodeShape,
        nodeColor = nodeColor,
        nodeAlpha = nodeAlpha,
        nodeFill = nodeFill,
        nodeSize = nodeSize,

        lineType = lineType,
        lineWidth = lineWidth,
        lineWidthY = lineWidthY,
        lineColor = lineColor,

        rpos = rpos,
        rshift = rshift,
        balanced = balanced,
        coord = coord,
        height = height,

        colorPalette = colorPalette,

        aes = aes,
    )

    val features = mutableListOf<Feature>()

    override var plot: Plot
        get() = run {
            var plt = letsPlot()
            plt += ggDendro
            plt += themeBlank().legendPositionRight()
            if (aes.color != null)
                plt += Palettes.Categorical.auto.colorScale(
                    tree.toList().map { it.metadata[aes.color] }.distinct().filterNotNull()
                )
            plt + FeatureList(features)
        }
        set(value) {}
}

operator fun GGDendroPlot.plusAssign(f: Feature) = run {
    features += f
}

operator fun GGDendroPlot.
        plus(f: Feature) = run {
    features += f
    this
}
