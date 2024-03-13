package com.example.pawtrack

import com.example.pawtrack.AlarmItem
import org.jetbrains.annotations.Async.Schedule

interface AlarmScheduler {
    fun Schedule(item: AlarmItem)
    fun Cancel(item: AlarmItem)
}