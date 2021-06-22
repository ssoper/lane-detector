package com.seansoper.laneDetector

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.opencv.core.*
import org.opencv.core.Core.addWeighted
import org.opencv.core.Core.bitwise_and
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc.*
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import java.io.File
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.WindowConstants.EXIT_ON_CLOSE

object Core {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("laneDetector")
        val inputFilePath by parser.option(ArgType.String, shortName = "i", description = "Path to input file").default("./samples/seattle.mp4")
        val outputFilePath by parser.option(ArgType.String, shortName = "o", description = "Path to output file").default("./output.mov")
        val showLive by parser.option(ArgType.Boolean, shortName = "s", description = "Opens a window showing the live encoding").default(false)
        val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Debug").default(false)
        parser.parse(args)

        nu.pattern.OpenCV.loadShared()
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)

        val image = Mat()
        val input = VideoCapture(inputFilePath)
        val fourcc = VideoWriter.fourcc('H', '2', '6', '4')

        val outputFile = File(outputFilePath)
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val fps = input.get(Videoio.CAP_PROP_FPS)
        val size = Size(input.get(Videoio.CAP_PROP_FRAME_WIDTH),input.get(Videoio.CAP_PROP_FRAME_HEIGHT))
        val writer = VideoWriter(outputFile.absolutePath, fourcc, fps, size, true)

        if (debug) {
            println("Input file: ${File(inputFilePath).absolutePath}")
            println("Output file: ${File(outputFilePath).absolutePath}")
            println("FPS: $fps")
            println("Size: $size")
        }
        println()

        var videoPanel: JLabel? = null

        if (showLive) {
            videoPanel = JLabel()

            val frame = HighGui.createJFrame("canny", HighGui.WINDOW_AUTOSIZE)
            frame.defaultCloseOperation = EXIT_ON_CLOSE
            frame.setSize(size.width.toInt(), size.height.toInt())
            frame.contentPane = videoPanel
            frame.isVisible = true
        }

        val frameCount = input.get(Videoio.CAP_PROP_FRAME_COUNT)
        val progressBar = ProgressBar(frameCount)

        while (input.read(image)) {
            val canny = getEdges(image)
            val slice = getSlice(canny)
            val lines = getLines(slice)
            val visualized = visualize(image, lines)

            videoPanel?.icon = ImageIcon(HighGui.toBufferedImage(visualized))
            videoPanel?.repaint()

            progressBar.step()
            print("\r$progressBar")

            if (writer.isOpened) {
                writer.write(visualized)
            }
        }

        progressBar.stepToEnd()
        print("\r$progressBar")

        input.release()
        writer.release()
    }

    private fun getEdges(source: Mat): Mat {
        val gray = Mat()
        cvtColor(source, gray, COLOR_RGB2GRAY)

        val blur = Mat()
        GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

        val dest = Mat()
        Canny(blur, dest, 50.0, 150.0)

        return dest
    }

    private fun getSlice(source: Mat): Mat {
        val height = source.height().toDouble()
        val width = source.width().toDouble()

        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(175.0, height),   // bottom left
                Point(450.0, 400.0), // top left
                Point(900.0, 400.0), // top right
                Point(width, height)       // bottom right
            )
        )

        val mask = Mat.zeros(source.rows(), source.cols(), 0)
        fillPoly(mask, polygons, Scalar(255.0))

        val dest = Mat()
        bitwise_and(source, mask, dest)

        return dest
    }

    private fun getLines(source: Mat): Pair<HoughLine, HoughLine> {
        val lines = Mat()
        HoughLinesP(source, lines,2.0, Math.PI/180, 100, 100.0, 50.0)

        val left = HoughLine(source)
        val right = HoughLine(source)

        for (row in 0 until lines.rows()) {
            val points: DoubleArray = lines.get(row, 0)
            val weighted = WeightedObservedPoints()
            val fitter = PolynomialCurveFitter.create(1)

            weighted.add(points[0], points[1])
            weighted.add(points[2], points[3])

            val fitted = fitter.fit(weighted.toList())
            val slope = fitted[1]

            if (slope < 0) {
                left.add(fitted)
            } else {
                right.add(fitted)
            }
        }

        return Pair(left, right)
    }

    private fun visualize(source: Mat, lines: Pair<HoughLine, HoughLine>): Mat {
        val grey = Mat.zeros(source.rows(), source.cols(), 0)
        val dest = Mat()
        cvtColor(grey, dest, COLOR_GRAY2RGB)

        val color = Scalar(0.0, 255.0, 0.0)
        line(dest, lines.first.coordinates.first, lines.first.coordinates.second, color, LINE_8)
        line(dest, lines.second.coordinates.first, lines.second.coordinates.second, color, LINE_8)

        val done = Mat()
        addWeighted(source, 0.9, dest, 1.0, 1.0, done)

        return done
    }
}