package com.maverick.iotsocket.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.MessageCodeHelper
import com.maverick.iotsocket.MyApplication.Companion.context
import com.maverick.iotsocket.R
import com.maverick.iotsocket.connection.*

class MainActivityViewModel : ViewModel() {
    private val TAG = "MainViewModel"
    private val mIsLoading = MutableLiveData(false)
    private var mMessage = MutableLiveData<String>()
    private var connection = ConnectionManager.getConnection()
    private val busyStateHelper = BusyStateHelper()

    init {
        connection.onBusyStateChanged = busyStateHelper
    }

    val isLoading: LiveData<Boolean> get() = mIsLoading

    val message: LiveData<String> get() = mMessage

    inner class BusyStateHelper: OnBusyStateChanged {
        override fun onBusyStateChanged(state: Boolean) {
            mIsLoading.postValue(state)
        }
    }

    inner class ConnectResultCallback : OperationResultCallback {
        override fun onResult(operationStatus: OperationStatus) {
            Log.i(TAG, "connect callback: $operationStatus")
            if (operationStatus == OperationStatus.FAIL){
                mMessage.postValue(context.getString(R.string.prompt_connection_error))
            }
        }
    }

    inner class CommandResponseCallback : ResponseCallback {
        override fun onResponse(operationStatus: OperationStatus, message_code: Int) {
            Log.i(TAG, "ack callback: $operationStatus")
            when (operationStatus) {
                OperationStatus.SUCCESS -> {
                    if (message_code != 0) {
                        mMessage.postValue(MessageCodeHelper.get(message_code))
                    }
                }
                OperationStatus.FAIL -> mMessage.postValue(context.getString(R.string.prompt_send_failed))
                OperationStatus.TIMEOUT -> mMessage.postValue(context.getString(R.string.prompt_ack_timeout))
                OperationStatus.ERROR -> mMessage.postValue("Something is Wrong")
            }
        }
    }

    fun clearErrorMessage() {
        mMessage = MutableLiveData<String>()
    }

    fun disconnect() {
        connection.disconnect()
    }

    fun connect() {
        connection.connect(ConnectResultCallback())
    }

    fun sendCommand(command: String) {
        connection.sendCommand(command, CommandResponseCallback())
    }

    fun switchBT() {
        connection.disconnect()
        if (ConnectionManager.switchToBluetooth()) {
            connection = ConnectionManager.getConnection()
            connect()
        }
    }

    fun switchMqtt() {
        connection.disconnect()
        ConnectionManager.switchToMqtt()
        connection = ConnectionManager.getConnection()
        connect()
    }

//    fun switchConnection() {
//        connection.disconnect()
//        connection = ConnectionManager.getConnection()
//        connect()
//    }
}