package com.maverick.iotsocket.ui.tasks

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.R

class TasksViewModel : ViewModel() {
    private val TAG = "TasksViewModel"

    val basicTaskInput = MutableLiveData<String>()
    val basicTaskRadioGroupConditionCheckedId =
        MutableLiveData(R.id.radioButtonBasicTaskConditionTemperature)
    val basicTaskRadioGroupTypeCheckedId = MutableLiveData(R.id.radioButtonBasicTaskTypeHigher)
    val basicTaskValid = MutableLiveData(false)

    fun updateBasicTaskValid() {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                basicTaskValid.postValue(
                    with(getBasicTaskInput()) {
                        Log.i(TAG, "basicTaskValid call")
                        this != null
                    }
                )
            }, 100)
        }
    }

    private fun getBasicTaskInput(): Int? {
        return try {
            basicTaskInput.value?.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun getBasicTaskCommand(): String {
        val condition = when (basicTaskRadioGroupConditionCheckedId.value) {
            R.id.radioButtonBasicTaskConditionTemperature -> "temperature "
            R.id.radioButtonBasicTaskConditionBrightness -> "brightness "
            R.id.radioButtonBasicTaskConditionPressure -> "pressure "
            else -> null
        }
        val type = when (basicTaskRadioGroupTypeCheckedId.value) {
            R.id.radioButtonBasicTaskTypeHigher -> "higher "
            R.id.radioButtonBasicTaskTypeLower -> "lower "
            else -> null
        }
        val inputValue = getBasicTaskInput()

        return if (condition != null && type != null && inputValue != null) {
            StringBuilder("task add relay ")
                .append(condition)
                .append(type)
                .append("$inputValue ")
                .toString()
        } else {
            ""
        }
    }
}