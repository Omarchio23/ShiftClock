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

    var swipeDirection: String
        get() = prefs.getString(KEY_SWIPE_DIRECTION, "RIGHT") ?: "RIGHT"
        set(value) = prefs.edit().putString(KEY_SWIPE_DIRECTION, value).apply()

    var bedExitEnabled: Boolean
        get() = prefs.getBoolean(KEY_BED_EXIT_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BED_EXIT_ENABLED, value).apply()

    var bedExitWindowMinutes: Int
        get() = prefs.getInt(KEY_BED_EXIT_WINDOW, 10)
        set(value) = prefs.edit().putInt(KEY_BED_EXIT_WINDOW, value).apply()

    var bedExitMotionTimeSeconds: Int
        get() = prefs.getInt(KEY_BED_EXIT_MOTION_TIME, 5)
        set(value) = prefs.edit().putInt(KEY_BED_EXIT_MOTION_TIME, value).apply()

    var bedExitSensitivity: Float
        get() = prefs.getFloat(KEY_BED_EXIT_SENSITIVITY, 2.0f)
        set(value) = prefs.edit().putFloat(KEY_BED_EXIT_SENSITIVITY, value).apply()

    companion object {
        private const val PREFS_NAME = "shift_clock_settings"
        private const val KEY_GLOBAL_VIBRATE = "global_vibrate_enabled"
        private const val KEY_GRADUAL_VOLUME = "gradual_volume_enabled"
        private const val KEY_SWIPE_DIRECTION = "swipe_direction"
        private const val KEY_BED_EXIT_ENABLED = "bed_exit_enabled"
        private const val KEY_BED_EXIT_WINDOW = "bed_exit_window"
        private const val KEY_BED_EXIT_MOTION_TIME = "bed_exit_motion_time"
        private const val KEY_BED_EXIT_SENSITIVITY = "bed_exit_sensitivity"
    }
}
