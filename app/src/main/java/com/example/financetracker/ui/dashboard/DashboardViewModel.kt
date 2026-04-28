package com.example.financetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Category
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val balance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val recent: List<TransactionEntity> = emptyList(),
    val categoryBreakdown: List<Pair<Category, Double>> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: TransactionRepository,
    preferences: UserPreferences
) : ViewModel() {

    val state: StateFlow<DashboardUiState> = preferences.currentUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else repository.observeAll(userId)
        }
        .map { all -> buildState(all) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun buildState(all: List<TransactionEntity>): DashboardUiState {
        val income = all.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = all.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val (from, to) = monthRange()
        val monthTx = all.filter { it.date in from..to }
        val mIncome = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val mExpense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val breakdown = monthTx
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (cat, list) -> cat to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
        return DashboardUiState(
            balance = income - expense,
            monthIncome = mIncome,
            monthExpense = mExpense,
            recent = all.take(5),
            categoryBreakdown = breakdown
        )
    }

    private fun monthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val from = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return from to (cal.timeInMillis - 1)
    }

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch { repository.delete(tx) }
    }
}
