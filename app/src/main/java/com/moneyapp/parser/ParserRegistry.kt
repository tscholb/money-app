package com.moneyapp.parser

object ParserRegistry {
    val parsers: List<TransactionParser> = listOf(
        SamsungCardParser(),
        KbCardParser(),
        HanaBankParser()
    )

    fun parse(text: String, receivedAt: Long, source: String): ParsedTransaction? {
        if (text.isBlank()) return null
        return parsers.firstOrNull { it.canParse(text) }
            ?.parse(text, receivedAt, source)
    }
}
