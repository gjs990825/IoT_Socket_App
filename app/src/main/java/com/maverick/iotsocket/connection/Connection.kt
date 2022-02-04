package com.maverick.iotsocket.connection

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ConcurrentLinkedQueue

enum class OperationStatus {
    SUCCESS,
    FAIL,
    TIMEOUT,
    ERROR,
}

interface OperationResultCallback {
    fun onResult(operationStatus: OperationStatus)
}

interface ResponseCallback {
    fun onResponse(operationStatus: OperationStatus, message_code: Int = 0)
}

interface SubscriptionCallback {
    fun onMessage(message: String)
}

interface OnBusyStateChanged {
    fun onBusyStateChanged(state: Boolean)
}

abstract class Connection() {
    constructor(connection: Connection) : this() {
        this.subscribeCallbackMap = connection.subscribeCallbackMap
        this.connect(null)
        this.timeout = connection.timeout
        this.onBusyStateChanged = connection.onBusyStateChanged
        connection.disconnect()
    }

    private val TAG = "Connection"
    protected val topicAck = "IoT_Socket/Ack"
    private val topicCommand = "IoT_Socket/Command"
    private var subscribeCallbackMap = HashMap<String, Set<SubscriptionCallback>>()
    private val ackHandlerQueue = ConcurrentLinkedQueue<Pair<ResponseCallback, Long>>()
    protected val ackSubscriptionCallback = AckSubscriptionCallback()
    fun isBusy(): Boolean = isBusy
    var timeout = 2000L
    var onBusyStateChanged: OnBusyStateChanged? = null


    private var isBusy = false
        set(value) {
            field = value
            onBusyStateChanged?.onBusyStateChanged(field)
        }

    private fun ackHandlerQueueAdd(callback: ResponseCallback) {
        isBusy = true
        ackHandlerQueue.add(Pair(callback, System.currentTimeMillis()))

    }

    private fun ackHandlerQueueRemove(): ResponseCallback? {
        return if (!isAckQueueEmpty()) {
            try {
                ackHandlerQueue.remove().first
            } catch (e: NoSuchElementException) {
                Log.w(TAG, "ack queue changed")
                null
            } finally {
                if (isAckQueueEmpty()) {
                    isBusy = false
                }
            }
        } else {
            isBusy = false
            null
        }
    }

    private fun ackHandlerQueueRemoveTimeout() {
        with(ackHandlerQueue.peek()) {
            if (this != null) {
                try {
                    if (second + timeout < System.currentTimeMillis()) {
                        ackHandlerQueue.remove()
                        first.onResponse(OperationStatus.TIMEOUT)
                        if (isAckQueueEmpty()) {
                            isBusy = false
                        }
                    }
                } catch (e: NoSuchElementException) {
                    Log.w(TAG, "ack queue changed")
                }
            }
        }
    }

    abstract fun connect(callback: OperationResultCallback?)
    abstract fun disconnect()

    private fun isAckQueueEmpty() = ackHandlerQueue.isEmpty()


    fun sendCommand(command: String, callback: ResponseCallback) {
        if (command.isEmpty()) {
            callback.onResponse(OperationStatus.ERROR)
            return
        }
        send(topicCommand, command, callback)
        ackHandlerQueueAdd(callback)
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                ackHandlerQueueRemoveTimeout()
            }, timeout)
        }
    }

    inner class AckSubscriptionCallback : SubscriptionCallback {
        override fun onMessage(message: String) {
            if (!isAckQueueEmpty()) {
                try {
                    with(JSONObject(message)) {
                        val msg = getInt("message_code")
                        ackHandlerQueueRemove()?.onResponse(OperationStatus.SUCCESS, msg)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "parseMessage: failed")
                    ackHandlerQueueRemove()?.onResponse(OperationStatus.FAIL)
                } catch (e: NoSuchElementException) {
                    Log.w(TAG, "ack queue changed")
                }
            }
        }
    }

    // Incoming message goes here
    fun onIncomingMessage(message: String, topic: String) {
        val callback = subscribeCallbackMap[topic]
        callback?.forEach {
            it.onMessage(message)
        } ?: Log.w(TAG, "unknown topic:$topic, message:$message")
    }

    protected abstract fun send(topic: String, content: String, callback: ResponseCallback)
    protected abstract fun subscribe(topic: String)
    protected abstract fun unsubscribe(topic: String)

    fun subscribeTopic(topic: String, callback: SubscriptionCallback) {
        var callbackSet = subscribeCallbackMap[topic]
        if (callbackSet == null) {
            callbackSet = setOf(callback)
        } else {
            callbackSet.plus(callback)
        }
        subscribeCallbackMap[topic] = callbackSet
        subscribe(topic)
    }

    fun unsubscribeTopic(topic: String, callback: SubscriptionCallback) {
        val callbackSet = subscribeCallbackMap[topic]
        if (callbackSet != null) {
            when (callbackSet.size) {
                0 -> subscribeCallbackMap.remove(topic)
                1 -> {
                    subscribeCallbackMap.remove(topic)
                    unsubscribe(topic)
                }
                else -> {
                    callbackSet.minus(callback)
                    subscribeCallbackMap[topic] = callbackSet
                }
            }
        }
        Log.i(TAG, "unsubscribeTopic: $topic")
    }

    abstract fun isAvailable(): Boolean
}