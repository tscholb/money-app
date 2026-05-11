package com.moneyapp.parser

import com.moneyapp.data.TxType

data class ParsedTransaction(
    val type: TxType,
    val amount: Long,
    val merchant: String?,
    val occurredAt: Long,
    val issuer: String,
    val source: String,
    val rawText: String,
    val isCancel: Boolean = false
)
