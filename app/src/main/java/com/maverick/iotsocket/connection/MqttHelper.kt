package com.maverick.iotsocket.connection

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

object MqttHelper {
    const val TAG = "MqttHelper"
    private const val host = "120.24.84.40"
    private const val clientId = "IoT_Socket Android App"
    private const val userName = "maverick"
    private const val password = "password"
    const val topicState = "IoT_Socket/State"
    private const val topicAck = "IoT_Socket/Ack"
    private const val topicCommand = "IoT_Socket/Command"
    private const val RESPONSE_TIMEOUT: Long = 3000

    enum class OperationStatus {
        SUCCESS,
        FAIL,
        TIMEOUT
    }

    private val subscribeCallbackMap = HashMap<String, Set<SubscriptionCallback>>()

    private val ackHandlerQueue = ConcurrentLinkedQueue<ResponseCallback>()


    private val client: Mqtt5AsyncClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier(clientId)
        .serverHost(host)
        .serverPort(1883)
        .buildAsync()

    interface OperationResultCallback {
        fun onResult(operationStatus: OperationStatus)
    }

    interface ResponseCallback {
        fun onResponse(operationStatus: OperationStatus, response: Boolean = false, message_code: Int = 0)
    }

    interface SubscriptionCallback {
        fun onMessage(message: String)
    }

    private object AckSubscriptionCallback : SubscriptionCallback {
        override fun onMessage(message: String) {
            if (!ackHandlerQueue.isEmpty()) {
                try {
                    with(JSONObject(message)) {
                        val response = getString("acknowledgement").equals("OK", true)
                        val msg = getInt("message_code")
                        ackHandlerQueue.remove().onResponse(OperationStatus.SUCCESS, response, msg)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "parseMessage: failed")
                    ackHandlerQueue.remove().onResponse(OperationStatus.SUCCESS, false, 0)
                }

            }
        }
    }

    private class MqttSubscriptionsCallback : Consumer<Mqtt5Publish> {
        override fun accept(mqtt3Publish: Mqtt5Publish) {
            with(mqtt3Publish) {
                val message = String(payloadAsBytes)
                Log.v(TAG, "received: $topic:$message")

                val callback = subscribeCallbackMap[topic.toString()]
                callback?.forEach {
                    it.onMessage(message)
                } ?: Log.w(TAG, "unknown topic")
            }
        }
    }

    fun subscribeTopic(topic: String, callback: SubscriptionCallback) {
        client.subscribeWith()
            .topicFilter(topic)
            .callback(MqttSubscriptionsCallback())
            .send()
            .whenCompleteAsync { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "$topic subscribe failed")
                    throwable.printStackTrace()
                } else {
                    Log.i(TAG, "$topic subscribe succeeded")
                    var callbackSet = subscribeCallbackMap[topic]
                    if (callbackSet == null) {
                        callbackSet = setOf(callback)
                    } else {
                        callbackSet.plus(callback)
                    }
                    subscribeCallbackMap[topic] = callbackSet
                }
            }
    }

    fun unsubscribeTopic(topic: String, callback: SubscriptionCallback) {
        val callbackSet = subscribeCallbackMap[topic]
        if (callbackSet != null) {
            when (callbackSet.size) {
                0 -> subscribeCallbackMap.remove(topic)
                1 -> {
                    subscribeCallbackMap.remove(topic)
                    client.unsubscribeWith()
                        .topicFilter(topic)
                        .send()
                }
                else -> {
                    callbackSet.minus(callback)
                    subscribeCallbackMap[topic] = callbackSet
                }
            }
        }
        Log.i(TAG, "unsubscribeTopic: $topic")
    }

    private fun connectWithDefaultConfig() {
        Log.i(TAG, "connectWithDefaultConfig")
        client.connectWith()
            .simpleAuth()
            .username(userName)
            .password(password.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "connect failed")
                    throwable.printStackTrace()
                } else {
                    Log.i(TAG, "connect success")
                    subscribeTopic(topicAck, AckSubscriptionCallback)
                }
            }
    }

    fun connect(callback: OperationResultCallback) {
        client.connectWith()
            .simpleAuth()
            .username(userName)
            .password(password.toByteArray())
            .applySimpleAuth()
            .send()
            .whenCompleteAsync { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "connect failed")
                    throwable.printStackTrace()
                } else {
                    Log.i(TAG, "connect success")
                    subscribeTopic(topicAck, AckSubscriptionCallback)
                }

                val status = if (throwable == null) {
                    OperationStatus.SUCCESS
                } else {
                    OperationStatus.FAIL
                }
                callback.onResult(status)
            }
    }

    fun disconnect() {
        client.disconnect()
        Log.i(TAG, "disconnect")
    }

    fun sendCommand(command: String, callback: ResponseCallback) {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                if (!ackHandlerQueue.isEmpty()) {
                    ackHandlerQueue.remove().onResponse(OperationStatus.TIMEOUT)
                }
            }, RESPONSE_TIMEOUT)
        }
        if (!isConnected()) {
            connectWithDefaultConfig()
        }
        client.publishWith()
            .topic(topicCommand)
            .payload(command.toByteArray())
            .send()
            .whenCompleteAsync { _, u ->
                run {
                    if (u != null) {
                        callback.onResponse(OperationStatus.FAIL)
                    } else {
                        ackHandlerQueue.add(callback)
                    }
                }
            }
    }

    private fun isConnected(): Boolean = client.state.isConnected

    fun isAckQueueEmpty(): Boolean {
        return ackHandlerQueue.isEmpty()
    }
}