package com.example.financetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.financetracker.data.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun findByFirebaseUid(uid: String): User?
}
