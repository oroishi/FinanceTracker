package com.example.financetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Category
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.ui.transaction.PeriodFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class TimePoint(val label: String, val income: Double, val expense: Double)

data class StatsUiState(
    val period: PeriodFilter = PeriodFilter.MONTH,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val expenseByCategory: List<Pair<Category, Double>> = emptyList(),
    val timeline: List<TimePoint> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    repository: TransactionRepository,
    preferences: UserPreferences
) : ViewModel() {

    private val period = MutableStateFlow(PeriodFilter.MONTH)

    private val transactions = preferences.currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeAll(id)
    }

    val state: StateFlow<StatsUiState> = combine(transactions, period) { tx, p ->
        val (from, to) = periodRange(p)
        val periodTx = if (from == null) tx else tx.filter { it.date in from..to!! }
        val income = periodTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = periodTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val byCat = periodTx.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (c, l) -> c to l.sumOf { it.amount } }
            .sortedByDescending { it.second }
        val timeline = buildTimeline(periodTx, p)
        StatsUiState(
            period = p,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            expenseByCategory = byCat,
            timeline = timeline
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun setPeriod(p: PeriodFilter) = period.update { p }

    private fun periodRange(period: PeriodFilter): Pair<Long?, Long?> {
        if (period == PeriodFilter.ALL) return null to null
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val to = System.currentTimeMillis()
        when (period) {
            PeriodFilter.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -7)
            PeriodFilter.MONTH -> cal.add(Calendar.MONTH, -1)
            PeriodFilter.YEAR -> cal.add(Calendar.YEAR, -1)
            else -> Unit
        }
        return cal.timeInMillis to to
    }

    private fun buildTimeline(list: List<TransactionEntity>, period: PeriodFilter): List<TimePoint> {
        if (list.isEmpty()) return emptyList()
        val months = listOf("Янв","Фев","Мар","Апр","Май","Июн","Июл","Авг","Сен","Окт","Ноя","Дек")
        val dayLabel: (Calendar) -> String = { c ->
            "${c.get(Calendar.DAY_OF_MONTH)}.${c.get(Calendar.MONTH) + 1}"
        }
        val monthLabel: (Calendar) -> String = { c -> months[c.get(Calendar.MONTH)] }
        val (bucketCount, field, format) = when (period) {
            PeriodFilter.WEEK -> Triple(7, Calendar.DAY_OF_YEAR, dayLabel)
            PeriodFilter.MONTH -> Triple(30, Calendar.DAY_OF_YEAR, dayLabel)
            PeriodFilter.YEAR -> Triple(12, Calendar.MONTH, monthLabel)
            PeriodFilter.ALL -> Triple(12, Calendar.MONTH, monthLabel)
        }
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val points = mutableListOf<TimePoint>()
        for (i in (bucketCount - 1) downTo 0) {
            cal.timeInMillis = now
            cal.add(field, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val from = cal.timeInMillis
            cal.add(field, 1)
            val to = cal.timeInMillis - 1
            val bucket = list.filter { it.date in from..to }
            val inc = bucket.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = bucket.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            cal.timeInMillis = from
            points += TimePoint(format(cal), inc, exp)
        }
        return points
    }
}
