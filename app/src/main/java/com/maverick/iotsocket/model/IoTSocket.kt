package com.maverick.iotsocket.model

data class IoTSocket(val sensor: Sensor = Sensor(-1f, -1f, -1f),
                     val peripheral: Peripheral = Peripheral(false, false, false, 0),
                     val systemInfo: SystemInfo = SystemInfo(0, -1f))