package com.example.financetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Category
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.TransactionRepository
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
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PeriodFilter(val label: String) {
    ALL("Все"),
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год")
}

data class TransactionListFilters(
    val period: PeriodFilter = PeriodFilter.ALL,
    val type: TransactionType? = null,
    val categories: Set<Category> = emptySet(),
    val query: String = ""
)

data class TransactionListUiState(
    val items: List<TransactionEntity> = emptyList(),
    val filters: TransactionListFilters = TransactionListFilters()
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModel(
    private val repository: TransactionRepository,
    preferences: UserPreferences
) : ViewModel() {

    private val filters = MutableStateFlow(TransactionListFilters())
    val filtersFlow: StateFlow<TransactionListFilters> = filters.asStateFlow()

    private val transactions = preferences.currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeAll(id)
    }

    val state: StateFlow<TransactionListUiState> =
        combine(transactions, filters) { tx, f ->
            val (from, to) = periodRange(f.period)
            val filtered = tx.filter { item ->
                (from == null || item.date in from..to!!) &&
                    (f.type == null || item.type == f.type) &&
                    (f.categories.isEmpty() || item.category in f.categories) &&
                    (f.query.isBlank() || item.comment.contains(f.query, ignoreCase = true))
            }
            TransactionListUiState(items = filtered, filters = f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionListUiState())

    fun setPeriod(period: PeriodFilter) = filters.update { it.copy(period = period) }
    fun setType(type: TransactionType?) = filters.update { it.copy(type = type, categories = emptySet()) }
    fun toggleCategory(category: Category) = filters.update {
        val next = if (category in it.categories) it.categories - category else it.categories + category
        it.copy(categories = next)
    }
    fun clearCategories() = filters.update { it.copy(categories = emptySet()) }
    fun setQuery(query: String) = filters.update { it.copy(query = query) }

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch { repository.delete(tx) }
    }

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
}
