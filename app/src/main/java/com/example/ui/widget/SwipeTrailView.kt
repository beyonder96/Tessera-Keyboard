package com.example.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class SwipeTrailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class TimedPoint(val x: Float, val y: Float, val time: Long)

    private val points = mutableListOf<TimedPoint>()
    private val path = Path()

    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
        strokeWidth = 10f
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.argb(80, 255, 255, 255)
        strokeWidth = 18f
    }

    private val maxAgeMs = 250L

    init {
        isClickable = false
        isFocusable = false
    }

    fun setTrailColor(color: Int) {
        trailPaint.color = color
        val alpha = (Color.alpha(color) * 0.35f).toInt()
        glowPaint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun addPoint(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        points.add(TimedPoint(x, y, now))
        cleanupOldPoints(now)
        invalidate()
    }

    fun clearTrail() {
        points.clear()
        invalidate()
    }

    private fun cleanupOldPoints(now: Long) {
        points.removeAll { now - it.time > maxAgeMs }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis()
        cleanupOldPoints(now)

        if (points.size < 2) return

        path.reset()
        val first = points[0]
        path.moveTo(first.x, first.y)

        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            path.quadTo(p0.x, p0.y, midX, midY)
        }
        val last = points.last()
        path.lineTo(last.x, last.y)

        // Desenha brilho difuso externo
        canvas.drawPath(path, glowPaint)
        // Desenha trilha sólida interna
        canvas.drawPath(path, trailPaint)

        // Se ainda há pontos, agenda redesenho para suavizar o desaparecimento
        if (points.isNotEmpty()) {
            postInvalidateDelayed(16)
        }
    }
}
