package com.lelestacia.nutapostest2.data.repository

import com.lelestacia.nutapostest2.data.db.dao.TransactionDao
import com.lelestacia.nutapostest2.data.db.model.TransactionModel
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.lelestacia.nutapostest2.domain.repository.TransactionRepository
import com.lelestacia.nutapostest2.util.toBase64
import com.lelestacia.nutapostest2.util.toBitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun saveTransaction(transaction: Transaction) {

        val dbModel = TransactionModel(
            id = transaction.id,
            receiver = transaction.receiver,
            sender = transaction.sender,
            description = transaction.description,
            amount = transaction.amount,
            type = transaction.type,
            date = transaction.date,
            image = transaction.image?.toBase64()
        )

        dao.upsertTransaction(dbModel)
    }

    override suspend fun getTransactionBetweenRange(start: Long, end: Long): Flow<List<Transaction>> {
        return dao.getTransactionInRange(start, end).map { list ->
            list.map {
                Transaction(
                    id = it.id,
                    receiver = it.receiver,
                    sender = it.sender,
                    amount = it.amount,
                    description = it.description,
                    type = it.type,
                    image = it.image?.toBitmap(),
                    date = it.date
                )
            }
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(
            TransactionModel(
                id = transaction.id,
                receiver = transaction.receiver,
                sender = transaction.sender,
                description = transaction.description,
                amount = transaction.amount,
                type = transaction.type,
                date = transaction.date,
                image = transaction.image?.toBase64()
            )
        )
    }
}