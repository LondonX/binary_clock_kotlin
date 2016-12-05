package com.londonx.binwallpaper.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.londonx.binwallpaper.R
import java.util.*

/**
 * Created by London on 2016/12/3.
 *
 */
const val ROW_COUNT: Int = 4
const val COLUMN_COUNT: Int = 6

class LiveWallpaper : WallpaperService() {
    val dotSize: Int by lazy { resources.getDimensionPixelSize(R.dimen.dot_size) }
    val margin: Int by lazy { resources.getDimensionPixelSize(R.dimen.margin) * 2 }

    override fun onCreateEngine(): Engine {
        return MyEngine()
    }

    inner class MyEngine : Engine() {
        val dotPaint: Paint by lazy {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = Color.WHITE
            p
        }
        val bgPaint: Paint by lazy {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = Color.DKGRAY
            p
        }
        var canvas: Canvas? = null
        val bgPoints: Array<Point> by lazy {
            val centerX = canvas!!.width / 2
            val centerY = canvas!!.height / 2
            val startX = centerX - dotSize * COLUMN_COUNT / 2 - margin * 2
            val startY = centerY - dotSize * ROW_COUNT / 2 - margin * 2
            Array(24, { i ->
                Point((startX + (i % COLUMN_COUNT) * dotSize + (i % COLUMN_COUNT) * margin).toFloat(),
                        (startY + (i / COLUMN_COUNT) * dotSize + (i / COLUMN_COUNT) * margin).toFloat())
            })
        }

        var timer: Timer = Timer()
        var timerTask: TimerTask? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            timerTask?.cancel()
            if (visible) {
                timerTask = object : TimerTask() {
                    var hourBin: String = ""
                    var minBin: String = ""
                    var secBin: String = ""
                    override fun run() {
                        canvas = surfaceHolder.lockCanvas()
                        if (canvas == null) {
                            timerTask?.cancel()
                            return
                        }
                        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
                        val min = Calendar.getInstance()[Calendar.MINUTE]
                        val sec = Calendar.getInstance()[Calendar.SECOND]
                        hourBin = Integer.toBinaryString(hour)
                        minBin = Integer.toBinaryString(min)
                        secBin = Integer.toBinaryString(sec)
                        while (hourBin.length < 8) {
                            hourBin = "0" + hourBin
                        }
                        while (minBin.length < 8) {
                            minBin = "0" + minBin
                        }
                        while (secBin.length < 8) {
                            secBin = "0" + secBin
                        }
                        canvas!!.drawColor(Color.BLACK)
                        bgPoints.forEachIndexed { i, point ->
                            val column = i % COLUMN_COUNT
                            val row = i / COLUMN_COUNT
                            if (column == 0 && (row == 0 || row == 1)) {
                                return@forEachIndexed
                            }
                            if (row == 0 && (column == 2 || column == 4)) {
                                return@forEachIndexed
                            }
                            if (column == 0 && 49 == hourBin[row].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else if (column == 1 && 49 == hourBin[row + 4].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else if (column == 2 && 49 == minBin[row].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else if (column == 3 && 49 == minBin[row + 4].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else if (column == 4 && 49 == secBin[row].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else if (column == 5 && 49 == secBin[row + 4].toInt()) {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), dotPaint)
                            } else {
                                canvas!!.drawCircle(point.x, point.y, dotSize.toFloat(), bgPaint)
                            }
                        }
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                timer.schedule(timerTask, 1000, 1000)
            }
        }
    }

    data class Point(val x: Float, val y: Float)
}