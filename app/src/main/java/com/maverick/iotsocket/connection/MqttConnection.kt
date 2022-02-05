package com.maverick.iotsocket.connection

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.util.function.Consumer

class MqttConnection(connection: Connection?): Connection(connection) {
    constructor(): this(null)
    private val TAG = "MqttConnection"

    private val host = "120.24.84.40"
    private val clientId = "IoT_Socket Android App"
    private val userName = "maverick"
    private val password = "password"
    private val client: Mqtt5AsyncClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier(clientId)
        .serverHost(host)
        .serverPort(1883)
        .buildAsync()
    private var subsList = ArrayList<String>()

    init {
        // check old subs, re-subs mqtt topic later when connect
        for (topic in subscribeCallbackMap.keys) {
            subsList.add(topic)
            Log.w(TAG, "topic: $topic copied")
        }
    }

    inner class MqttSubscriptionsCallback : Consumer<Mqtt5Publish> {
        override fun accept(mqtt3Publish: Mqtt5Publish) {
            with(mqtt3Publish) {
                val message = String(payloadAsBytes)
                Log.v(TAG, "mqtt received: $topic:$message")
                onIncomingMessage(message, topic.toString())
            }
        }
    }

    override fun connect(callback: OperationResultCallback?) {
        isBusy = true
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
                    subscribeTopic(topicAck, ackSubscriptionCallback)

                    // re-subs and clear
                    if (subsList.isNotEmpty()) {
                        for (topic in subsList) {
                            subscribe(topic)
                        }
                        subsList.clear()
                    }
                }

                val status = if (throwable == null) {
                    OperationStatus.SUCCESS
                } else {
                    OperationStatus.FAIL
                }
                callback?.onResult(status)
                isBusy = false
            }
    }

    override fun disconnect() {
        client.disconnect()
        Log.i(TAG, "disconnect")
    }

    override fun reconnect(callback: OperationResultCallback?) {
        connect(callback)
    }

    override fun send(topic: String, content: String, callback: ResponseCallback) {
        if (isAvailable()) {
            client.publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(content.toByteArray())
                .send()
                .whenCompleteAsync { _, u ->
                    run {
                        if (u != null) {
                            callback.onResponse(OperationStatus.FAIL)
                        }
                    }
                }
        }
    }

    override fun subscribe(topic: String) {
        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(MqttSubscriptionsCallback())
            .send()
            .whenCompleteAsync { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "$topic subscribe failed")
                    throwable.printStackTrace()
                } else {
                    Log.i(TAG, "$topic subscribe succeeded")
                }
            }
    }

    override fun unsubscribe(topic: String) {
        Log.i(TAG, "unsubscribe: $topic")
        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
    }

    override fun isAvailable(): Boolean = client.state.isConnected
}