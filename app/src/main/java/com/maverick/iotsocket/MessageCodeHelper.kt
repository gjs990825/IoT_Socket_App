package com.maverick.iotsocket

object MessageCodeHelper {
    private val codes = MyApplication.context.resources.getStringArray(R.array.message_codes)
    fun get(codeId: Int): String {
        if (codeId >= 0 && codeId < codes.size) {
            return codes[codeId]
        }
        return ""
    }
}