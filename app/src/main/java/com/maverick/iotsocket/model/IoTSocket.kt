package com.maverick.iotsocket.model

data class IoTSocket(val sensor: Sensor = Sensor(-1f, -1f, -1),
                     val peripheral: Peripheral = Peripheral(
                         relay = false,
                         led = false,
                         beeper = false,
                         motor = 0
                     ),
                     val systemInfo: SystemInfo = SystemInfo(0, -1f))