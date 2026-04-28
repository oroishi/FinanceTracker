package com.example.financetracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val success: Boolean = false,
    val verificationSent: Boolean = false,
    val needsVerification: Boolean = false,
    val passwordResetSent: Boolean = false
)

enum class StartDestination { Splash, Auth, Home }

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _startDestination = MutableStateFlow(StartDestination.Splash)
    val startDestination: StateFlow<StartDestination> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            val firebaseUser = authRepository.currentFirebaseUser()
            _startDestination.value = when {
                userId != null && firebaseUser != null -> StartDestination.Home
                else -> {
                    if (userId != null) preferences.clearSession()
                    StartDestination.Auth
                }
            }
        }
    }

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, error = null, info = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, error = null, info = null) }

    fun onConfirmChange(value: String) =
        _state.update { it.copy(confirmPassword = value, error = null, info = null) }

    fun reset() {
        _state.value = AuthUiState()
    }

    fun consumeSuccess() = _state.update { it.copy(success = false) }
    fun consumeVerificationSent() = _state.update { it.copy(verificationSent = false) }
    fun consumeNeedsVerification() = _state.update { it.copy(needsVerification = false) }
    fun consumePasswordResetSent() = _state.update { it.copy(passwordResetSent = false) }

    fun login() {
        if (_state.value.isLoading) return
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            handleResult(authRepository.login(current.email, current.password))
        }
    }

    fun register() {
        if (_state.value.isLoading) return
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            handleResult(authRepository.register(current.email, current.password, current.confirmPassword))
        }
    }

    fun resendVerification() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            handleResult(authRepository.resendVerification())
        }
    }

    fun checkVerified() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            handleResult(authRepository.checkVerified())
        }
    }

    fun sendPasswordReset() {
        if (_state.value.isLoading) return
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            handleResult(authRepository.sendPasswordReset(current.email))
        }
    }

    private fun handleResult(result: AuthResult) {
        _state.update { state ->
            when (result) {
                is AuthResult.Success -> state.copy(isLoading = false, success = true, error = null)
                is AuthResult.Failure -> state.copy(isLoading = false, error = result.message)
                AuthResult.VerificationSent -> state.copy(
                    isLoading = false,
                    verificationSent = true,
                    info = "Письмо со ссылкой подтверждения отправлено"
                )
                AuthResult.NeedsVerification -> state.copy(
                    isLoading = false,
                    needsVerification = true,
                    error = "Email не подтверждён. Проверьте почту или запросите письмо повторно."
                )
                AuthResult.PasswordResetSent -> state.copy(
                    isLoading = false,
                    passwordResetSent = true,
                    info = "Письмо для сброса пароля отправлено"
                )
            }
        }
    }
}
