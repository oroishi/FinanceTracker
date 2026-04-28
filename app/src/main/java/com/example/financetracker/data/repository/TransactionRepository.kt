package com.example.financetracker.data.repository

import com.example.financetracker.data.dao.TransactionDao
import com.example.financetracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val dao: TransactionDao
) {

    fun observeAll(userId: Long): Flow<List<TransactionEntity>> =
        dao.observeAll(userId)

    fun observeRecent(userId: Long, limit: Int = 5): Flow<List<TransactionEntity>> =
        dao.observeRecent(userId, limit)

    fun observeByPeriod(userId: Long, from: Long, to: Long): Flow<List<TransactionEntity>> =
        dao.observeByPeriod(userId, from, to)

    suspend fun add(tx: TransactionEntity): Long = dao.insert(tx)

    suspend fun update(tx: TransactionEntity) = dao.update(tx)

    suspend fun delete(tx: TransactionEntity) = dao.delete(tx)

    suspend fun findById(id: Long): TransactionEntity? = dao.findById(id)
}
