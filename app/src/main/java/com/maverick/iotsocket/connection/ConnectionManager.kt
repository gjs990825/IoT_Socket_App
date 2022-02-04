package com.maverick.iotsocket.connection

object ConnectionManager {
    const val TOPIC_STATE = "IoT_Socket/State"
    const val TOPIC_COMMAND = "IoT_Socket/Command"

    private var connection: Connection = MqttConnection()

    fun getConnection() = connection

    fun switchConnection(connection: Connection) {
        this.connection = connection
    }
}