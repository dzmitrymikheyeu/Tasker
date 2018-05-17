package com.dev.tasker.core.di

import PreferenceHelper
import android.content.Context
import android.content.SharedPreferences
import com.dev.tasker.core.R
import dagger.Module
import dagger.Provides
import set
import javax.inject.Singleton

@Module
class StorageModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(context: Context): SharedPreferences {
        val prefs = PreferenceHelper.defaultPrefs(context)
        prefs[PreferenceHelper.PREFS_TAGS] = context.resources.getStringArray(R.array.tags).joinToString()
        return prefs
    }
}