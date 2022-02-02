package com.maverick.iotsocket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maverick.iotsocket.model.IoTSocket

class HomeViewModelFactory(private val ioTSocket: IoTSocket?): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(ioTSocket) as T
    }
}