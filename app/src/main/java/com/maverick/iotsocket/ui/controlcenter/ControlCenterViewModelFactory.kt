package com.maverick.iotsocket.ui.controlcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maverick.iotsocket.model.ControlCenterUIData


class ControlCenterViewModelFactory(private val controlCenterUIData: ControlCenterUIData): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ControlCenterViewModel(controlCenterUIData) as T
    }
}