package com.example.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            val color = textColors.defaultColor
            val shimmerColor = (color and 0x00FFFFFF.toInt()) or 0x66000000.toInt() // Alpha adjusted
            val brightColor = (color and 0x00FFFFFF.toInt()) or 0xFF000000.toInt() // Full alpha
            
            linearGradient = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                intArrayOf(color, brightColor, color),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = linearGradient
            gradientMatrix = Matrix()

            startAnimation(w)
        }
    }

    private fun startAnimation(width: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 2f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                translate = width * (value - 1f)
                gradientMatrix?.setTranslate(translate, 0f)
                linearGradient?.setLocalMatrix(gradientMatrix)
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
