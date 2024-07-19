package com.lelestacia.nutapostest2.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.lelestacia.nutapostest2.data.db.model.TransactionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM `transaction` WHERE transaction_date BETWEEN :start AND :end")
    fun getTransactionInRange(start: Long, end: Long): Flow<List<TransactionModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionModel)

    @Upsert
    suspend fun upsertTransaction(transaction: TransactionModel)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionModel)
}