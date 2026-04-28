package com.example.financetracker

import android.app.Application
import com.example.financetracker.data.AppDatabase
import com.example.financetracker.data.preferences.UserPreferences
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.TransactionRepository

class FinanceApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferences: UserPreferences
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var transactionRepository: TransactionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        preferences = UserPreferences(this)
        authRepository = AuthRepository(database.userDao(), preferences)
        transactionRepository = TransactionRepository(database.transactionDao())
    }
}
