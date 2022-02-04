package com.maverick.iotsocket.connection

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter

class BluetoothConnection(private val bluetoothDevice: BluetoothDevice, connection: Connection) :
    Connection(connection), BluetoothService.OnBluetoothEventCallback {
    private val TAG = "BluetoothConnection"
    private var operationResultCallback: OperationResultCallback? = null
    private var subsList = ArrayList<String>()

    private val service = BluetoothService.getDefaultInstance()
    private val writer = BluetoothWriter(service)

    init {
        for (topic in super.subscribeCallbackMap.keys) {
            subsList.add(topic)
            Log.w(TAG, "topic: $topic copied")
        }
        subscribeTopic(topicAck, ackSubscriptionCallback)
    }

    override fun connect(callback: OperationResultCallback?) {
        service.setOnEventCallback(this)
        service.connect(bluetoothDevice)
        operationResultCallback = callback

        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                if (!isAvailable()) {
                    callback?.onResult(OperationStatus.FAIL)
                }
            }, 3000)
        }
    }

    override fun disconnect() {
        service.disconnect()
//        service.setOnEventCallback(null)
    }

    override fun send(topic: String, content: String, callback: ResponseCallback) {
        if (isAvailable()) {
            writer.writeln(content)
        } else {
            callback.onResponse(OperationStatus.FAIL)
        }
    }

    override fun subscribe(topic: String) {
        subsList.add(topic)
    }

    override fun unsubscribe(topic: String) {
        subsList.remove(topic)
    }

    override fun isAvailable(): Boolean = service.status == BluetoothStatus.CONNECTED

    override fun onDataRead(buffer: ByteArray?, length: Int) {
        if (buffer != null && length > 0) {
            val rawMessage = String(buffer, 0, length)

            Log.d(TAG, "onDataRead: $rawMessage")
            val topic = when {
                rawMessage.startsWith(topicAck, false) -> topicAck
                rawMessage.startsWith(topicState, false) -> topicState
                else -> "unknown_topic"
            }
            val message = rawMessage.removePrefix(topic)
            if (subsList.contains(topic)) {
                onIncomingMessage(message, topic)
            } else {
                Log.w(TAG, "unsubscribed topic:$topic, message dropped:$message")
            }
        }
    }

    override fun onStatusChange(status: BluetoothStatus?) {
        if (status != null) {
            Log.i(TAG, "onStatusChange: $status")
            when (status) {
                BluetoothStatus.CONNECTED -> operationResultCallback?.onResult(OperationStatus.SUCCESS)

                else -> {} // do nothing
            }
        }
    }

    override fun onDeviceName(deviceName: String?) {
        Log.i(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String?) {
        Log.i(TAG, "onToast: $message")
    }

    override fun onDataWrite(buffer: ByteArray?) {
        if (buffer != null) {
            Log.i(TAG, "onDataWrite: ${String(buffer)}")
        }
    }
}