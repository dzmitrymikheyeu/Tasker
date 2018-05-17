package com.dev.tasker.core.di

import android.content.Context
import android.content.SharedPreferences
import com.dev.tasker.core.networking.Scheduler
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, StorageModule::class])
interface CoreComponent {

    fun context(): Context

    fun sharedPreferences(): SharedPreferences

    fun scheduler(): Scheduler
}