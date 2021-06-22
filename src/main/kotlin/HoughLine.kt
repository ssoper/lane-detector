package com.seansoper.laneDetector

import org.apache.commons.math3.stat.StatUtils
import org.opencv.core.Mat
import org.opencv.core.Point

class HoughLine(private val source: Mat) {
    private val slope: MutableList<Double> = mutableListOf()
    private val yIntercept: MutableList<Double> = mutableListOf()

    private val slopeAvg: Double by lazy {
        StatUtils.mean(slope.toDoubleArray())
    }

    private val yInterceptAvg: Double by lazy {
        StatUtils.mean(yIntercept.toDoubleArray())
    }

    val coordinates: Pair<Point, Point>
        get() {
            val y1 = source.height()

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