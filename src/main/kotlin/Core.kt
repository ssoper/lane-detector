package com.seansoper.laneDetector

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY
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

            if (writer.isOpened) {
                writer.write(canny)
                //println("Wrote to file")

                videoPanel.icon = ImageIcon(HighGui.toBufferedImage(canny))
                videoPanel.repaint()
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
}