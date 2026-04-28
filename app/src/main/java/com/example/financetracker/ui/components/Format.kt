package com.example.financetracker.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val moneyLocale: Locale = Locale.forLanguageTag("ru-RU")
private val dateFormatter = SimpleDateFormat("dd MMM yyyy", moneyLocale)
private val shortDateFormatter = SimpleDateFormat("dd.MM.yy", moneyLocale)

fun formatMoney(value: Double): String {
    val abs = Math.abs(value)
    val formatted = String.format(moneyLocale, "%,.2f", abs)
    return if (value < 0) "-$formatted ₽" else "$formatted ₽"
}

fun formatMoneyShort(value: Double): String {
    val abs = Math.abs(value)
    return when {
        abs >= 1_000_000 -> String.format(moneyLocale, "%.1fM ₽", value / 1_000_000)
        abs >= 1_000 -> String.format(moneyLocale, "%.1fK ₽", value / 1_000)
        else -> String.format(moneyLocale, "%.0f ₽", value)
    }
}

fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))
fun formatShortDate(timestamp: Long): String = shortDateFormatter.format(Date(timestamp))
