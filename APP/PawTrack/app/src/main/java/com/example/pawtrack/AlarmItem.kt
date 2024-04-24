package com.example.pawtrack


import java.time.LocalDateTime
import java.time.LocalTime
data class AlarmItem(
    val time: LocalTime,
    val message: String,
    val repeat: Boolean
)