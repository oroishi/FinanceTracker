package com.example.financetracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.ui.components.LegendList
import com.example.financetracker.ui.components.LineChart
import com.example.financetracker.ui.components.LinePoint
import com.example.financetracker.ui.components.PieChart
import com.example.financetracker.ui.components.PieSlice
import com.example.financetracker.ui.components.colorForIndex
import com.example.financetracker.ui.components.formatMoney
import com.example.financetracker.ui.dashboard.HomeBottomBar
import com.example.financetracker.ui.dashboard.HomeTab
import com.example.financetracker.ui.theme.LocalFinanceColors
import com.example.financetracker.ui.transaction.PeriodFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val financeColors = LocalFinanceColors.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Статистика", fontWeight = FontWeight.SemiBold) })
        },
        bottomBar = { HomeBottomBar(selectedTab, onTabSelected) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PeriodFilter.WEEK, PeriodFilter.MONTH, PeriodFilter.YEAR, PeriodFilter.ALL).forEach { p ->
                        FilterChip(
                            selected = state.period == p,
                            onClick = { viewModel.setPeriod(p) },
                            label = { Text(p.label) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Итоги", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        TotalsRow("Доходы", formatMoney(state.totalIncome), financeColors.income)
                        TotalsRow("Расходы", formatMoney(state.totalExpense), financeColors.expense)
                        TotalsRow("Баланс", formatMoney(state.balance), MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Расходы по категориям", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        if (state.expenseByCategory.isEmpty()) {
                            Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp))
                        } else {
                            val slices = state.expenseByCategory.mapIndexed { i, (cat, value) ->
                                PieSlice(
                                    label = "${cat.emoji} ${cat.displayName}",
                                    value = value,
                                    color = colorForIndex(i)
                                )
                            }
                            PieChart(slices = slices, centerText = formatMoney(state.totalExpense))
                            Spacer(Modifier.height(8.dp))
                            LegendList(slices = slices, valueFormatter = { formatMoney(it) })
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Динамика", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        if (state.timeline.isEmpty()) {
                            Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp))
                        } else {
                            LineChart(
                                incomeSeries = state.timeline.map { LinePoint(it.label, it.income) },
                                expenseSeries = state.timeline.map { LinePoint(it.label, it.expense) },
                                incomeColor = financeColors.income,
                                expenseColor = financeColors.expense
                            )
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun TotalsRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Text(text = value, fontWeight = FontWeight.SemiBold, color = color)
    }
}
