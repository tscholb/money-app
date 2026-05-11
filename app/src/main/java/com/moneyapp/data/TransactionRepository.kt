package com.moneyapp.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TransactionRepository(private val dao: TransactionDao) {

    fun observePending(): Flow<List<Transaction>> =
        dao.observeByStatus(TxStatus.PENDING)

    fun observeMonth(year: Int, monthZeroBased: Int): Flow<List<Transaction>> {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, monthZeroBased, 1, 0, 0, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return dao.observeRange(start, end)
    }

    suspend fun add(tx: Transaction): Long = dao.insert(tx)

    suspend fun update(tx: Transaction) = dao.update(tx)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun isDuplicate(tx: Transaction): Boolean {
        val window = TimeUnit.MINUTES.toMillis(2)
        val from = tx.occurredAt - window
        val to = tx.occurredAt + window
        return dao.countSimilar(tx.type, tx.amount, tx.merchant, from, to) > 0
    }

    suspend fun allForExport(): List<Transaction> = dao.all()
}
