package com.moneyapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC")
    fun observeByStatus(status: TxStatus): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE status = 'CONFIRMED'
          AND occurredAt >= :start AND occurredAt < :end
        ORDER BY occurredAt DESC
    """)
    fun observeRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE status = 'CONFIRMED'
          AND type = :type
          AND amount = :amount
          AND occurredAt BETWEEN :from AND :to
          AND (merchant IS :merchant OR merchant = :merchant)
    """)
    suspend fun countSimilar(
        type: TxType,
        amount: Long,
        merchant: String?,
        from: Long,
        to: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tx: Transaction): Long

    @Update
    suspend fun update(tx: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    suspend fun all(): List<Transaction>
}
