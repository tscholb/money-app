package com.moneyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.Transaction
import com.moneyapp.data.TransactionRepository
import com.moneyapp.data.TxStatus
import com.moneyapp.data.TxType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthSummary(
    val income: Long = 0,
    val expense: Long = 0
) {
    val balance: Long get() = income - expense
}

data class MonthCursor(val year: Int, val monthZeroBased: Int) {
    fun shift(delta: Int): MonthCursor {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, monthZeroBased, 1)
            add(Calendar.MONTH, delta)
        }
        return MonthCursor(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun labelMillis(): Long {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, monthZeroBased, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        fun current(): MonthCursor {
            val cal = Calendar.getInstance()
            return MonthCursor(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repo: TransactionRepository) : ViewModel() {

    private val _cursor = MutableStateFlow(MonthCursor.current())
    val cursor: StateFlow<MonthCursor> = _cursor.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = _cursor
        .flatMapLatest { c -> repo.observeMonth(c.year, c.monthZeroBased) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<MonthSummary> = transactions
        .map { list ->
            val income = list.filter { it.type == TxType.INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
            MonthSummary(income, expense)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary())

    val pending: StateFlow<List<Transaction>> = repo.observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun prevMonth() { _cursor.value = _cursor.value.shift(-1) }
    fun nextMonth() { _cursor.value = _cursor.value.shift(1) }

    fun addManual(tx: Transaction) {
        viewModelScope.launch { repo.add(tx.copy(status = TxStatus.CONFIRMED, source = "MANUAL")) }
    }

    fun approve(tx: Transaction) {
        viewModelScope.launch { repo.update(tx.copy(status = TxStatus.CONFIRMED)) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    class Factory(private val repo: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repo) as T
    }
}
