package com.maverick.iotsocket.ui.commands

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommandsViewModel : ViewModel() {
    val commandInput = MutableLiveData<String>()
    val isCommandValid = MutableLiveData(false)

    fun updateIsCommandValid() {
        isCommandValid.postValue(!commandInput.value.isNullOrBlank())
    }

    fun getUserInputCommand(): String = commandInput.value ?: ""
}