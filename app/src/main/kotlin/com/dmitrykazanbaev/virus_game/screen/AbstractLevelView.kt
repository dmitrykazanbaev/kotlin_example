package com.dmitrykazanbaev.virus_game.screen

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import com.dmitrykazanbaev.virus_game.R
import com.dmitrykazanbaev.virus_game.model.Building
import com.dmitrykazanbaev.virus_game.model.level.AbstractLevel


abstract class AbstractLevelView(context: Context, protected val level: AbstractLevel) : SurfaceView(context), SurfaceHolder.Callback {
    protected val background: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.background)
    protected val paintForFilling = Paint()
    protected val paintForStroke = Paint()
    protected val scrollGestureDetector = GestureDetector(context, MyGestureListener())
    protected val scaleGestureDetector = ScaleGestureDetector(context, MyGestureListener())

    private var drawThread: DrawThread? = null

    private var xOffset = 0f
    private var yOffset = 0f
    private var scaleFactor = 1f

    private var minScaleFactor = scaleFactor
    private var maxScaleFactor = scaleFactor

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
            return true
        }

        override fun onScaleEnd(p0: ScaleGestureDetector?) {
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            scaleFactor *= detector?.scaleFactor!!
            if (scaleFactor < minScaleFactor)
                scaleFactor = minScaleFactor
            if (scaleFactor > maxScaleFactor)
                scaleFactor = maxScaleFactor

            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            xOffset += distanceX
            if (xOffset / scaleFactor < level.minPoint.x) {
                xOffset = level.minPoint.x * scaleFactor
            }
            if (xOffset / scaleFactor > level.minPoint.x + (level.width - width)) {
                xOffset = (level.minPoint.x + (level.width - width)) * scaleFactor
            }
            /*if (xOffset < level.minPoint.x) {
                xOffset = level.minPoint.x.toFloat()
            }
            if (xOffset > level.minPoint.x + (level.width - width)) {
                xOffset = ((level.minPoint.x + (level.width - width)).toFloat())
            }*/

            yOffset += distanceY
            if (yOffset / scaleFactor < level.minPoint.y) {
                yOffset = level.minPoint.y * scaleFactor
            }
            if (yOffset / scaleFactor > level.minPoint.y + (level.height - height)) {
                yOffset = (level.minPoint.y + (level.height - height)) * scaleFactor
            }
            /*if (yOffset < level.minPoint.y) {
                yOffset = level.minPoint.y.toFloat()
            }
            if (yOffset > level.minPoint.y + (level.height - height)) {
                yOffset = ((level.minPoint.y + (level.height - height)).toFloat())
            }*/
            Log.w("dmka", "${xOffset} ${yOffset}")

            return true
        }
    }

    init {
        paintForFilling.style = Paint.Style.FILL

        paintForStroke.style = Paint.Style.STROKE
        paintForStroke.strokeWidth = resources.getString(R.string.strokeWidth).toFloat()
        paintForStroke.color = Color.BLACK
    }

    inner class DrawThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        var runFlag = false

        override fun run() {
            var canvas: Canvas?
            while (runFlag) {
                canvas = surfaceHolder.lockCanvas()

                synchronized(surfaceHolder) {
                    canvas?.let { draw(it) }
                }

                canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
            }
        }

        fun draw(canvas: Canvas) {
            canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)
            canvas.translate(-xOffset / scaleFactor, -yOffset / scaleFactor)

            //canvas.drawBitmap(background, 0f, 0f, paintForFilling)
            canvas.drawColor(ContextCompat.getColor(context, R.color.colorBackground))

            level.buildings.forEach {
                drawBuilding(it, canvas)
            }
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        var retry = true
        drawThread?.runFlag = false
        while (retry) {
            try {
                drawThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                throw Exception("DrawThread stopping problem")
            }
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        scaleFactor = minOf(width.toFloat() / level.width,height.toFloat() / level.height)
        scaleFactor *= 0.9f
        minScaleFactor = scaleFactor
        maxScaleFactor = 3 * minScaleFactor

        //TODO
        Log.w("dmka", "${level.minPoint} ${level.maxPoint}")
        //xOffset = (level.minPoint.x + (level.width - width)).toFloat()//level.minPoint.x / scaleFactor
        //yOffset = (level.minPoint.y + (level.height - height)).toFloat()//level.minPoint.y / scaleFactor
        Log.w("dmka", "$xOffset $yOffset")

        drawThread = DrawThread(holder)
        drawThread?.runFlag = true
        drawThread?.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return scrollGestureDetector.onTouchEvent(event) && scaleGestureDetector.onTouchEvent(event)
    }

    fun drawBuilding(building: Building, canvas: Canvas?) {
        drawLeftSideBuilding(building, canvas)
        drawCenterSideBuilding(building, canvas)
        drawRoofBuilding(building, canvas)
    }

    fun drawLeftSideBuilding(building: Building, canvas: Canvas?) {
        paintForFilling.color = Color.BLACK

        canvas?.drawPath(building.leftSide, paintForFilling)
    }

    fun drawCenterSideBuilding(building: Building, canvas: Canvas?) {
        paintForFilling.color = R.color.colorCenter

        canvas?.drawPath(building.centerSide, paintForFilling)
        canvas?.drawPath(building.centerSide, paintForStroke)
    }

    fun drawRoofBuilding(building: Building, canvas: Canvas?) {
        paintForFilling.color = Color.WHITE

        canvas?.drawPath(building.roof, paintForFilling)
        canvas?.drawPath(building.roof, paintForStroke)
    }
}