package com.dev.tasker.core.networking

import io.reactivex.Scheduler

interface Scheduler {
    fun mainThread():Scheduler
    fun io():Scheduler
}