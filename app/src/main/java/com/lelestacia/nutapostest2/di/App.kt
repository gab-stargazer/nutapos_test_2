package com.lelestacia.nutapostest2.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(
            koinApplication {
                androidContext(this@App)
                modules(module)
            }
        )
    }
}