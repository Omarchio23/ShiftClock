package es.aipp.shiftclock.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isGlobalVibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_GLOBAL_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_GLOBAL_VIBRATE, value).apply()

    var isGradualVolumeEnabled: Boolean
        get() = prefs.getBoolean(KEY_GRADUAL_VOLUME, false)
        set(value) = prefs.edit().putBoolean(KEY_GRADUAL_VOLUME, value).apply()

    companion object {
        private const val PREFS_NAME = "shift_clock_settings"
        private const val KEY_GLOBAL_VIBRATE = "global_vibrate_enabled"
        private const val KEY_GRADUAL_VOLUME = "gradual_volume_enabled"
    }
}
