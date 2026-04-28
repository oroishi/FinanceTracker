package com.example.financetracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["firebaseUid"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firebaseUid: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)
