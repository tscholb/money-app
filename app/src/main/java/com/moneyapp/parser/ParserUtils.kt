package com.moneyapp.parser

import java.util.Calendar

internal object ParserUtils {

    private val amountRegex = Regex("""([0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)\s*원""")
    private val dateTimeRegex = Regex(
        """(?:(\d{2,4})[/.\-])?(\d{1,2})[/.\-](\d{1,2})\s+(\d{1,2}):(\d{2})"""
    )

    fun extractAmount(text: String): Long? {
        val match = amountRegex.find(text) ?: return null
        return match.groupValues[1].replace(",", "").toLongOrNull()
    }

    fun extractOccurredAt(text: String, fallback: Long): Long {
        val m = dateTimeRegex.find(text) ?: return fallback
        val cal = Calendar.getInstance()
        val year = m.groupValues[1].takeIf { it.isNotEmpty() }?.let {
            if (it.length == 2) 2000 + it.toInt() else it.toInt()
        } ?: cal.get(Calendar.YEAR)
        val month = m.groupValues[2].toInt() - 1
        val day = m.groupValues[3].toInt()
        val hour = m.groupValues[4].toInt()
        val minute = m.groupValues[5].toInt()
        cal.set(year, month, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
