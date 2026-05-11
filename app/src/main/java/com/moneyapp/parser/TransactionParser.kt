package com.moneyapp.parser

interface TransactionParser {
    val issuer: String
    fun canParse(text: String): Boolean
    fun parse(text: String, receivedAt: Long, source: String): ParsedTransaction?
}
