package com.lelestacia.nutapostest2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lelestacia.nutapostest2.data.db.dao.TransactionDao
import com.lelestacia.nutapostest2.data.db.model.TransactionModel

@Database(
    entities = [TransactionModel::class],
    version = 1
)
abstract class TransactionDb : RoomDatabase() {

    abstract fun getDao(): TransactionDao
}