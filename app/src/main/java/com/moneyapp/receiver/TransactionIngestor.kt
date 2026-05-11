package com.moneyapp.receiver

import com.moneyapp.data.Transaction
import com.moneyapp.data.TransactionRepository
import com.moneyapp.data.TxStatus
import com.moneyapp.parser.CategoryGuesser
import com.moneyapp.parser.ParsedTransaction

class TransactionIngestor(private val repo: TransactionRepository) {

    suspend fun ingest(parsed: ParsedTransaction): Long? {
        val tx = Transaction(
            type = parsed.type,
            amount = parsed.amount,
            category = CategoryGuesser.guess(parsed.merchant),
            merchant = parsed.merchant,
            memo = parsed.issuer,
            occurredAt = parsed.occurredAt,
            source = parsed.source,
            status = TxStatus.PENDING,
            rawText = parsed.rawText
        )
        if (repo.isDuplicate(tx)) return null
        return repo.add(tx)
    }
}
