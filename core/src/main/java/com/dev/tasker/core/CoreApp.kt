package com.dev.tasker.core

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.dev.tasker.core.di.AppModule
import com.dev.tasker.core.di.CoreComponent
import com.dev.tasker.core.di.DaggerCoreComponent
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo

open class CoreApp : Application() {

    companion object {
        lateinit var coreComponent: CoreComponent
    }

    override fun onCreate() {
        super.onCreate()
        RxPaparazzo.register(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initDI()
    }

    private fun initDI() {
        coreComponent = DaggerCoreComponent.builder().appModule(AppModule(this)).build()
    }
}