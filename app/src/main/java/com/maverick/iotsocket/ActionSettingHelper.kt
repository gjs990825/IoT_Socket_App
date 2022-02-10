package com.maverick.iotsocket

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object ActionSettingHelper {
    private val sharedPreferences: SharedPreferences =
        MyApplication.context.getSharedPreferences("action_settings_key", MODE_PRIVATE)

    var switchState: Boolean = false
        get() = sharedPreferences.getBoolean("switch_status", false)
        set(value) {
            sharedPreferences.edit().putBoolean("switch_status", value).apply()
            field = value
        }

//    init {
//        sharedPreferences.edit().clear().apply()
//    }

    var switchOnCommand: String = ""
        get() = sharedPreferences.getString("switch_on_command", null) ?: "relay true"
        set(value) {
            sharedPreferences.edit().putString("switch_on_command", value).apply()
            field = value
        }

    var switchOffCommand: String = ""
        get() = sharedPreferences.getString("switch_off_command", null) ?: "relay false"
        set(value) {
            sharedPreferences.edit().putString("switch_off_command", value).apply()
            field = value
        }

    var switchFlipCommand: String = ""
        get() = sharedPreferences.getString("switch_flip_command", null) ?: "flip relay"
        set(value) {
            sharedPreferences.edit().putString("switch_flip_command", value).apply()
            field = value
        }
}