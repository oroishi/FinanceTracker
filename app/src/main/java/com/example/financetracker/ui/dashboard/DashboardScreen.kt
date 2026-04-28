package com.example.financetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.ui.components.LegendList
import com.example.financetracker.ui.components.PieChart
import com.example.financetracker.ui.components.PieSlice
import com.example.financetracker.ui.components.TransactionRow
import com.example.financetracker.ui.components.colorForIndex
import com.example.financetracker.ui.components.formatMoney
import com.example.financetracker.ui.theme.LocalFinanceColors

enum class HomeTab { Dashboard, Transactions, Stats, Settings }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAll: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val financeColors = LocalFinanceColors.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Главная", fontWeight = FontWeight.SemiBold) })
        },
        bottomBar = { HomeBottomBar(selectedTab, onTabSelected) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.size(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Текущий баланс",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = formatMoney(state.balance),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.size(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BalanceMetric(
                                title = "Доход за месяц",
                                value = formatMoney(state.monthIncome)
                            )
                            BalanceMetric(
                                title = "Расход за месяц",
                                value = formatMoney(state.monthExpense),
                                alignEnd = true
                            )
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Расходы за месяц",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.size(12.dp))
                        if (state.categoryBreakdown.isEmpty()) {
                            Text(
                                text = "Нет расходов в этом месяце",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        } else {
                            val slices = state.categoryBreakdown.mapIndexed { i, (cat, value) ->
                                PieSlice(
                                    label = "${cat.emoji} ${cat.displayName}",
                                    value = value,
                                    color = colorForIndex(i)
                                )
                            }
                            PieChart(
                                slices = slices,
                                centerText = formatMoney(state.monthExpense)
                            )
                            Spacer(Modifier.size(8.dp))
                            LegendList(
                                slices = slices,
                                valueFormatter = { formatMoney(it) }
                            )
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Последние транзакции",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = onSeeAll) { Text("Все") }
                }
            }

            if (state.recent.isEmpty()) {
                item {
                    Text(
                        text = "Здесь появятся ваши транзакции",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(state.recent, key = { it.id }) { tx ->
                    TransactionRow(
                        tx = tx,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.size(8.dp))
                }
            }
            item { Spacer(Modifier.size(80.dp)) }
        }
    }
}

@Composable
private fun BalanceMetric(title: String, value: String, alignEnd: Boolean = false) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HomeBottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == HomeTab.Dashboard,
            onClick = { onSelect(HomeTab.Dashboard) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Главная") }
        )
        NavigationBarItem(
            selected = selected == HomeTab.Transactions,
            onClick = { onSelect(HomeTab.Transactions) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
            label = { Text("Список") }
        )
        NavigationBarItem(
            selected = selected == HomeTab.Stats,
            onClick = { onSelect(HomeTab.Stats) },
            icon = { Icon(Icons.Default.BarChart, null) },
            label = { Text("Статистика") }
        )
        NavigationBarItem(
            selected = selected == HomeTab.Settings,
            onClick = { onSelect(HomeTab.Settings) },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Настройки") }
        )
    }
}
