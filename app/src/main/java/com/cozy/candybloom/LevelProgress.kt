package com.cozy.candybloom

import android.content.SharedPreferences

/** Tracks unlocked levels and coins, persisted across launches. */
object LevelProgress {
    const val PREFS_NAME = "candy_cozy_progress"
    private const val KEY_UNLOCKED = "unlocked_level"
    private const val KEY_COINS = "coins"

    fun unlockedLevel(prefs: SharedPreferences): Int =
        prefs.getInt(KEY_UNLOCKED, 1)

    fun coins(prefs: SharedPreferences): Int = prefs.getInt(KEY_COINS, 0)

    /** Coins are earned directly from the score achieved in a completed level. */
    fun coinsForScore(score: Int): Int = (score / 100).coerceAtLeast(1)

    fun addCoinsForScore(prefs: SharedPreferences, score: Int): Int {
        val earned = coinsForScore(score)
        prefs.edit().putInt(KEY_COINS, coins(prefs) + earned).apply()
        return earned
    }

    /** Spends coins on a Shop purchase; returns true and deducts the balance if it's sufficient. */
    fun spendCoins(prefs: SharedPreferences, amount: Int): Boolean {
        val current = coins(prefs)
        if (current < amount) return false
        prefs.edit().putInt(KEY_COINS, current - amount).apply()
        return true
    }

    fun unlockNext(prefs: SharedPreferences, completedLevel: Int) {
        val current = unlockedLevel(prefs)
        val next = completedLevel + 1
        if (next > current) prefs.edit().putInt(KEY_UNLOCKED, next).apply()
    }
}
