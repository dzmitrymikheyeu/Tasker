package com.dev.tasker.list.di

import com.dev.tasker.commons.data.local.TaskDb
import com.dev.tasker.core.networking.Scheduler
import com.dev.tasker.list.fragment.CurrentTabFragment
import com.dev.tasker.list.fragment.DoneTabFragment
import com.dev.tasker.list.fragment.PendingTabFragment
import com.dev.tasker.list.model.ListDataContract
import com.dev.tasker.list.model.ListLocalData
import com.dev.tasker.list.model.ListRepository
import com.dev.tasker.list.viewmodel.ListViewModelFactory
import com.dev.tasker.service.di.ServiceComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@ListScope
@Component(dependencies = [ServiceComponent::class], modules = [ListModule::class])
interface ListComponent {
    fun inject(fragment: PendingTabFragment)
    fun inject(fragment: DoneTabFragment)
    fun inject(fragment: CurrentTabFragment)
}

@Module
@ListScope
class ListModule {

    /*ViewModel*/
    @Provides
    @ListScope
    fun currentListViewModelFactory(repository: ListDataContract.Repository,
                                    compositeDisposable: CompositeDisposable):
            ListViewModelFactory = ListViewModelFactory(repository, compositeDisposable)

    /*Repository*/
    @Provides
    @ListScope
    fun listRepo(local: ListDataContract.Local,
                 scheduler: Scheduler,
                 compositeDisposable: CompositeDisposable): ListDataContract.Repository
            = ListRepository(local, scheduler, compositeDisposable)

    @Provides
    @ListScope
    fun localData(taskDb: TaskDb, scheduler: Scheduler): ListDataContract.Local = ListLocalData(taskDb, scheduler)

    @Provides
    @ListScope
    fun compositeDisposable(): CompositeDisposable = CompositeDisposable()
}