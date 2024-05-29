package com.example.pawtrack

import com.example.pawtrack.Alarms.AlarmItem

interface AlarmScheduler {
    fun Schedule(item: AlarmItem)
    fun Cancel(item: AlarmItem)
}