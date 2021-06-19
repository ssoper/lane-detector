package com.seansoper.laneDetector

import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.apache.commons.math3.stat.StatUtils
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.opencv.core.*
import org.opencv.core.Core.addWeighted
import org.opencv.core.Core.bitwise_and
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import java.io.File
import java.lang.Double.NaN
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import kotlin.system.exitProcess

object Core {
    @JvmStatic
    fun main(args: Array<String>) {
        nu.pattern.OpenCV.loadShared()
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        println("hello you")
        val image = Mat()
        val input = VideoCapture("/Users/ssoper/Downloads/seattle_driving.mp4")
        val fourcc = VideoWriter.fourcc('H', '2', '6', '4')
        // TODO: look closer at why i couldn't use mp4
        val outputFile = File("/Users/ssoper/workspace/lane-detector/", "output.mov").absolutePath
        val fps = input.get(Videoio.CAP_PROP_FPS)
        val size = Size(input.get(Videoio.CAP_PROP_FRAME_WIDTH),input.get(Videoio.CAP_PROP_FRAME_HEIGHT))
        val writer = VideoWriter(outputFile, fourcc, fps, size, true)

        println("FPS: $fps, Size: $size")

        val frame = HighGui.createJFrame("canny", HighGui.WINDOW_AUTOSIZE)
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.setSize(size.width.toInt(), size.height.toInt())

        val videoPanel = JLabel()
        frame.contentPane = videoPanel
        frame.isVisible = true

        while (input.read(image)) {
            val canny = canny(image)
            val segment = segment(canny)
            val lines = getLines(segment)
            val visualized = visualize(image, lines)

            if (writer.isOpened) {
                writer.write(canny)

                videoPanel.icon = ImageIcon(HighGui.toBufferedImage(visualized))
                videoPanel.repaint()
            }
        }

        input.release()
        writer.release()
    }

    private fun canny(source: Mat): Mat {
        val gray = Mat()
        cvtColor(source, gray, COLOR_RGB2GRAY)

        val blur = Mat()
        GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

        val dest = Mat()
        Canny(blur, dest, 50.0, 150.0)

        return dest
    }

    private fun segment(source: Mat): Mat {
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

    private class HoughLine(val source: Mat) {
        val slope: MutableList<Double> = mutableListOf()
        val yIntercept: MutableList<Double> = mutableListOf()

        val slopeAvg: Double by lazy {
            StatUtils.mean(slope.toDoubleArray())
        }

        val yInterceptAvg: Double by lazy {
            StatUtils.mean(yIntercept.toDoubleArray())
        }

        val coordinates: Pair<Point, Point>
            get() {
                val y1 = source.height()

                println(slope.toString())
                println("slopeAvg ${slopeAvg}")
                println("#####")
                println(yIntercept.toString())
                println("yinterceptAvg ${yInterceptAvg}")

                // (720-150-321)/0.12
                // 60/(3-(10+20+30)/80) axis=0 by column
                // y=ax+b
                // 72=(0.12)x+321

                return Pair(
                    Point((y1-yInterceptAvg)/slopeAvg, y1.toDouble()),
                    Point((y1-150-yInterceptAvg)/slopeAvg,y1.toDouble()-150)
                )
            }

        fun add(fitted: DoubleArray) {
            slope.add(fitted[1])
            yIntercept.add(fitted[0])
        }
    }

    private fun getLines(source: Mat): Pair<HoughLine, HoughLine> {
        val lines = Mat()
        HoughLinesP(source, lines,2.0, Math.PI/180, 100, 100.0, 50.0)

        val left = HoughLine(source)
        val right = HoughLine(source)

//        println("size: ${lines.size()}, dims ${lines.dims()}, channels ${lines.channels()}")
//        println("rows ${lines.rows()}, cols ${lines.cols()}")
//        println("cv4 ${lines.checkVector(4)}")

        for (row in 0 until lines.rows()) {
            val points: DoubleArray = lines.get(row, 0)
            val pointA = Point(points[0], points[1])
            val pointB = Point(points[2], points[3])
            val weighted = WeightedObservedPoints()
            weighted.add(points[0], points[1])
            weighted.add(points[2], points[3])
            val fitter = PolynomialCurveFitter.create(1)
            val fitted = fitter.fit(weighted.toList())
            val (yIntercept, slope) = fitted

//            val c = mk.ndarray(mk[mk[1.0, 2.0]])
//            mk.stat.mean<Double, D2, D2>(c, 1)

            println("******")
            if (slope < 0) {
                left.add(fitted)
                println("LEFT")
            } else {
                right.add(fitted)
                println("RIGHT")
            }

            println("point A $pointA, B $pointB")
            println("yIntercept $yIntercept, slope $slope")
        }

//        if (left.coordinates == null) {
//
//        }

        /*

        println("left ${left.coordinates}")
        println("@@@@@")
        println("right ${right.coordinates}")
        println("%%%%%%")
        println()
         */

//        exitProcess(1)
//        println(lines)
/*
        for (row in 0..lines.rows()) {
            for (col in 0..lines.cols()) {
                val thing = lines.get(row, col)
                println(thing)
            }
        }
        println("end!")
        */

//        println(lines.step1())
//        println(lines.step1())

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