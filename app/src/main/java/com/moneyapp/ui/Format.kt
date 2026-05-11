package com.moneyapp.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object Format {
    private val won = NumberFormat.getNumberInstance(Locale.KOREA)
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
    private val monthFormat = SimpleDateFormat("yyyy년 M월", Locale.KOREA)

    fun money(value: Long): String = "${won.format(value)}원"

    fun signedMoney(value: Long, isExpense: Boolean): String {
        val sign = if (isExpense) "-" else "+"
        return "$sign${won.format(value)}원"
    }

    fun dateTime(ms: Long): String = dateFormat.format(ms)
    fun month(ms: Long): String = monthFormat.format(ms)
}
