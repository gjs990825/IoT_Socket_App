package com.maverick.iotsocket.model

import java.util.*


data class Date(val month: Int, val day: Int) {
    companion object {
        private val notADate = Date(-1, -1)

        fun getDefault() = notADate

        fun getNow(): Date {
            with(Calendar.getInstance()) {
                return Date(get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
            }
        }
    }

    fun getFormatted() = "${month + 1}/$day"
}
