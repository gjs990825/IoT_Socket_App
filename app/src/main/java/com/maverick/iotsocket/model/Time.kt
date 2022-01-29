package com.maverick.iotsocket.model

import java.util.*

data class Time(val hour: Int, val minute: Int) {
    companion object {
        private val notTime = Time(-1, -1)

        fun getDefault() = notTime

        fun getNow(): Time {
            with(Calendar.getInstance()) {
                return Time(get(Calendar.HOUR), get(Calendar.MINUTE))
            }
        }
    }

    fun getFormatted() = "$hour:$minute"
}
