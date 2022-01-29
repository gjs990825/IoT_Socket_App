package com.maverick.iotsocket.model

import com.maverick.iotsocket.R


data class ControlCenterUIData(val startDate: Date = Date.getDefault(), val startTime: Time = Time.getDefault(),
                               val endDate: Date = Date.getDefault(), val endTime: Time = Time.getDefault(),
                               val radioButtonCheckedId: Int = -1)
