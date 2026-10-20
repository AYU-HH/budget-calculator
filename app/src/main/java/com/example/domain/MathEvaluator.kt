package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object MathEvaluator {

    var lastAnswer: Double = 0.0

    /**
     * Safely evaluates mathematical expressions supporting:
     * - Parentheses: ( )
     * - Operators: +, -, *, ×, /, ÷
     * - Decimals: 3.14
     * - Percentages: 15% (treated as 0.15 or (number / 100)) e.g. "15%*2000" = 300
     * - Chained answer token: "ans" substituted with [ansValue]
     */
    fun evaluate(expression: String, ansValue: Double = lastAnswer): Result<Double> {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("ans", ansValue.toString(), ignoreCase = true)
            .replace(" ", "")

        if (sanitized.isEmpty()) {
            return Result.failure(IllegalArgumentException("Expression is empty"))
        }

        return try {
            val parser = Parser(sanitized)
            val result = parser.parse()
            if (!result.isFinite() || result.isNaN()) {
                Result.failure(ArithmeticException("Calculation error (division by zero or overflow)"))
            } else {
                lastAnswer = result
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0 && kotlin.math.abs(value) < 1e12) {
            String.format(Locale.US, "%.0f", value)
        } else {
            val bd = BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
            bd.stripTrailingZeros().toPlainString()
        }
    }

    private class Parser(private val src: String) {
        private var pos = 0

        private fun peek(): Char = if (pos < src.length) src[pos] else '\u0000'
        private fun consume(): Char = if (pos < src.length) src[pos++] else '\u0000'

        fun parse(): Double {
            val res = parseExpression()
            if (pos < src.length) {
                throw IllegalArgumentException("Unexpected character '${src[pos]}' at position $pos")
            }
            return res
        }

        // expression = term (('+' | '-') term)*
        private fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                val c = peek()
                if (c == '+' || c == '-') {
                    consume()
                    val right = parseTerm()
                    left = if (c == '+') left + right else left - right
                } else {
                    break
                }
            }
            return left
        }

        // term = factor (('*' | '/') factor)*
        private fun parseTerm(): Double {
            var left = parseFactor()
            while (true) {
                val c = peek()
                if (c == '*' || c == '/') {
                    consume()
                    val right = parseFactor()
                    if (c == '/') {
                        if (right == 0.0) throw ArithmeticException("Division by zero")
                        left /= right
                    } else {
                        left *= right
                    }
                } else {
                    break
                }
            }
            return left
        }

        // factor = ('+' | '-')? primary ('%')*
        private fun parseFactor(): Double {
            val c = peek()
            var sign = 1.0
            if (c == '+' || c == '-') {
                consume()
                if (c == '-') sign = -1.0
            }

            var value = parsePrimary() * sign

            // Check for percentage suffix: e.g. 15% -> 0.15
            while (peek() == '%') {
                consume()
                value /= 100.0
            }

            return value
        }

        // primary = number | '(' expression ')'
        private fun parsePrimary(): Double {
            val c = peek()
            if (c == '(') {
                consume() // eat '('
                val res = parseExpression()
                if (peek() != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                consume() // eat ')'
                return res
            }

            if (c.isDigit() || c == '.') {
                val start = pos
                var hasDot = false
                while (peek().isDigit() || (!hasDot && peek() == '.')) {
                    if (peek() == '.') hasDot = true
                    consume()
                }
                val numStr = src.substring(start, pos)
                return numStr.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Invalid number format: $numStr")
            }

            throw IllegalArgumentException("Unexpected token at position $pos: '$c'")
        }
    }
}
