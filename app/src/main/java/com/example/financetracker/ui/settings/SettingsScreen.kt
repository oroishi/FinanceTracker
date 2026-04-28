package com.example.financetracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.ui.dashboard.HomeBottomBar
import com.example.financetracker.ui.dashboard.HomeTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onLoggedOut: () -> Unit
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки", fontWeight = FontWeight.SemiBold) })
        },
        bottomBar = { HomeBottomBar(selectedTab, onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = email.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Аккаунт", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(email.ifBlank { "—" }, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Темная тема",
                        subtitle = if (isDarkTheme) "Включена" else "Отключена",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = viewModel::setDarkTheme
                            )
                        }
                    )
                    HorizontalDivider()
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Выйти из аккаунта",
                        subtitle = "Завершить текущую сессию",
                        onClick = { showLogoutConfirm = true }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "О приложении",
                    subtitle = "FinanceTracker v1.0"
                )
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Вы будете перенаправлены на экран входа.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout(onLoggedOut)
                }) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val base = Modifier.fillMaxWidth()
    val rowModifier = if (onClick != null) base.clickable(onClick = onClick) else base
    Row(
        modifier = rowModifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}
