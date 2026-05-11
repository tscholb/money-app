package com.moneyapp

import android.app.Application
import com.moneyapp.data.AppDatabase
import com.moneyapp.data.TransactionRepository
import com.moneyapp.receiver.TransactionIngestor

class MoneyApplication : Application() {
    lateinit var repository: TransactionRepository
        private set
    lateinit var ingestor: TransactionIngestor
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = TransactionRepository(db.transactionDao())
        ingestor = TransactionIngestor(repository)
    }
}
