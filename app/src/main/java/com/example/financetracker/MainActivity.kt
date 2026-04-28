package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financetracker.navigation.Routes
import com.example.financetracker.ui.AppViewModelFactory
import com.example.financetracker.ui.auth.AuthViewModel
import com.example.financetracker.ui.auth.ForgotPasswordScreen
import com.example.financetracker.ui.auth.LoginScreen
import com.example.financetracker.ui.auth.RegisterScreen
import com.example.financetracker.ui.auth.StartDestination
import com.example.financetracker.ui.auth.VerifyEmailScreen
import com.example.financetracker.ui.dashboard.DashboardScreen
import com.example.financetracker.ui.dashboard.DashboardViewModel
import com.example.financetracker.ui.dashboard.HomeTab
import com.example.financetracker.ui.settings.SettingsScreen
import com.example.financetracker.ui.settings.SettingsViewModel
import com.example.financetracker.ui.splash.SplashScreen
import com.example.financetracker.ui.stats.StatsScreen
import com.example.financetracker.ui.stats.StatsViewModel
import com.example.financetracker.ui.theme.FinanceTrackerTheme
import com.example.financetracker.ui.transaction.AddTransactionScreen
import com.example.financetracker.ui.transaction.AddTransactionViewModel
import com.example.financetracker.ui.transaction.TransactionListScreen
import com.example.financetracker.ui.transaction.TransactionListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerApp()
        }
    }
}

@Composable
private fun FinanceTrackerApp() {
    val settingsVm: SettingsViewModel = viewModel(factory = AppViewModelFactory.Factory)
    val systemDark = isSystemInDarkTheme()
    val storedDark by settingsVm.darkTheme.collectAsStateWithLifecycle()
    val isDark = storedDark ?: systemDark

    val authVm: AuthViewModel = viewModel(factory = AppViewModelFactory.Factory)
    val startDestination by authVm.startDestination.collectAsStateWithLifecycle()

    FinanceTrackerTheme(darkTheme = isDark) {
        when (startDestination) {
            StartDestination.Splash -> LoadingScreen()
            else -> AppNavGraph(
                startRoute = if (startDestination == StartDestination.Home) Routes.DASHBOARD else Routes.SPLASH,
                authViewModel = authVm,
                settingsViewModel = settingsVm,
                isDarkTheme = isDark
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppNavGraph(
    startRoute: String,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startRoute) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onNeedsVerification = { navController.navigate(Routes.VERIFY_EMAIL) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onVerificationSent = {
                    navController.navigate(Routes.VERIFY_EMAIL) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VERIFY_EMAIL) {
            VerifyEmailScreen(
                viewModel = authViewModel,
                onVerified = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    authViewModel.reset()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            val vm: DashboardViewModel = viewModel(factory = AppViewModelFactory.Factory)
            DashboardScreen(
                viewModel = vm,
                selectedTab = HomeTab.Dashboard,
                onTabSelected = { tab -> navigateToTab(navController, tab) },
                onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                onTransactionClick = { id -> navController.navigate(Routes.editTransaction(id)) },
                onSeeAll = { navigateToTab(navController, HomeTab.Transactions) }
            )
        }

        composable(Routes.TRANSACTIONS) {
            val vm: TransactionListViewModel = viewModel(factory = AppViewModelFactory.Factory)
            TransactionListScreen(
                viewModel = vm,
                selectedTab = HomeTab.Transactions,
                onTabSelected = { tab -> navigateToTab(navController, tab) },
                onEdit = { id -> navController.navigate(Routes.editTransaction(id)) }
            )
        }

        composable(Routes.STATS) {
            val vm: StatsViewModel = viewModel(factory = AppViewModelFactory.Factory)
            StatsScreen(
                viewModel = vm,
                selectedTab = HomeTab.Stats,
                onTabSelected = { tab -> navigateToTab(navController, tab) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                isDarkTheme = isDarkTheme,
                selectedTab = HomeTab.Settings,
                onTabSelected = { tab -> navigateToTab(navController, tab) },
                onLoggedOut = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_TRANSACTION) {
            val vm: AddTransactionViewModel = viewModel(factory = AppViewModelFactory.Factory)
            AddTransactionScreen(
                viewModel = vm,
                editId = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val vm: AddTransactionViewModel = viewModel(factory = AppViewModelFactory.Factory)
            AddTransactionScreen(
                viewModel = vm,
                editId = id,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

private fun navigateToTab(
    navController: androidx.navigation.NavHostController,
    tab: HomeTab
) {
    val route = when (tab) {
        HomeTab.Dashboard -> Routes.DASHBOARD
        HomeTab.Transactions -> Routes.TRANSACTIONS
        HomeTab.Stats -> Routes.STATS
        HomeTab.Settings -> Routes.SETTINGS
    }
    navController.navigate(route) {
        popUpTo(Routes.DASHBOARD) {
            saveState = true
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}
