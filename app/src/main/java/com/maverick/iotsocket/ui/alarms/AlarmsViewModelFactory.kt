package com.maverick.iotsocket.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maverick.iotsocket.model.AlarmsUIData


class AlarmsViewModelFactory(private val alarmsUIData: AlarmsUIData): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlarmsViewModel(alarmsUIData) as T
    }
}