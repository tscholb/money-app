package com.moneyapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxType { INCOME, EXPENSE }

enum class TxStatus {
    CONFIRMED,
    PENDING,
    CANCELED
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxType,
    val amount: Long,
    val category: String,
    val merchant: String? = null,
    val memo: String? = null,
    val occurredAt: Long,
    val source: String = "MANUAL",
    val status: TxStatus = TxStatus.CONFIRMED,
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
