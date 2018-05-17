package com.dev.tasker.service.di

import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import com.dev.tasker.commons.data.local.TaskDb
import com.dev.tasker.core.constants.Constants
import com.dev.tasker.core.di.CoreComponent
import com.dev.tasker.core.networking.Scheduler
import com.dev.tasker.service.TaskerService
import com.dev.tasker.service.model.ServiceDataContract
import com.dev.tasker.service.model.ServiceLocalData
import com.dev.tasker.service.model.ServiceRepository
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@ServiceScope
@Component(dependencies = [CoreComponent::class], modules = [ServiceModule::class])
interface ServiceComponent {

    //Expose to dependent components
    fun postDb(): TaskDb
    fun scheduler(): Scheduler
    fun preferences(): SharedPreferences

    fun inject(service: TaskerService)
}

@Module
@ServiceScope
class ServiceModule {

    /*Repository*/
    @Provides
    @ServiceScope
    fun listRepo(local: ServiceDataContract.Local,
                 scheduler: Scheduler,
                 compositeDisposable: CompositeDisposable): ServiceDataContract.Repository
            = ServiceRepository(local, scheduler, compositeDisposable)

    @Provides
    @ServiceScope
    fun localData(taskDb: TaskDb, scheduler: Scheduler): ServiceDataContract.Local = ServiceLocalData(taskDb, scheduler)

    @Provides
    @ServiceScope
    fun compositeDisposable(): CompositeDisposable = CompositeDisposable()

    @Provides
    @ServiceScope
    fun postDb(context: Context): TaskDb = Room.databaseBuilder(context, TaskDb::class.java, Constants.DB_NAME).build()
}