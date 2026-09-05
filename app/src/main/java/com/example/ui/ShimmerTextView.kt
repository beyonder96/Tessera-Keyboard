package com.example.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class ShimmerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private var linearGradient: LinearGradient? = null
    private var gradientMatrix: Matrix? = null
    private var animator: ValueAnimator? = null
    private var translate = 0f
    private var isPaused = false

    private val resumeRunnable = Runnable {
        if (isPaused) {
            isPaused = false
            if (isShown && isAttachedToWindow && animator?.isPaused == true) {
                animator?.resume()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            val color = textColors.defaultColor
            val shimmerColor = (color and 0x00FFFFFF) or 0x44000000 // Alpha sutil
            val brightColor = (color and 0x00FFFFFF) or -0x1000000 // Full alpha
            
            linearGradient = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                intArrayOf(color, brightColor, color),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = linearGradient
            gradientMatrix = Matrix()

            if (isShown) {
                startAnimation(w)
            }
        }
    }

    fun temporarilyPause(durationMs: Long = 1000L) {
        removeCallbacks(resumeRunnable)
        if (!isPaused) {
            isPaused = true
            animator?.pause()
        }
        postDelayed(resumeRunnable, durationMs)
    }

    private fun startAnimation(width: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 2f).apply {
            duration = 3500
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                if (!isPaused && isShown && isAttachedToWindow) {
                    val value = animation.animatedValue as Float
                    translate = width * (value - 1f)
                    gradientMatrix?.setTranslate(translate, 0f)
                    linearGradient?.setLocalMatrix(gradientMatrix)
                    invalidate()
                }
            }
            start()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE && width > 0) {
            startAnimation(width)
        } else {
            animator?.cancel()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE && width > 0) {
            startAnimation(width)
        } else {
            animator?.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(resumeRunnable)
        animator?.cancel()
        animator = null
    }
}
