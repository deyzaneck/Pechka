package com.example.pechka.theme

import kotlin.math.abs

/**
 * Formats an amount as "1 500 ₽" using a thin non-breaking space as the
 * thousands separator. Russian convention; far easier to scan on a 5-inch
 * screen while driving than "1500₽".
 */
fun Int.asRubles(): String = "${this.withThinSpaces()} ₽"

fun Double.asRubles(): String = this.toInt().asRubles()

fun Int.withThinSpaces(): String {
    val negative = this < 0
    val digits = abs(this).toString()
    val sb = StringBuilder()
    val n = digits.length
    for (i in 0 until n) {
        if (i > 0 && (n - i) % 3 == 0) sb.append(' ')
        sb.append(digits[i])
    }
    return if (negative) "−$sb" else sb.toString()
}
