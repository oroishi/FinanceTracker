package com.example.financetracker.data

enum class TransactionType { INCOME, EXPENSE }

enum class Category(
    val displayName: String,
    val type: TransactionType
) {
    SALARY("Зарплата", TransactionType.INCOME),
    BONUS("Премия", TransactionType.INCOME),
    INVESTMENT("Инвестиции", TransactionType.INCOME),
    OTHER_INCOME("Другие доходы", TransactionType.INCOME),

    FOOD("Еда", TransactionType.EXPENSE),
    TRANSPORT("Транспорт", TransactionType.EXPENSE),
    HOUSING("Жилье", TransactionType.EXPENSE),
    UTILITIES("Коммуналка", TransactionType.EXPENSE),
    ENTERTAINMENT("Развлечения", TransactionType.EXPENSE),
    HEALTH("Здоровье", TransactionType.EXPENSE),
    SHOPPING("Покупки", TransactionType.EXPENSE),
    EDUCATION("Образование", TransactionType.EXPENSE),
    OTHER_EXPENSE("Другие расходы", TransactionType.EXPENSE);

    companion object {
        fun forType(type: TransactionType): List<Category> =
            entries.filter { it.type == type }
    }
}
