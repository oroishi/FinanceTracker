package com.example.financetracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(name: String): Category = Category.valueOf(name)

    @TypeConverter
    fun fromType(type: TransactionType): String = type.name

    @TypeConverter
    fun toType(name: String): TransactionType = TransactionType.valueOf(name)
}
