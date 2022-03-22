package com.maverick.iotsocket.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.R

class TasksViewModel : ViewModel() {
    private val TAG = "TasksViewModel"

    val basicTaskUserInput = MutableLiveData<String>()
    val basicTaskRadioGroupConditionCheckedId =
        MutableLiveData(R.id.radioButtonBasicTaskConditionBrightness)
    val basicTaskRadioGroupTypeCheckedId = MutableLiveData(R.id.radioButtonBasicTaskTypeHigher)
    val basicTaskValid = MutableLiveData(false)

    fun updateBasicTaskValid() = basicTaskValid.postValue(getBasicTaskUserInputValue() != null)

    private fun getBasicTaskUserInputValue(): Float? {
        return try {
            basicTaskUserInput.value?.toFloat()
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
        val inputValue = getBasicTaskUserInputValue()

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

    val mappingTaskRadioGroupConditionCheckedId =
        MutableLiveData(R.id.radioButtonMappingTaskConditionBrightness)
    val mappingTaskMaxIn = MutableLiveData<String>()
    val mappingTaskMinIn = MutableLiveData<String>()
    val mappingTaskMaxOut = MutableLiveData<String>()
    val mappingTaskMinOut = MutableLiveData<String>()
    val mappingTaskValid = MutableLiveData(false)

    fun updateMappingTaskValid() {
        mappingTaskValid.postValue(getMappingTaskCommand().isNotEmpty())
    }

    fun getMappingTaskCommand(): String {
        val condition = when (mappingTaskRadioGroupConditionCheckedId.value) {
            R.id.radioButtonMappingTaskConditionTemperature -> "temperature "
            R.id.radioButtonMappingTaskConditionBrightness -> "brightness "
            R.id.radioButtonMappingTaskConditionPressure -> "pressure "
            else -> null
        } ?: return ""

        val maxIn: Float?
        val maxOut: Float?
        val minIn: Float?
        val minOut: Float?

        try {
            maxIn = mappingTaskMaxIn.value?.toFloat()
            maxOut = mappingTaskMaxOut.value?.toFloat()
            minIn = mappingTaskMinIn.value?.toFloat()
            minOut = mappingTaskMinOut.value?.toFloat()
        } catch (e: NumberFormatException) {
            return ""
        }

        return if (maxIn != null &&
            minIn != null &&
            maxOut != null &&
            minOut != null
        ) {
            StringBuilder("task add pwm ")
                .append("$condition ")
                .append("linear ")
                .append("$minIn ")
                .append("$maxIn ")
                .append("$minOut ")
                .append("$maxOut")
                .toString()
        } else {
            ""
        }
    }
}