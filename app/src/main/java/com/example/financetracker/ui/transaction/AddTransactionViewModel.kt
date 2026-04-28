package com.example.financetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Category
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val id: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val category: Category = Category.FOOD,
    val date: Long = System.currentTimeMillis(),
    val comment: String = "",
    val error: String? = null,
    val saved: Boolean = false,
    val isLoading: Boolean = false,
    val isEdit: Boolean = false
)

class AddTransactionViewModel(
    private val repository: TransactionRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionUiState())
    val state: StateFlow<AddTransactionUiState> = _state.asStateFlow()

    fun loadForEdit(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val tx = repository.findById(id) ?: return@launch
            _state.value = AddTransactionUiState(
                id = tx.id,
                type = tx.type,
                amount = tx.amount.toCleanString(),
                category = tx.category,
                date = tx.date,
                comment = tx.comment,
                isEdit = true
            )
        }
    }

    fun setType(type: TransactionType) {
        _state.update {
            val first = Category.forType(type).first()
            it.copy(type = type, category = if (it.category.type == type) it.category else first)
        }
    }

    fun setAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        if (filtered.count { it == '.' } > 1) return
        _state.update { it.copy(amount = filtered, error = null) }
    }

    fun setCategory(cat: Category) = _state.update { it.copy(category = cat) }
    fun setDate(date: Long) = _state.update { it.copy(date = date) }
    fun setComment(value: String) = _state.update { it.copy(comment = value) }

    fun reset() {
        _state.value = AddTransactionUiState()
    }

    fun save() {
        val current = _state.value
        if (current.isLoading) return
        val amount = current.amount.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(error = "Введите корректную сумму больше 0") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val userId = preferences.currentUserId.first() ?: run {
                _state.update { it.copy(isLoading = false, error = "Пользователь не авторизован") }
                return@launch
            }
            val entity = TransactionEntity(
                id = current.id,
                userId = userId,
                amount = amount,
                type = current.type,
                category = current.category,
                date = current.date,
                comment = current.comment.trim()
            )
            if (current.isEdit) repository.update(entity) else repository.add(entity)
            _state.update { it.copy(isLoading = false, saved = true) }
        }
    }
}

private fun Double.toCleanString(): String {
    val rounded = (this * 100).toLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
    else rounded.toString()
}
