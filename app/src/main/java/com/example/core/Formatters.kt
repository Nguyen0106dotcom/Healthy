package com.example.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val vndFormatter: DecimalFormat by lazy {
    val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
        groupingSeparator = '.'
    }
    DecimalFormat("#,###", symbols)
}

fun formatVnd(amount: Int): String {
    return "${vndFormatter.format(amount.coerceAtLeast(0))}đ"
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    return sdf.format(Date(timestamp))
}
