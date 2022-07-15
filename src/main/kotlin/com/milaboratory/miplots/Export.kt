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
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.fop.activity.ContainerUtil
import org.apache.fop.configuration.Configurable
import org.apache.fop.configuration.DefaultConfigurationBuilder
import org.apache.fop.render.ps.EPSTranscoder
import org.apache.fop.svg.PDFTranscoder
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
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

enum class ExportType { PDF, EPS, SVG, PNG }

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

private fun toBytes(svg: String, type: ExportType): ByteArray {
    if (type == SVG)
        return svg.toByteArray()

    val transcoder = when (type) {
        PDF -> PDFTranscoder()
        EPS -> EPSTranscoder()
        PNG -> PNGTranscoder()
        else -> throw RuntimeException()
    }
    if (transcoder is Configurable)
        ContainerUtil.configure(transcoder, fopConfig)
    for (s in listOf(fixFonts(svg), svg)) { // try to replace fonts and write
        val input = TranscoderInput(ByteArrayInputStream(s.toByteArray()))
        try {
            return ByteArrayOutputStream().use { byteArrayOutputStream ->
                val output = TranscoderOutput(byteArrayOutputStream)
                transcoder.transcode(input, output)
                byteArrayOutputStream.toByteArray()
            }
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }
    }
    throw RuntimeException()
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
