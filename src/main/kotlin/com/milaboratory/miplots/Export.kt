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
package com.milaboratory.miplots

import com.milaboratory.miplots.ExportType.*
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.Figure
import jetbrains.letsPlot.GGBunch
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.DocumentLoader
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.gvt.GraphicsNode
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.apache.fop.activity.ContainerUtil
import org.apache.fop.configuration.Configurable
import org.apache.fop.configuration.DefaultConfigurationBuilder
import org.apache.fop.render.ps.EPSTranscoder
import org.apache.fop.svg.PDFTranscoder
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText


fun Figure.toSpec() = when (this) {
    is Plot -> this.toSpec()
    is GGBunch -> this.toSpec()
    else -> throw RuntimeException()
}

fun Figure.toSvg() = PlotSvgExport.buildSvgImageFromRawSpecs(toSpec())
fun Figure.toPDF() = toPDF(toSvg())
fun Figure.toEPS() = toEPS(this.toSvg())
fun toPDF(svg: String) = toBytes(svg, PDF)
fun toEPS(svg: String) = toBytes(svg, EPS)

enum class ExportType {
    PDF, EPS, SVG, PNG, JPEG;

    companion object {
        @JvmStatic
        fun determine(path: Path) = run {
            val name = path.fileName.toString()
            if (name.endsWith(".pdf"))
                PDF
            else if (name.endsWith(".svg"))
                SVG
            else if (name.endsWith(".eps"))
                EPS
            else if (name.endsWith(".png"))
                PNG
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
                JPEG
            else
                throw IllegalArgumentException("un")
        }
    }
}

private val javaClass = object {}.javaClass

private val fopConfig = DefaultConfigurationBuilder()
    .buildFromFile(javaClass.getResourceAsStream("/fonts/fopconf.xml")?.use {
        val xml = BufferedReader(InputStreamReader(it)).use { bf ->
            bf.readLines()
                .joinToString("")
                .replace(
                    "IBM_PLEX_MONO_PATH",
                    javaClass.getResource("/fonts/IBM_Plex_Mono/IBMPlexMono-Text.ttf")!!.toURI().toString()
                )
        }
        val file = Files.createTempFile("fopconf", "xml")
        file.writeText(xml)
        file.toFile()
    })

val fontRegex = """(font:\d+\.\d+E-(\d{2}|[2-9])px)""".toRegex()
private fun fixFonts(svg: String) = run {
    var fixed = svg
    // replace font family
    fixed = fixed.replace("font-family:", "font-family: \"IBM Plex Mono\",")
    // replace dimension fonts not supported by batik
    fixed = fontRegex.replace(fixed, "font:0px")

    fixed
}

private fun svgSize(svg: String) = run {
    val saxFactory = SAXSVGDocumentFactory(
        XMLResourceDescriptor.getXMLParserClassName()
    )
    val document = saxFactory.createDocument(
        "file:temp",
        StringReader(svg)
    )
    val agent = UserAgentAdapter()
    val loader = DocumentLoader(agent)
    val context = BridgeContext(agent, loader)
    context.isDynamic = true
    val builder = GVTBuilder()
    val root: GraphicsNode = builder.build(context, document)
    Dimensions(root.primitiveBounds.width, root.primitiveBounds.height)
}

private data class Dimensions(val w: Double, val h: Double)

private const val DEFAULT_DPI = 1200f
private const val DIMENSION_SCALE = DEFAULT_DPI / 72f
private const val PIXEL_UNIT_TO_MILLIMETER = 25.4f / DEFAULT_DPI

private fun toBytes(svg: String, type: ExportType): ByteArray {
    if (type == SVG)
        return svg.toByteArray()

    val transcoder = when (type) {
        PDF -> PDFTranscoder()
        EPS -> EPSTranscoder()
        PNG -> {
            val t = PNGTranscoder()
            val (w, h) = svgSize(svg)
            val sw = w * DIMENSION_SCALE
            val sh = h * DIMENSION_SCALE
            t.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, PIXEL_UNIT_TO_MILLIMETER.toFloat())
            t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, sw.toFloat())
            t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, sh.toFloat())
            t
        }
        JPEG -> {
            val t = JPEGTranscoder()
            t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.95)
            t
        }
        else -> throw RuntimeException()
    }
    if (transcoder is Configurable)
        ContainerUtil.configure(transcoder, fopConfig)

    val svgFixed = fixFonts(svg)
    val input = TranscoderInput(ByteArrayInputStream(svgFixed.toByteArray()))

    return ByteArrayOutputStream().use { byteArrayOutputStream ->
        val output = TranscoderOutput(byteArrayOutputStream)
        transcoder.transcode(input, output)
        byteArrayOutputStream.toByteArray()
    }
}

fun writeFile(destination: Path, plots: List<Plot>) {
    val type = ExportType.determine(destination)
    if (type != PDF && plots.size > 1)
        throw IllegalArgumentException("$type does not allow to write multiple plots in one file")
    when (type) {
        PDF -> writePDF(destination, plots)
        else -> destination.writeBytes(toBytes(plots[0].toSvg(), type))
    }
}

fun writeEPS(destination: Path, image: ByteArray) {
    destination.writeBytes(image)
}

fun writePDF(destination: Path, vararg images: ByteArray) {
    writePDF(destination, images.toList())
}

fun writePDF(destination: Path, vararg plots: Figure) {
    writePDF(destination, plots.toList())
}

@JvmName("writePDFFigure")
fun writePDF(destination: Path, plots: List<Figure>) {
    writePDF(destination, plots.map { it.toPDF() })
}

@JvmName("writePDFPlotWrapper")
fun writePDF(destination: Path, plots: List<PlotWrapper>) {
    writePDF(destination, plots.map { it.toPDF() })
}

fun writePDF(destination: Path, images: List<ByteArray>) {
    val merger = PDFMergerUtility()
    merger.destinationFileName = destination.absolutePathString()

    for (image in images) {
        merger.addSource(ByteArrayInputStream(image))
    }

    merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
}

fun PlotWrapper.toSpec() = this.plot.toSpec()
fun PlotWrapper.toSvg() = PlotSvgExport.buildSvgImageFromRawSpecs(toSpec())
fun PlotWrapper.toPDF() = toPDF(toSvg())
fun PlotWrapper.toEPS() = toEPS(this.toSvg())
fun writePDF(destination: Path, vararg plots: PlotWrapper) {
    writePDF(destination, plots.toList().map { it.toPDF() })
}
