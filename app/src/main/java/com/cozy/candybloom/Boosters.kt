package com.cozy.candybloom

import android.content.SharedPreferences

/** The purchasable power-ups sold in the Shop and used from the in-game power-up bar. */
enum class BoosterType(val id: String, val emoji: String, val price: Int) {
    ROCKET("rocket", "\uD83D\uDE80", 60),
    BOMB("bomb", "\uD83D\uDCA3", 90),
    PADDLE("paddle", "\uD83C\uDFD3", 40);

    companion object {
        fun byId(id: String): BoosterType? = entries.firstOrNull { it.id == id }
    }
}

/** Tracks how many of each booster the player owns, persisted across launches. */
object BoosterInventory {
    const val PREFS_NAME = "candy_cozy_boosters"

    private fun key(type: BoosterType) = "count_${type.id}"

    fun count(prefs: SharedPreferences, type: BoosterType): Int = prefs.getInt(key(type), 0)

    fun add(prefs: SharedPreferences, type: BoosterType, amount: Int = 1) {
        prefs.edit().putInt(key(type), count(prefs, type) + amount).apply()
    }

    /** Consumes one unit if available; returns true if it was consumed. */
    fun consume(prefs: SharedPreferences, type: BoosterType): Boolean {
        val current = count(prefs, type)
        if (current <= 0) return false
        prefs.edit().putInt(key(type), current - 1).apply()
        return true
    }
}
