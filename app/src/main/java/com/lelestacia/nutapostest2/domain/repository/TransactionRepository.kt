package com.lelestacia.nutapostest2.domain.repository

import com.lelestacia.nutapostest2.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun saveTransaction(transaction: Transaction)

    suspend fun getTransactionBetweenRange(start: Long, end: Long): Flow<List<Transaction>>

    suspend fun deleteTransaction(transaction: Transaction)
}