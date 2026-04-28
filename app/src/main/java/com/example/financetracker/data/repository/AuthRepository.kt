package com.example.financetracker.data.repository

import com.example.financetracker.data.dao.UserDao
import com.example.financetracker.data.entity.User
import com.example.financetracker.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val userId: Long) : AuthResult()
    data object VerificationSent : AuthResult()
    data object NeedsVerification : AuthResult()
    data object PasswordResetSent : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val preferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun register(email: String, password: String, confirm: String): AuthResult {
        val normalized = email.trim().lowercase()
        if (!isEmailValid(normalized)) return AuthResult.Failure("Некорректный email")
        if (password.length < 6) return AuthResult.Failure("Пароль должен быть не менее 6 символов")
        if (password != confirm) return AuthResult.Failure("Пароли не совпадают")
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(normalized, password).await()
            val user = result.user ?: return AuthResult.Failure("Не удалось создать аккаунт")
            user.sendEmailVerification().await()
            AuthResult.VerificationSent
        } catch (_: FirebaseAuthUserCollisionException) {
            AuthResult.Failure("Пользователь с таким email уже существует")
        } catch (_: FirebaseAuthWeakPasswordException) {
            AuthResult.Failure("Пароль слишком слабый")
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure("Некорректный email")
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Ошибка регистрации")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalized = email.trim().lowercase()
        if (!isEmailValid(normalized)) return AuthResult.Failure("Некорректный email")
        if (password.isBlank()) return AuthResult.Failure("Введите пароль")
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(normalized, password).await()
            val firebaseUser = result.user ?: return AuthResult.Failure("Не удалось войти")
            firebaseUser.reload().await()
            val refreshed = firebaseAuth.currentUser ?: firebaseUser
            if (!refreshed.isEmailVerified) {
                return AuthResult.NeedsVerification
            }
            val localId = ensureLocalUser(refreshed.uid, refreshed.email ?: normalized)
            preferences.setUserId(localId)
            AuthResult.Success(localId)
        } catch (_: FirebaseAuthInvalidUserException) {
            AuthResult.Failure("Пользователь не найден")
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure("Неверный email или пароль")
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Ошибка входа")
        }
    }

    suspend fun resendVerification(): AuthResult {
        val user = firebaseAuth.currentUser
            ?: return AuthResult.Failure("Сессия истекла, войдите ещё раз")
        return try {
            user.sendEmailVerification().await()
            AuthResult.VerificationSent
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Не удалось отправить письмо")
        }
    }

    suspend fun checkVerified(): AuthResult {
        val user = firebaseAuth.currentUser
            ?: return AuthResult.Failure("Сессия истекла, войдите ещё раз")
        return try {
            user.reload().await()
            val refreshed = firebaseAuth.currentUser ?: user
            if (!refreshed.isEmailVerified) {
                AuthResult.NeedsVerification
            } else {
                val localId = ensureLocalUser(refreshed.uid, refreshed.email ?: "")
                preferences.setUserId(localId)
                AuthResult.Success(localId)
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Ошибка проверки")
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        val normalized = email.trim().lowercase()
        if (!isEmailValid(normalized)) return AuthResult.Failure("Некорректный email")
        return try {
            firebaseAuth.sendPasswordResetEmail(normalized).await()
            AuthResult.PasswordResetSent
        } catch (_: FirebaseAuthInvalidUserException) {
            AuthResult.Failure("Пользователь с таким email не найден")
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Не удалось отправить письмо")
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        preferences.clearSession()
    }

    suspend fun userById(id: Long): User? = userDao.findById(id)

    private suspend fun ensureLocalUser(uid: String, email: String): Long {
        userDao.findByFirebaseUid(uid)?.let { return it.id }
        return userDao.insert(User(firebaseUid = uid, email = email))
    }

    private fun isEmailValid(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return regex.matches(email)
    }
}
