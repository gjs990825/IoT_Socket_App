package com.maverick.iotsocket

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import java.util.*

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        private val UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private val UUID_SERVICE = UUID.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2")
        private val UUID_CHARACTERISTIC = UUID.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f")
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        val config = BluetoothConfiguration()

        config.bluetoothServiceClass = BluetoothClassicService::class.java //  BluetoothClassicService.class or BluetoothLeService.class

        config.context = applicationContext
        config.bufferSize = 1024
        config.characterDelimiter = '\n'
        config.deviceName = "Bluetooth Sample"
        config.callListenersInMainThread = true

        //config.uuid = null; // When using BluetoothLeService.class set null to show all devices on scan.
        config.uuid = UUID_DEVICE // For Classic

        config.uuidService = UUID_SERVICE // For BLE
        config.uuidCharacteristic = UUID_CHARACTERISTIC // For BLE
        config.transport = BluetoothDevice.TRANSPORT_LE // Only for dual-mode devices

        // For BLE
//        config.connectionPriority = BluetoothGatt.CONNECTION_PRIORITY_HIGH // Automatically request connection priority just after connection is through.
        //or request connection priority manually, mService.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

        BluetoothService.init(config)
    }
}