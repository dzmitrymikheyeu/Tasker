package com.dev.tasker.commons

import com.dev.tasker.core.CoreApp
import com.dev.tasker.create.di.DaggerDetailsComponent
import com.dev.tasker.create.di.DetailsComponent
import com.dev.tasker.list.di.DaggerListComponent
import com.dev.tasker.list.di.ListComponent
import com.dev.tasker.service.di.DaggerServiceComponent
import com.dev.tasker.service.di.ServiceComponent
import javax.inject.Singleton

@Singleton
object TaskDH {
    private var listComponent: ListComponent? = null
    private var detailsComponent: DetailsComponent? = null
    private var serviceComponent: ServiceComponent? = null

    fun listComponent(): ListComponent {
        if (listComponent == null)
            listComponent = DaggerListComponent.builder().serviceComponent(serviceComponent()).build()
        return listComponent as ListComponent
    }

    fun destroyListComponent() {
        listComponent = null
    }

    fun serviceComponent(): ServiceComponent {
        if (serviceComponent == null)
            serviceComponent = DaggerServiceComponent.builder().coreComponent(CoreApp.coreComponent).build()
        return serviceComponent as ServiceComponent
    }

    fun destroyServiceComponent() {
        serviceComponent = null
    }

    fun createComponent(): DetailsComponent {
        if (detailsComponent == null)
            detailsComponent = DaggerDetailsComponent.builder().serviceComponent(serviceComponent()).build()
        return detailsComponent as DetailsComponent
    }

    fun destroyDetailsComponent() {
        detailsComponent = null
    }
}