package com.seansoper.laneDetector

import kotlin.math.ceil

class ProgressBar(private val total: Double, private val width: Int = 20) {

    private var current: Double = 0.0

    fun step() {
        current++
    }

    fun stepToEnd() {
        current = total
    }

    override fun toString(): String {
        val progress = ceil((current/total)*100).toInt()
        val progressChar = ceil((current/total)*width).toInt()
        var emptySpace = width-progressChar+1

        if (current == total) {
            emptySpace = 0
        }

        return "Progress [${"=".repeat(progressChar)}>${" ".repeat(emptySpace)}] $progress%"
    }

}