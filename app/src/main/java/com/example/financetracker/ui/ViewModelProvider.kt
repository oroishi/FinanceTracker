package com.example.financetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.example.financetracker.FinanceApp
import com.example.financetracker.ui.auth.AuthViewModel
import com.example.financetracker.ui.dashboard.DashboardViewModel
import com.example.financetracker.ui.settings.SettingsViewModel
import com.example.financetracker.ui.stats.StatsViewModel
import com.example.financetracker.ui.transaction.AddTransactionViewModel
import com.example.financetracker.ui.transaction.TransactionListViewModel

object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = financeApp()
            AuthViewModel(app.authRepository, app.preferences)
        }
        initializer {
            val app = financeApp()
            DashboardViewModel(app.transactionRepository, app.preferences)
        }
        initializer {
            val app = financeApp()
            AddTransactionViewModel(app.transactionRepository, app.preferences)
        }
        initializer {
            val app = financeApp()
            TransactionListViewModel(app.transactionRepository, app.preferences)
        }
        initializer {
            val app = financeApp()
            StatsViewModel(app.transactionRepository, app.preferences)
        }
        initializer {
            val app = financeApp()
            SettingsViewModel(app.authRepository, app.preferences)
        }
    }

    private fun CreationExtras.financeApp(): FinanceApp =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinanceApp)
}
