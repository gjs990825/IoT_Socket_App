package com.maverick.iotsocket.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.util.MessageCodeHelper
import com.maverick.iotsocket.MyApplication.Companion.context
import com.maverick.iotsocket.R
import com.maverick.iotsocket.connection.*

class MainActivityViewModel : ViewModel(), ConnectionManager.ConnectionTypeChangeListener {
    private val TAG = "MainViewModel"
    private val mIsLoading = MutableLiveData(false)
    private var mMessage = MutableLiveData<String>()
    private var connection = ConnectionManager.getConnection()
    private val busyStateListener = BusyStateListener()
    var isUsingBluetoothConnection = MutableLiveData(false)

    init {
        connection.onBusyStateChanged = busyStateListener
        ConnectionManager.setTypeChangeListener(this)
    }

    val isLoading: LiveData<Boolean> get() = mIsLoading

    val message: LiveData<String> get() = mMessage

    fun setMessage(msg: String) {
        mMessage.postValue(msg)
    }

    fun setMessage(id: Int) {
        mMessage.postValue(context.getString(id))
    }

    inner class BusyStateListener: OnBusyStateChanged {
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
                        setMessage(MessageCodeHelper.get(message_code))
                    }
                }
                OperationStatus.FAIL -> setMessage(context.getString(R.string.prompt_send_failed))
                OperationStatus.TIMEOUT -> setMessage(context.getString(R.string.prompt_ack_timeout))
                OperationStatus.ERROR -> setMessage(R.string.text_something_wrong)
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
        if (ConnectionManager.bluetoothConnectionAvailable()) {
            connection.disconnect()
            if (ConnectionManager.switchToBluetooth()) {
                connection = ConnectionManager.getConnection()
                connect()
            }
        } else {
            setMessage(context.getString(R.string.prompt_please_select_a_bluetooth_device_first))
        }
    }

    fun switchMqtt() {
        connection.disconnect()
        ConnectionManager.switchToMqtt()
        connection = ConnectionManager.getConnection()
        connect()
    }

    override fun onTypeChange(type: ConnectionManager.Type) {
        isUsingBluetoothConnection.postValue(type == ConnectionManager.Type.BLUETOOTH)
    }
}