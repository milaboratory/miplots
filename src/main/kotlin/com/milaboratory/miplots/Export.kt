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

import com.milaboratory.miplots.ExportType.EPS
import com.milaboratory.miplots.ExportType.PDF
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.Figure
import jetbrains.letsPlot.GGBunch
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.fop.activity.ContainerUtil
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

fun toPDF(svg: String) = toVector(svg, PDF)
fun toEPS(svg: String) = toVector(svg, EPS)

enum class ExportType { PDF, EPS }

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

private fun toVector(svg: String, type: ExportType) = run {
    val pdfTranscoder = if (type == PDF) PDFTranscoder() else EPSTranscoder()
    ContainerUtil.configure(pdfTranscoder, fopConfig)
    val input = TranscoderInput(ByteArrayInputStream(svg.toByteArray()))
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val output = TranscoderOutput(byteArrayOutputStream)
        pdfTranscoder.transcode(input, output)
        byteArrayOutputStream.toByteArray()
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
