package com.cozy.candybloom

import android.content.SharedPreferences

/** Persists the player's chosen candy skin, tile frame, and target frame rate. */
object SettingsPrefs {
    const val PREFS_NAME = "candy_cozy_settings"
    private const val KEY_SKIN = "skin_id"
    private const val KEY_FRAME = "frame_id"
    private const val KEY_FPS = "target_fps"

    fun skinId(prefs: SharedPreferences): String =
        prefs.getString(KEY_SKIN, CandySkins.COZY_BLOOM.id) ?: CandySkins.COZY_BLOOM.id

    fun setSkinId(prefs: SharedPreferences, id: String) {
        prefs.edit().putString(KEY_SKIN, id).apply()
    }

    /** No usage cap — this is a plain persisted preference, freely switchable at any time. */
    fun frameId(prefs: SharedPreferences): String =
        prefs.getString(KEY_FRAME, CandyFrames.NORMAL.id) ?: CandyFrames.NORMAL.id

    fun setFrameId(prefs: SharedPreferences, id: String) {
        prefs.edit().putString(KEY_FRAME, id).apply()
    }

    fun targetFps(prefs: SharedPreferences): Int =
        prefs.getInt(KEY_FPS, 60)

    fun setTargetFps(prefs: SharedPreferences, fps: Int) {
        prefs.edit().putInt(KEY_FPS, fps).apply()
    }
}
