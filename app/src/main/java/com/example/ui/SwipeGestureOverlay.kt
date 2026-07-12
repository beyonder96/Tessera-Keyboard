package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

class SwipeGestureOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.parseColor("#06FBFB")
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        alpha = 255
    }


    var onSwipeComplete: ((String) -> Unit)? = null
    var onSwipeChar: ((String) -> Unit)? = null
    var onSwipeStart: (() -> Unit)? = null
    var onKeyDown: ((View, String) -> Unit)? = null
    var onKeyUp: (() -> Unit)? = null


    private val keys = mutableListOf<Pair<TextView, String>>()
    private val swipeWord = StringBuilder()
    private var lastKey: String? = null
    private var isSwiping = false
    private var startX = 0f
    private var startY = 0f
    private val swipePoints = mutableListOf<PointF>()

    fun setKeys(keyViews: List<Pair<TextView, String>>) {
        keys.clear()
        keys.addAll(keyViews)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!path.isEmpty) {
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                val hit = checkKeyHit(x, y)
                if (hit == null) return false // Let it pass to command keys
                
                startX = x
                startY = y
                swipePoints.clear()
                swipePoints.add(PointF(x, y))
                path.reset()
                path.moveTo(x, y)
                swipeWord.clear()
                lastKey = null
                isSwiping = false
                onKeyDown?.invoke(hit.first, hit.second)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - startX)
                val dy = Math.abs(y - startY)
                if (dx > 10f || dy > 10f) {
                    if (!isSwiping) {
                        isSwiping = true
                        onSwipeStart?.invoke()
                    }
                }
                swipePoints.add(PointF(x, y))
                drawSmoothSwipe()
                checkKeyHit(x, y)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onKeyUp?.invoke()
                if (isSwiping && swipeWord.length > 1) {
                    onSwipeComplete?.invoke(swipeWord.toString())
                } else if (swipeWord.length > 0) {
                    onSwipeChar?.invoke(swipeWord.toString())
                }
                swipePoints.clear()
                path.reset()
                invalidate()
                isSwiping = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }


    private fun checkKeyHit(x: Float, y: Float): Pair<TextView, String>? {
        for ((view, char) in keys) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            
            val myLocation = IntArray(2)
            getLocationInWindow(myLocation)
            
            val viewX = location[0] - myLocation[0]
            val viewY = location[1] - myLocation[1]
            
            if (x >= viewX && x <= viewX + view.width &&
                y >= viewY && y <= viewY + view.height) {
                if (char != lastKey) {
                    swipeWord.append(char)
                    lastKey = char
                }
                return Pair(view, char)
            }
        }
        return null
    }
    
    fun setThemeColor(color: Int) {
        paint.color = color
        paint.alpha = 180
        invalidate()
    }
    
    private fun drawSmoothSwipe() {
        path.reset()
        if (swipePoints.size < 2) return

        path.moveTo(swipePoints[0].x, swipePoints[0].y)
        
        for (i in 1 until swipePoints.size - 1) {
            val p1 = swipePoints[i]
            val p2 = swipePoints[i + 1]
            
            // Calcula o ponto médio para suavizar a curva
            val midX = (p1.x + p2.x) / 2
            val midY = (p1.y + p2.y) / 2
            
            path.quadTo(p1.x, p1.y, midX, midY)
        }
        
        // Linha final até o último ponto
        val lastPoint = swipePoints.last()
        path.lineTo(lastPoint.x, lastPoint.y)
        
        invalidate()
    }
}
