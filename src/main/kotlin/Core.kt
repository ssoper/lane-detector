package com.seansoper.laneDetector

import org.jetbrains.kotlinx.multik.api.empty
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.operations.flatMap
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import org.opencv.core.*
import org.opencv.core.Core.bitwise_and
import org.opencv.core.CvType.CV_32S
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import java.io.File
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

        var count = 0

        while (input.read(image)) {
            val canny = canny(image)
            val segment = segment(canny) // should be canny

//            println(segment)

            if (writer.isOpened) {
                writer.write(canny)
                //println("Wrote to file")

//                if (count == 1) {
                    println("wtf")
                    videoPanel.icon = ImageIcon(HighGui.toBufferedImage(segment))
                    videoPanel.repaint()
//                }
                count++
            }
        }

        input.release()
        writer.release()
    }

    private fun canny(source: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(source, gray, COLOR_RGB2GRAY)

        val blur = Mat()
        Imgproc.GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

        val dest = Mat()
        Imgproc.Canny(blur, dest, 50.0, 150.0)

        return dest
    }

    private fun segment(source: Mat): Mat {
        val height = source.height().toDouble()
        val width = source.width().toDouble()
//        val polygons = mk.ndarray(mk[mk[0, height], mk[1200, height], mk[380, 290]])
//        println(source.type())
        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(200.0, height),
                Point(400.0, 200.0),
                Point(1000.0, 200.0),
                Point(width, height)
            )
        )

        val polygons2: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(50.0, 50.0),
                Point(100.0, 50.0),
                Point(100.0, 100.0),
                Point(50.0, 100.0)
            )
        )

        // val mask = mk.empty<Double, D2>(source.width(), source.height())
        val mask = Mat.zeros(source.rows(), source.cols(), 0)
//        println(polygons)
//        println(polygons.flatten())
//        println(mask)
//        polygons.toList().forEach()
//        MatOfPoint().fromArray()
        fillPoly(mask, polygons, Scalar(255.0))

//        return mask
        val dest = Mat()
        bitwise_and(source, mask, dest)

        return dest
    }
}