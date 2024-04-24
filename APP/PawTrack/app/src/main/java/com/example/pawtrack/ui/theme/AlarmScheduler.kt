package com.example.pawtrack

import org.jetbrains.annotations.Async.Schedule

interface AlarmScheduler {
    fun Schedule(item: AlarmItem)
    fun Cancel(item: AlarmItem)
}