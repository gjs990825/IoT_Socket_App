package com.maverick.iotsocket.util

import com.maverick.iotsocket.MyApplication.Companion.context
import com.maverick.iotsocket.R

object MessageCodeHelper {
    private val codes = context.resources.getStringArray(R.array.message_codes)
    fun get(codeId: Int): String {
        if (codeId >= 0 && codeId < codes.size) {
            return codes[codeId]
        }
        return ""
    }
}