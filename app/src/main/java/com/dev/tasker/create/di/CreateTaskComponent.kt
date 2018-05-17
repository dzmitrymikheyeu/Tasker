package com.dev.tasker.create.di

import android.content.SharedPreferences
import com.dev.tasker.commons.data.local.TaskDb
import com.dev.tasker.core.networking.Scheduler
import com.dev.tasker.create.CreateTaskActivity
import com.dev.tasker.create.model.TaskDataContract
import com.dev.tasker.create.model.TaskLocalData
import com.dev.tasker.create.model.TaskRepository
import com.dev.tasker.create.viewmodel.CreateViewModelFactory
import com.dev.tasker.service.di.ServiceComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@CreateScope
@Component(dependencies = [ServiceComponent::class], modules = [CreateTaskModule::class])
interface DetailsComponent {
    fun inject(detailsActivity: CreateTaskActivity)
}

@Module
class CreateTaskModule {

    /*ViewModel*/
    @Provides
    @CreateScope
    fun createTaskViewModelFactory(repo: TaskDataContract.Repository,
                                   compositeDisposable: CompositeDisposable)
            : CreateViewModelFactory {
        return CreateViewModelFactory(repo, compositeDisposable)
    }

    /*Repository*/
    @Provides
    @CreateScope
    fun createTaskRepo(local: TaskDataContract.Local): TaskDataContract.Repository {
        return TaskRepository(local)
    }

    @Provides
    @CreateScope
    fun localData(preferences: SharedPreferences, taskDb: TaskDb, scheduler: Scheduler)
            : TaskDataContract.Local{
        return TaskLocalData(preferences, taskDb, scheduler)
    }

    @Provides
    @CreateScope
    fun compositeDisposable(): CompositeDisposable = CompositeDisposable()
}