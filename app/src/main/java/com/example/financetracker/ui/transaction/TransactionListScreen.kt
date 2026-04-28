package com.example.financetracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.Category
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.ui.components.TransactionRow
import com.example.financetracker.ui.dashboard.HomeBottomBar
import com.example.financetracker.ui.dashboard.HomeTab
import com.example.financetracker.ui.theme.LocalFinanceColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onEdit: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val financeColors = LocalFinanceColors.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Транзакции", fontWeight = FontWeight.SemiBold) })
        },
        bottomBar = { HomeBottomBar(selectedTab, onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.filters.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Поиск по комментарию") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodFilter.entries.forEach { period ->
                    FilterChip(
                        selected = state.filters.period == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(period.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filters.type == null,
                    onClick = { viewModel.setType(null) },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = state.filters.type == TransactionType.INCOME,
                    onClick = { viewModel.setType(TransactionType.INCOME) },
                    label = { Text("Доход") }
                )
                FilterChip(
                    selected = state.filters.type == TransactionType.EXPENSE,
                    onClick = { viewModel.setType(TransactionType.EXPENSE) },
                    label = { Text("Расход") }
                )
            }
            Spacer(Modifier.height(8.dp))
            val availableCategories = state.filters.type?.let { Category.forType(it) }
                ?: Category.entries.toList()
            CategoryDropdown(
                selected = state.filters.categories,
                categories = availableCategories,
                onToggle = viewModel::toggleCategory,
                onClear = viewModel::clearCategories
            )
            Spacer(Modifier.height(12.dp))
            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет транзакций по выбранным фильтрам",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items, key = { it.id }) { tx ->
                        SwipeRow(
                            tx = tx,
                            onEdit = { onEdit(tx.id) },
                            onDelete = { viewModel.delete(tx) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: Set<Category>,
    categories: List<Category>,
    onToggle: (Category) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected.size) {
        0 -> "Все категории"
        1 -> selected.first().let { "${it.emoji} ${it.displayName}" }
        else -> "${selected.size} ${pluralCategories(selected.size)}"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Категории") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Все категории") },
                leadingIcon = {
                    Checkbox(
                        checked = selected.isEmpty(),
                        onCheckedChange = null
                    )
                },
                onClick = { onClear() }
            )
            HorizontalDivider()
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text("${cat.emoji} ${cat.displayName}") },
                    leadingIcon = {
                        Checkbox(
                            checked = cat in selected,
                            onCheckedChange = null
                        )
                    },
                    onClick = { onToggle(cat) }
                )
            }
        }
    }
}

private fun pluralCategories(n: Int): String = when {
    n % 100 in 11..14 -> "категорий"
    n % 10 == 1 -> "категория"
    n % 10 in 2..4 -> "категории"
    else -> "категорий"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val direction = state.dismissDirection
            val (color, icon, alignment) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    Icons.Default.Edit,
                    Alignment.CenterStart
                )
                SwipeToDismissBoxValue.EndToStart -> Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    Icons.Default.Delete,
                    Alignment.CenterEnd
                )
                else -> Triple(Color.Transparent, Icons.Default.Edit, Alignment.Center)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(imageVector = icon, contentDescription = null)
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TransactionRow(
                tx = tx,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}
