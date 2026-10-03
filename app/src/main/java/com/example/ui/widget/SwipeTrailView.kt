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

    data class TimedPoint(val x: Float, val y: Float, val time: Long)

    private val points = mutableListOf<TimedPoint>()
    private val segmentPath = Path()

    private var themeColor = Color.parseColor("#38BDF8")

    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val headGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val headCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val maxAgeMs = 230L
    private val density = resources.displayMetrics.density

    private val baseHeadWidth = 13.5f * density
    private val baseTailWidth = 2.5f * density

    private var isDissolving = false
    private var dissolveStartTime = 0L
    private val dissolveDurationMs = 130L

    init {
        isClickable = false
        isFocusable = false
        setTrailColor(themeColor)
    }

    fun setTrailColor(color: Int) {
        themeColor = color
        trailPaint.color = color
        glowPaint.color = color
        headGlowPaint.color = color
    }

    fun addPoint(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        if (isDissolving) {
            isDissolving = false
            points.clear()
        }
        points.add(TimedPoint(x, y, now))
        cleanupOldPoints(now)
        postInvalidateOnAnimation()
    }

    fun fadeAndClear() {
        if (points.isEmpty()) return
        isDissolving = true
        dissolveStartTime = System.currentTimeMillis()
        postInvalidateOnAnimation()
    }

    fun clearTrail() {
        points.clear()
        isDissolving = false
        postInvalidateOnAnimation()
    }

    private fun cleanupOldPoints(now: Long) {
        points.removeAll { now - it.time > maxAgeMs }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis()

        var dissolveFactor = 1.0f
        if (isDissolving) {
            val elapsed = now - dissolveStartTime
            if (elapsed >= dissolveDurationMs) {
                points.clear()
                isDissolving = false
                return
            }
            dissolveFactor = 1.0f - (elapsed.toFloat() / dissolveDurationMs)
        } else {
            cleanupOldPoints(now)
        }

        if (points.size < 2) {
            if (points.size == 1 && !isDissolving) {
                val pt = points[0]
                headGlowPaint.alpha = (80 * dissolveFactor).toInt()
                canvas.drawCircle(pt.x, pt.y, baseHeadWidth * 0.9f, headGlowPaint)
                headCenterPaint.alpha = (230 * dissolveFactor).toInt()
                canvas.drawCircle(pt.x, pt.y, baseHeadWidth * 0.45f, headCenterPaint)
            }
            return
        }

        val totalPoints = points.size

        // Desenha a fita com afunilamento dinâmico (tapering) e brilho difuso
        for (i in 1 until totalPoints) {
            val p0 = points[i - 1]
            val p1 = points[i]

            val ageFraction = ((now - p1.time).toFloat() / maxAgeMs).coerceIn(0f, 1f)
            val life = (1.0f - ageFraction) * dissolveFactor
            if (life <= 0.01f) continue

            val width = baseTailWidth + (baseHeadWidth - baseTailWidth) * life
            val glowWidth = width + 8f * density * life

            segmentPath.reset()
            segmentPath.moveTo(p0.x, p0.y)
            segmentPath.lineTo(p1.x, p1.y)

            // Camada de brilho difuso externo
            glowPaint.strokeWidth = glowWidth
            val baseGlowAlpha = (Color.alpha(themeColor) * 0.35f).toInt()
            glowPaint.alpha = (baseGlowAlpha * life).toInt().coerceIn(0, 255)
            canvas.drawPath(segmentPath, glowPaint)

            // Camada central sólida
            trailPaint.strokeWidth = width
            val baseCoreAlpha = Color.alpha(themeColor)
            trailPaint.alpha = (baseCoreAlpha * life).toInt().coerceIn(0, 255)
            canvas.drawPath(segmentPath, trailPaint)
        }

        // Desenha ponto de impacto luminoso na cabeça da trilha
        if (!isDissolving && points.isNotEmpty()) {
            val head = points.last()
            headGlowPaint.alpha = 95
            canvas.drawCircle(head.x, head.y, baseHeadWidth * 1.15f, headGlowPaint)
            headCenterPaint.alpha = 245
            canvas.drawCircle(head.x, head.y, baseHeadWidth * 0.5f, headCenterPaint)
        }

        if (points.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }
}
