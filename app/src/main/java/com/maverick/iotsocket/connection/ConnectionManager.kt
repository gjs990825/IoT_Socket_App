package com.maverick.iotsocket.connection

import android.bluetooth.BluetoothDevice

object ConnectionManager {
    const val TOPIC_STATE = "IoT_Socket/State"
    const val TOPIC_COMMAND = "IoT_Socket/Command"

    private var bluetoothDevice: BluetoothDevice? = null
    private var connection: Connection = MqttConnection()

    fun getConnection() = connection

    fun changeBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice
    }

    fun switchToBluetooth(): Boolean {
        return if (bluetoothDevice != null) {
            this.connection = BluetoothConnection(bluetoothDevice!!, this.connection)
            true
        } else {
            false
        }
    }

    fun switchToMqtt(): Boolean {
        this.connection = MqttConnection(this.connection)
        return true
    }
}