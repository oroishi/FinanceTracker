package com.example.financetracker.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFY_EMAIL = "verify_email"
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction/{id}"
    fun editTransaction(id: Long) = "edit_transaction/$id"
}
