package com.milaboratory.statplots.util

import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.Figure
import jetbrains.letsPlot.GGBunch
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.fop.render.ps.EPSTranscoder
import org.apache.fop.svg.PDFTranscoder
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeBytes


fun Figure.toSpec() = when (this) {
    is Plot -> this.toSpec()
    is GGBunch -> this.toSpec()
    else -> throw RuntimeException()
}

fun Figure.toSvg() = PlotSvgExport.buildSvgImageFromRawSpecs(toSpec())
fun Figure.toPDF() = toPDF(toSvg())
fun Figure.toEPS() = toEPS(this.toSvg())

fun toPDF(svg: String) = toVector(svg, ExportType.PDF)
fun toEPS(svg: String) = toVector(svg, ExportType.EPS)

enum class ExportType { PDF, EPS }

private fun toVector(svg: String, type: ExportType) = run {
    val pdfTranscoder = if (type == ExportType.PDF) PDFTranscoder() else EPSTranscoder()
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

fun writePDF(destination: Path, images: List<ByteArray>) {
    val merger = PDFMergerUtility()
    merger.destinationFileName = destination.absolutePathString()

    for (image in images) {
        merger.addSource(ByteArrayInputStream(image))
    }

    merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
}
