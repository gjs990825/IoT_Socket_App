package com.maverick.iotsocket.ui.alarms

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.R
import com.maverick.iotsocket.model.AlarmsUIData
import com.maverick.iotsocket.model.Date
import com.maverick.iotsocket.model.Time

class AlarmsViewModel(alarmsUIData: AlarmsUIData) : ViewModel() {
    private val TAG = "ControlCenterViewModel"

    val alarmTypeCheckedId = MutableLiveData(R.id.radioButtonPeriodic)
    val alarmStartDate = MutableLiveData<Date>()
    val alarmStartTime = MutableLiveData<Time>()
    val alarmEndDate = MutableLiveData<Date>()
    val alarmEndTime = MutableLiveData<Time>()

    val tickAlarmSecond = MutableLiveData<String>()
    val tickAlarmMinute = MutableLiveData<String>()
    val tickAlarmHour = MutableLiveData<String>()
    val tickAlarmCheckedId = MutableLiveData(R.id.radioButtonEveryXthSecond)
    val tickAlarmValid = MutableLiveData(false)

    fun updateTickAlarmValid() {
        tickAlarmValid.postValue(with(getTickAlarmCheckedInput()) { this != null && this > 0 })
    }

    fun updateTickAlarmValid(id: Int) {
        tickAlarmValid.postValue(with(getTickAlarmCheckedInput(id)) { this != null && this > 0 })
    }

    init {
        with(alarmsUIData) {
            alarmTypeCheckedId.value = if (radioButtonCheckedId == -1) {
                R.id.radioButtonPeriodic
            } else {
                radioButtonCheckedId
            }
            alarmStartDate.value = if (startDate == Date.getDefault()) Date.getNow() else startDate
            alarmStartTime.value = if (startTime == Time.getDefault()) Time.getNow() else startTime
            alarmEndDate.value = if (endDate == Date.getDefault()) Date.getNow() else endDate
            alarmEndTime.value = if (endTime == Time.getDefault()) Time.getNow() else endTime
        }
    }

    fun updateAlarmStartDate(date: Date) = alarmStartDate.postValue(date)
    fun updateAlarmStartTime(time: Time) = alarmStartTime.postValue(time)
    fun updateAlarmEndDate(date: Date) = alarmEndDate.postValue(date)
    fun updateAlarmEndTime(time: Time) = alarmEndTime.postValue(time)

    fun updateAlarmStartDate(month: Int, day: Int) = alarmStartDate.postValue(Date(month, day))
    fun updateAlarmStartTime(hour: Int, minute: Int) = alarmStartTime.postValue(Time(hour, minute))
    fun updateAlarmEndDate(month: Int, day: Int) = alarmEndDate.postValue(Date(month, day))
    fun updateAlarmEndTime(hour: Int, minute: Int) = alarmEndTime.postValue(Time(hour, minute))

    fun resetAlarmInput() {
        val dateNow = Date.getNow()
        val timeNow = Time.getNow()
        updateAlarmStartDate(dateNow)
        updateAlarmStartTime(timeNow)
        updateAlarmEndDate(dateNow)
        updateAlarmEndTime(timeNow)
    }

    fun getIntervalAlarmCommand(): String {
        val isOneShot: Boolean
        var cronStringStart = ""
        var cronStringEnd = ""

        val timeStart = alarmStartTime.value
        val dateStart = alarmStartDate.value
        val timeEnd = alarmEndTime.value
        val dateEnd = alarmEndDate.value

        when (alarmTypeCheckedId.value) {
            R.id.radioButtonPeriodic -> {
                // second minute hour day(month) month day(week)
                if (timeStart != null && timeEnd != null) {
                    cronStringStart = "0 ${timeStart.minute} ${timeStart.hour} * * *"
                    cronStringEnd = "0 ${timeEnd.minute} ${timeEnd.hour} * * *"
                }
                isOneShot = false
            }
            R.id.radioButtonOnce -> {
                if (timeStart != null && dateStart != null && timeEnd != null && dateEnd != null) {
                    cronStringStart =
                        "0 ${timeStart.minute} ${timeStart.hour} ${dateStart.day} ${dateStart.month + 1} *"
                    cronStringEnd =
                        "0 ${timeEnd.minute} ${timeEnd.hour} ${dateEnd.day} ${dateEnd.month + 1} *"
                }
                isOneShot = true
            }
            else -> {
                isOneShot = false
            }
        }
        return if (cronStringStart.isEmpty()) {
            Log.e(TAG, "cron parsing error")
            ""
        } else {
            Log.i(TAG, "$cronStringStart, $cronStringEnd")
            "alarm add \"${cronStringStart}\" \"relay true\" $isOneShot\n" +
                    "alarm add \"${cronStringEnd}\" \"relay false\" $isOneShot"
        }
    }

    fun resetTickAlarmInput() {
        val none = String()
        tickAlarmSecond.postValue(none)
        tickAlarmMinute.postValue(none)
        tickAlarmHour.postValue(none)
        tickAlarmCheckedId.postValue(R.id.radioButtonEveryXthSecond)
    }

    private val cronIndexMap = mapOf(
        Pair(R.id.radioButtonEveryXthSecond, 0),
        Pair(R.id.radioButtonEveryXthMinute, 1),
        Pair(R.id.radioButtonEveryXthHour, 2),
    )

    private fun getTickAlarmCheckedInput(id: Int): Int? {
        return try {
            when (id) {
                R.id.radioButtonEveryXthSecond -> tickAlarmSecond.value?.toInt()
                R.id.radioButtonEveryXthMinute -> tickAlarmMinute.value?.toInt()
                R.id.radioButtonEveryXthHour -> tickAlarmHour.value?.toInt()
                else -> null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun getTickAlarmCheckedInput(): Int? {
        return with(tickAlarmCheckedId.value) { if (this != null) getTickAlarmCheckedInput(this) else null }
    }

    fun getTickAlarmCommand(): String {
        val x = getTickAlarmCheckedInput()
        val index = cronIndexMap[tickAlarmCheckedId.value]

        return if (x != null && index != null && x > 0) {
            with(StringBuilder()) {
                for (i in 0 until index) append("0 ")
                append("*/$x ")
                for (i in index + 1 until 6) append("* ")
                val cronString = toString().trimEnd()
                "alarm add \"$cronString\" \"flip relay\" false"
            }
        } else {
            ""
        }
    }
}
