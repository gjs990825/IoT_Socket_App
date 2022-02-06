package com.maverick.iotsocket.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverick.iotsocket.connection.ConnectionManager
import com.maverick.iotsocket.connection.SubscriptionCallback
import com.maverick.iotsocket.model.IoTSocket
import com.maverick.iotsocket.model.Peripheral
import com.maverick.iotsocket.model.Sensor
import com.maverick.iotsocket.model.SystemInfo
import org.json.JSONException
import org.json.JSONObject


class HomeViewModel(ioTSocket: IoTSocket?) : ViewModel() {
    private val TAG = "HomeViewModel"
    private val mPeripheral = MutableLiveData<Peripheral>()
    private val mSensor = MutableLiveData<Sensor>()
    private val mSystemInfo = MutableLiveData<SystemInfo>()
    private val connection = ConnectionManager.getConnection()
    private val topicStateCallback = TopicStateCallback()

    val wifiSSID = MutableLiveData("")
    val wifiPassword = MutableLiveData("")
    val wifiSettingValid = MutableLiveData(false)

    fun updateWifiSettingValid() {
        val ssid = wifiSSID.value
        val password = wifiPassword.value
        wifiSettingValid.postValue(
            ssid != null &&
                    password != null &&
                    ssid.isNotBlank()
                    && (password.isEmpty() || password.length >= 8)
        )
    }

    fun getWifiSettingCommand(): String {
        val ssid = wifiSSID.value
        val password = wifiPassword.value
        return if (
            ssid != null &&
            password != null &&
            ssid.isNotBlank()
            && (password.isEmpty() || password.length >= 8)
        ) {
            "settings wifi \"$ssid\" \"$password\""
        } else {
            ""
        }
    }

    fun getTimeSettingCommand(): String {
        return "settings time ${System.currentTimeMillis() / 1000}"
    }


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
                            getBoolean("led"),
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

    private inner class TopicStateCallback : SubscriptionCallback {
        override fun onMessage(message: String) {
            parseStateMessage(message)
        }
    }

    fun subscribeTopicState() {
        connection.subscribeTopic(ConnectionManager.TOPIC_STATE, topicStateCallback)
    }

    fun unsubscribeTopicState() {
        connection.unsubscribeTopic(ConnectionManager.TOPIC_STATE, topicStateCallback)
    }
}