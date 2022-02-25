package com.maverick.iotsocket.util

import java.time.LocalDateTime

fun LocalDateTime.toCron(): String {
    return String.format("%s %s %s %s %s %s", second, minute, hour, dayOfMonth, month.value, dayOfWeek.value)
}
