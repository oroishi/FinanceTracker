package com.example.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.data.TransactionType
import com.example.financetracker.data.entity.TransactionEntity
import com.example.financetracker.ui.theme.LocalFinanceColors

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    modifier: Modifier = Modifier
) {
    val financeColors = LocalFinanceColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = tx.category.emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.category.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            val sub = buildString {
                append(formatShortDate(tx.date))
                if (tx.comment.isNotBlank()) append(" • ").append(tx.comment)
            }
            Text(
                text = sub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
        val color = if (tx.type == TransactionType.INCOME) financeColors.income else financeColors.expense
        Text(
            text = "$sign${formatMoney(tx.amount)}",
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}
