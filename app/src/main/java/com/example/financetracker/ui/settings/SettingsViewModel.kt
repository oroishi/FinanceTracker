package com.example.financetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.entity.User
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val email: String = "",
    val darkTheme: Boolean? = null,
    val loggedOut: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val email: StateFlow<String> = preferences.currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf("") else flow {
                emit(authRepository.userById(id)?.email ?: "")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val darkTheme: StateFlow<Boolean?> = preferences.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkTheme(enabled) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
