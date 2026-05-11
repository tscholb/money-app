package com.moneyapp.parser

import com.moneyapp.data.TxType

class KbCardParser : TransactionParser {
    override val issuer = "KB국민카드"

    private val signatureRegex = Regex("""(KB(국민)?[ ]?카드|국민카드)""")
    private val cancelRegex = Regex("""(취소|승인취소)""")
    private val merchantLine = Regex("""\d{2}:\d{2}\s+(.+?)(?:\n|$)""")
    private val merchantFallback = Regex("""(?:일시불|할부)\s*(.+?)(?:\n|$)""")

    override fun canParse(text: String): Boolean = signatureRegex.containsMatchIn(text)

    override fun parse(text: String, receivedAt: Long, source: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(text) ?: return null
        val occurredAt = ParserUtils.extractOccurredAt(text, receivedAt)
        val merchant = merchantLine.find(text)?.groupValues?.get(1)?.trim()
            ?: merchantFallback.find(text)?.groupValues?.get(1)?.trim()
        val cancel = cancelRegex.containsMatchIn(text)
        return ParsedTransaction(
            type = if (cancel) TxType.INCOME else TxType.EXPENSE,
            amount = amount,
            merchant = merchant,
            occurredAt = occurredAt,
            issuer = issuer,
            source = source,
            rawText = text,
            isCancel = cancel
        )
    }
}
