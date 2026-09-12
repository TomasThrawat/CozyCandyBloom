package com.cozy.candybloom

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Lets the player spend coins earned from levels on Rocket / Bomb / Paddle boosters. */
class ShopActivity : AppCompatActivity() {

    private lateinit var progressPrefs: SharedPreferences
    private lateinit var boosterPrefs: SharedPreferences

    private lateinit var coinsText: TextView
    private lateinit var rocketOwnedText: TextView
    private lateinit var bombOwnedText: TextView
    private lateinit var paddleOwnedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        progressPrefs = getSharedPreferences(LevelProgress.PREFS_NAME, MODE_PRIVATE)
        boosterPrefs = getSharedPreferences(BoosterInventory.PREFS_NAME, MODE_PRIVATE)

        coinsText = findViewById(R.id.shopCoinsText)
        rocketOwnedText = findViewById(R.id.rocketOwnedText)
        bombOwnedText = findViewById(R.id.bombOwnedText)
        paddleOwnedText = findViewById(R.id.paddleOwnedText)

        findViewById<TextView>(R.id.rocketPriceText).text = getString(R.string.price_format, BoosterType.ROCKET.price)
        findViewById<TextView>(R.id.bombPriceText).text = getString(R.string.price_format, BoosterType.BOMB.price)
        findViewById<TextView>(R.id.paddlePriceText).text = getString(R.string.price_format, BoosterType.PADDLE.price)

        findViewById<Button>(R.id.buyRocketButton).setOnClickListener { buy(BoosterType.ROCKET) }
        findViewById<Button>(R.id.buyBombButton).setOnClickListener { buy(BoosterType.BOMB) }
        findViewById<Button>(R.id.buyPaddleButton).setOnClickListener { buy(BoosterType.PADDLE) }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buy(type: BoosterType) {
        if (LevelProgress.spendCoins(progressPrefs, type.price)) {
            BoosterInventory.add(boosterPrefs, type)
            refresh()
        } else {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show()
        }
    }

    private fun refresh() {
        coinsText.text = getString(R.string.coins_format, LevelProgress.coins(progressPrefs))
        rocketOwnedText.text = getString(R.string.owned_format, BoosterInventory.count(boosterPrefs, BoosterType.ROCKET))
        bombOwnedText.text = getString(R.string.owned_format, BoosterInventory.count(boosterPrefs, BoosterType.BOMB))
        paddleOwnedText.text = getString(R.string.owned_format, BoosterInventory.count(boosterPrefs, BoosterType.PADDLE))
    }
}
