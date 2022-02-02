package com.maverick.iotsocket.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.MessageCodeHelper
import com.maverick.iotsocket.MqttHelper
import com.maverick.iotsocket.MyApplication.Companion.context
import com.maverick.iotsocket.R

class MainActivityViewModel : ViewModel() {
    private val TAG = "MainViewModel"
    private val mIsLoading = MutableLiveData(false)
    private var mMessage = MutableLiveData<String>()

    val isLoading: LiveData<Boolean> get() = mIsLoading

    val message: LiveData<String> get() = mMessage

    inner class MqttConnectCallback : MqttHelper.OperationResultCallback {
        override fun onResult(operationStatus: MqttHelper.OperationStatus) {
            Log.i(TAG, "mqtt connect callback: $operationStatus")
            mIsLoading.postValue(false)
            if (operationStatus == MqttHelper.OperationStatus.FAIL){
                mMessage.postValue(context.getString(R.string.prompt_mqtt_connection_error))
            }
        }
    }

    inner class MqttResponseCallback : MqttHelper.ResponseCallback {
        override fun onResponse(operationStatus: MqttHelper.OperationStatus, response: Boolean, message_code: Int) {
            Log.i(TAG, "ack callback: $operationStatus")
            when (operationStatus) {
                MqttHelper.OperationStatus.SUCCESS -> {
                    if (message_code != 0) {
                        val responseString = if (response) {
                            "OK"
                        } else {
                            "FAIL"
                        }
                        mMessage.postValue(responseString + ": " +MessageCodeHelper.get(message_code))
                    }
                }
                MqttHelper.OperationStatus.FAIL -> mMessage.postValue(context.getString(R.string.prompt_send_failed))
                MqttHelper.OperationStatus.TIMEOUT -> mMessage.postValue(context.getString(R.string.prompt_ack_timeout))
            }
            if (MqttHelper.isAckQueueEmpty()) {
                mIsLoading.postValue(false)
            }
        }
    }

    fun clearErrorMessage() {
        mMessage = MutableLiveData<String>()
    }

    fun mqttDisconnect() {
        mIsLoading.value = true
        MqttHelper.disconnect()
        mIsLoading.value = false
    }

    fun mqttConnect() {
        mIsLoading.value = true
        MqttHelper.connect(MqttConnectCallback())
    }

    fun mqttSendCommand(command: String) {
        if (command.isNotBlank()) {
            mIsLoading.value = true
            MqttHelper.sendCommand(command, MqttResponseCallback())
        } else {
            mMessage.postValue(context.getString(R.string.prompt_blank_command))
        }

    }

    fun mqttSubscribe(topic: String, callback: MqttHelper.SubscriptionCallback) {
        MqttHelper.subscribeTopic(topic, callback)
    }
}