package com.maverick.iotsocket.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.MqttHelper
import com.maverick.iotsocket.model.IoTScoket
import com.maverick.iotsocket.model.Peripheral
import com.maverick.iotsocket.model.Sensor
import com.maverick.iotsocket.model.SystemInfo
import org.json.JSONException
import org.json.JSONObject


class HomeViewModel(ioTSocket: IoTScoket?) : ViewModel() {
    private val TAG = "HomeViewModel"
    private val mPeripheral = MutableLiveData<Peripheral>()
    private val mSensor = MutableLiveData<Sensor>()
    private val mSystemInfo = MutableLiveData<SystemInfo>()
    private val mqttTopicStateCallback = MqttTopicStateCallback()

    init {
        ioTSocket?.let {
            mPeripheral.value = it.peripheral
            mSensor.value = it.sensor
            mSystemInfo.value = it.systemInfo
        }
    }

    val peripheral: LiveData<Peripheral> get() = mPeripheral
    val sensor: LiveData<Sensor> get() = mSensor
    val systemInfo: LiveData<SystemInfo> get() = mSystemInfo

    fun parseStateMessage(message: String) {
        try {
            with(JSONObject(message)) {
                with(getJSONObject("peripheral")) {
                    mPeripheral.postValue(
                        Peripheral(
                            getBoolean("relay"),
                            getInt("led"),
                            getBoolean("beeper"),
                            getInt("motor")
                        )
                    )
                }
                with(getJSONObject("sensor")) {
                    mSensor.postValue(
                        Sensor(
                            getDouble("temperature").toFloat(),
                            getDouble("pressure").toFloat(),
                            getDouble("brightness").toFloat()
                        )
                    )
                }
                with(getJSONObject("system")) {
                    mSystemInfo.postValue(
                        SystemInfo(
                            getLong("time"),
                            getDouble("temperature").toFloat()
                        )
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "parseMessage: failed")
        }
    }

    private inner class MqttTopicStateCallback : MqttHelper.SubscriptionCallback {
        override fun onMessage(message: String) {
            parseStateMessage(message)
        }
    }

    fun mqttSubscribeTopicState() {
        MqttHelper.subscribeTopic(MqttHelper.topicState, mqttTopicStateCallback)
    }

    fun mqttUnsubscribeTopicState() {
        MqttHelper.unsubscribeTopic(MqttHelper.topicState, mqttTopicStateCallback)
    }
}