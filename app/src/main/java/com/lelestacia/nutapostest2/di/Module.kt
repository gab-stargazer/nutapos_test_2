package com.lelestacia.nutapostest2.di

import androidx.room.Room
import com.lelestacia.nutapostest2.data.db.TransactionDb
import com.lelestacia.nutapostest2.data.db.dao.TransactionDao
import com.lelestacia.nutapostest2.data.repository.TransactionRepositoryImpl
import com.lelestacia.nutapostest2.domain.repository.TransactionRepository
import com.lelestacia.nutapostest2.ui.tambah_transaksi.TambahTransaksiViewModel
import com.lelestacia.nutapostest2.ui.uang_masuk.UangMasukViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val module = module {
    single<TransactionDb>(createdAtStart = true) {
        Room
            .databaseBuilder(
                androidContext(),
                TransactionDb::class.java,
                "transaction_db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    single<TransactionDao>(createdAtStart = true) {
        val db: TransactionDb = get()
        db.getDao()
    }

    singleOf(::TransactionRepositoryImpl) {
        binds(listOf(TransactionRepository::class))
        createdAtStart()
    }

    viewModelOf(::TambahTransaksiViewModel)
    viewModelOf(::UangMasukViewModel)
}