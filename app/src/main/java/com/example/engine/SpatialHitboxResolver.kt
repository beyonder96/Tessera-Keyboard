package com.example.engine

import com.example.ui.keyboard.KeyAction
import com.example.ui.keyboard.KeyCell
import kotlin.math.hypot

object SpatialHitboxResolver {

    fun resolveKey(
        x: Float,
        y: Float,
        keys: List<KeyCell>,
        validNextCharsMask: Int = 0,
        isAdaptiveEnabled: Boolean = true
    ): KeyCell? {
        if (keys.isEmpty()) return null

        // 1. Direct hit on 70% inner core of any key (no ambiguity)
        for (key in keys) {
            val rect = key.visualRect
            if (rect.contains(x, y)) {
                val coreMarginX = rect.width() * 0.15f
                val coreMarginY = rect.height() * 0.15f
                if (x >= rect.left + coreMarginX && x <= rect.right - coreMarginX &&
                    y >= rect.top + coreMarginY && y <= rect.bottom - coreMarginY
                ) {
                    return key
                }
            }
        }

        // 2. Boundary or Margin zone: resolve with Voronoi + Trie Bitmask Bias
        var bestKey: KeyCell? = null
        var minEffectiveDistSq = Float.MAX_VALUE

        for (key in keys) {
            val cx = key.centerX
            val cy = key.centerY
            val dx = x - cx
            val dy = y - cy
            val rawDistSq = dx * dx + dy * dy

            // Check if key is a letter that continues a valid word prefix
            var weightFactor = 1.0f
            if (isAdaptiveEnabled && validNextCharsMask != 0 && key.action is KeyAction.CharKey) {
                val char = key.action.char.lowercase().firstOrNull()
                if (char != null && char in 'a'..'z') {
                    val bit = 1 shl (char - 'a')
                    if ((validNextCharsMask and bit) != 0) {
                        // 16% distance reduction in Euclidean space = (0.84)^2 in squared space ~ 0.7056
                        weightFactor = 0.7056f
                    }
                }
            }

            // Clamping penalty: prevent vertical row jumps across far distances
            val rowHeight = key.height.coerceAtLeast(1f)
            val verticalDistRatio = (Math.abs(dy) / rowHeight)
            val verticalPenalty = if (verticalDistRatio > 0.85f) 1.8f else 1.0f

            val effectiveDistSq = rawDistSq * weightFactor * verticalPenalty
            if (effectiveDistSq < minEffectiveDistSq) {
                minEffectiveDistSq = effectiveDistSq
                bestKey = key
            }
        }

        return bestKey
    }
}
