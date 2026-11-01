package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

data class CalcResult(val expression: String, val formatted: String)

object InlineCalculatorEngine {

    fun tryEvaluate(input: String): CalcResult? {
        val trimmed = input.trimEnd('=').trim()
        val expr = trimmed.takeLastWhile { it != '\n' && it != '?' && it != '!' && it != ':' && it != ';' }.trim()
        val res = evaluate(expr) ?: return null
        return CalcResult(expr, res)
    }

    fun evaluate(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.length < 3) return null

        // Detect if expression ends with '=' or has explicit calculation structure
        val hasEquals = trimmed.endsWith("=")
        val rawExpr = if (hasEquals) trimmed.dropLast(1).trim() else trimmed

        if (rawExpr.isEmpty()) return null

        // Must contain at least one valid arithmetic operator
        val operators = setOf('+', '-', '*', '×', '/', '÷', '%')
        var hasOp = false
        var hasDigit = false
        for (c in rawExpr) {
            if (c in operators) hasOp = true
            if (c.isDigit()) hasDigit = true
        }
        if (!hasOp || !hasDigit) return null

        // Normalize operators and decimals
        val normalized = StringBuilder()
        var lastChar = ' '
        for (i in rawExpr.indices) {
            val c = rawExpr[i]
            when (c) {
                '×' -> normalized.append('*')
                '÷' -> normalized.append('/')
                ',' -> {
                    // Check if comma is decimal separator between digits (e.g., "1,5")
                    val prevIsDigit = i > 0 && rawExpr[i - 1].isDigit()
                    val nextIsDigit = i < rawExpr.length - 1 && rawExpr[i + 1].isDigit()
                    if (prevIsDigit && nextIsDigit) {
                        normalized.append('.')
                    } else {
                        return null // Invalid comma usage
                    }
                }
                ' ' -> {} // skip spaces
                else -> {
                    if (c.isDigit() || c == '.' || c in operators || c == '(' || c == ')') {
                        normalized.append(c)
                    } else {
                        return null // Invalid character
                    }
                }
            }
        }

        val exprStr = normalized.toString()
        if (exprStr.isEmpty()) return null

        return try {
            val result = Parser(exprStr).parse()
            if (result.isNaN() || result.isInfinite()) return null

            // Format in PT-BR style
            val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
            val df = if (abs(result - result.toLong()) < 1e-9) {
                DecimalFormat("#,##0", symbols)
            } else {
                DecimalFormat("#,##0.####", symbols)
            }
            df.format(result)
        } catch (_: Throwable) {
            null
        }
    }

    private class Parser(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected: $ch")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (abs(divisor) < 1e-12) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw IllegalArgumentException("Unexpected char: $ch")
            }

            while (eat('%')) {
                x /= 100.0
            }
            return x
        }
    }
}
