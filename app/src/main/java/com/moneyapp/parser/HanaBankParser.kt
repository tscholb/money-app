package com.moneyapp.parser

import com.moneyapp.data.TxType

class HanaBankParser : TransactionParser {
    override val issuer = "하나은행"

    private val signatureRegex = Regex("""(하나(?:은행)?)""")
    private val depositRegex = Regex("""(입금|받음)""")
    private val withdrawRegex = Regex("""(출금|보냄|이체)""")
    private val counterpartyRegex = Regex("""(?:입금|출금|이체)\s*[:\-]?\s*([가-힣A-Za-z0-9 ()*]+?)(?:\n|$|\s{2,})""")

    override fun canParse(text: String): Boolean =
        signatureRegex.containsMatchIn(text) &&
            (depositRegex.containsMatchIn(text) || withdrawRegex.containsMatchIn(text))

    override fun parse(text: String, receivedAt: Long, source: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(text) ?: return null
        val occurredAt = ParserUtils.extractOccurredAt(text, receivedAt)
        val isDeposit = depositRegex.containsMatchIn(text)
        val counterparty = counterpartyRegex.find(text)?.groupValues?.get(1)?.trim()
        return ParsedTransaction(
            type = if (isDeposit) TxType.INCOME else TxType.EXPENSE,
            amount = amount,
            merchant = counterparty,
            occurredAt = occurredAt,
            issuer = issuer,
            source = source,
            rawText = text
        )
    }
}
