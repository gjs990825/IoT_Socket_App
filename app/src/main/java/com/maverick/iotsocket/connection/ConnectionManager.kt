package com.maverick.iotsocket.connection

import android.bluetooth.BluetoothDevice

object ConnectionManager {
    const val TOPIC_STATE = "IoT_Socket/State"
    const val TOPIC_COMMAND = "IoT_Socket/Command"

    private var typeChangeListener: ConnectionTypeChangeListener? = null
    private var mType = Type.MQTT
        set(value) {
            field = value
            typeChangeListener?.onTypeChange(field)
        }

    val type: Type get() = mType

    fun setTypeChangeListener(typeChangeListener: ConnectionTypeChangeListener) {
        this.typeChangeListener = typeChangeListener
    }

    enum class Type {
        BLUETOOTH,
        MQTT,
    }

    private var bluetoothDevice: BluetoothDevice? = null
    private var connection: Connection = MqttConnection()

    fun getConnection() = connection

    interface ConnectionTypeChangeListener {
        fun onTypeChange(type: Type)
    }

    fun changeBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice
    }

    fun bluetoothConnectionAvailable() = bluetoothDevice != null

    fun switchToBluetooth(): Boolean {
        return if (bluetoothDevice != null) {
            this.connection = BluetoothConnection(bluetoothDevice!!, this.connection)
            mType = Type.BLUETOOTH
            true
        } else {
            false
        }
    }

    fun switchToMqtt(): Boolean {
        this.connection = MqttConnection(this.connection)
        mType = Type.MQTT
        return true
    }
}