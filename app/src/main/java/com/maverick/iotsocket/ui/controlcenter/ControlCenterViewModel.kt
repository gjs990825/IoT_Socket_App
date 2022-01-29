package com.maverick.iotsocket.ui.controlcenter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.R
import com.maverick.iotsocket.model.ControlCenterUIData
import com.maverick.iotsocket.model.Date
import com.maverick.iotsocket.model.Time

class ControlCenterViewModel(controlCenterUIData: ControlCenterUIData) : ViewModel() {
    private val TAG = "ControlCenterViewModel"

    private val mAlarmTypeCheckedId = MutableLiveData(R.id.radioButtonPeriodic)
    private val mAlarmStartDate = MutableLiveData<Date>()
    private val mAlarmStartTime = MutableLiveData<Time>()
    private val mAlarmEndDate = MutableLiveData<Date>()
    private val mAlarmEndTime = MutableLiveData<Time>()

    init {
        with(controlCenterUIData) {
            mAlarmTypeCheckedId.value = if (radioButtonCheckedId == -1) {
                R.id.radioButtonPeriodic
            } else {
                radioButtonCheckedId
            }
            mAlarmStartDate.value = if (startDate == Date.getDefault()) Date.getNow() else startDate
            mAlarmStartTime.value = if (startTime == Time.getDefault()) Time.getNow() else startTime
            mAlarmEndDate.value = if (endDate == Date.getDefault()) Date.getNow() else endDate
            mAlarmEndTime.value = if (endTime == Time.getDefault()) Time.getNow() else endTime
        }
    }

    val alarmStartDate: LiveData<Date> get() = mAlarmStartDate
    val alarmStartTime: LiveData<Time> get() = mAlarmStartTime
    val alarmEndDate: LiveData<Date> get() = mAlarmEndDate
    val alarmEndTime: LiveData<Time> get() = mAlarmEndTime

    fun updateAlarmTypeCheckedId(id: Int) = mAlarmTypeCheckedId.postValue(id)
    fun updateAlarmStartDate(date: Date) = mAlarmStartDate.postValue(date)
    fun updateAlarmStartTime(time: Time) = mAlarmStartTime.postValue(time)
    fun updateAlarmEndDate(date: Date) = mAlarmEndDate.postValue(date)
    fun updateAlarmEndTime(time: Time) = mAlarmEndTime.postValue(time)

    fun resetAlarmInput() {
        val dateNow = Date.getNow()
        val timeNow = Time.getNow()
        updateAlarmStartDate(dateNow)
        updateAlarmStartTime(timeNow)
        updateAlarmEndDate(dateNow)
        updateAlarmEndTime(timeNow)
    }

    fun getCommandString(): String {
        var isOneShot = 1
        var cronStringStart = ""
        var cronStringEnd = ""

        val timeStart = mAlarmStartTime.value
        val dateStart = mAlarmStartDate.value
        val timeEnd = mAlarmEndTime.value
        val dateEnd = mAlarmEndDate.value

        when (mAlarmTypeCheckedId.value) {
            R.id.radioButtonPeriodic -> {
                // second minute hour day(month) month day(week)
                if (timeStart != null && timeEnd != null) {
                    cronStringStart = "0 ${timeStart.minute} ${timeStart.hour} * * *"
                    cronStringEnd = "0 ${timeEnd.minute} ${timeEnd.hour} * * *"
                }
                isOneShot = 0
            }
            R.id.radioButtonOnce -> {
                if (timeStart != null && dateStart != null && timeEnd != null && dateEnd != null) {
                    cronStringStart =
                        "0 ${timeStart.minute} ${timeStart.hour} ${dateStart.day} ${dateStart.month + 1} *"
                    cronStringEnd =
                        "0 ${timeEnd.minute} ${timeEnd.hour} ${dateEnd.day} ${dateEnd.month + 1} *"
                }
                isOneShot = 1
            }
        }
        return if (cronStringStart.isEmpty()) {
            Log.e(TAG, "cron parsing error")
            ""
        } else {
            Log.i(TAG, "$cronStringStart, $cronStringEnd")
            "alarm add \"${cronStringStart}\" \"relay 1\" $isOneShot\n" +
                    "alarm add \"${cronStringEnd}\" \"relay 0\" $isOneShot"
        }
    }
}