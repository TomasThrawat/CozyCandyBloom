package com.cozy.candybloom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    companion object { const val EXTRA_LEVEL = "extra_level" }
    private var resultShown = false
    private var selectedBooster: BoosterType? = null

    private lateinit var boosterPrefs: SharedPreferences
    private lateinit var rocketButton: FrameLayout
    private lateinit var bombButton: FrameLayout
    private lateinit var paddleButton: FrameLayout
    private lateinit var rocketBadgeText: TextView
    private lateinit var bombBadgeText: TextView
    private lateinit var paddleBadgeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val levelNumber = intent.getIntExtra(EXTRA_LEVEL, 1)
        val level = Levels.byNumber(levelNumber)
        val levelText = findViewById<TextView>(R.id.levelText)
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val coinsText = findViewById<TextView>(R.id.coinsText)
        val targetText = findViewById<TextView>(R.id.targetText)
        val powerUpText = findViewById<TextView>(R.id.powerUpText)
        val gameView = findViewById<GameView>(R.id.gameView)
        rocketButton = findViewById(R.id.rocketButton)
        bombButton = findViewById(R.id.bombButton)
        paddleButton = findViewById(R.id.paddleButton)
        rocketBadgeText = findViewById(R.id.rocketBadgeText)
        bombBadgeText = findViewById(R.id.bombBadgeText)
        paddleBadgeText = findViewById(R.id.paddleBadgeText)
        val prefs = getSharedPreferences(LevelProgress.PREFS_NAME, MODE_PRIVATE)
        boosterPrefs = getSharedPreferences(BoosterInventory.PREFS_NAME, MODE_PRIVATE)

        levelText.text = getString(R.string.level_button_format, level.number)
        targetText.text = getString(R.string.target_format, level.targetScore)
        scoreText.text = getString(R.string.score_format, 0)
        coinsText.text = getString(R.string.coins_format, LevelProgress.coins(prefs))
        powerUpText.text = getString(R.string.powerup_off)
        refreshBoosterButtons()

        val settingsPrefs = getSharedPreferences(SettingsPrefs.PREFS_NAME, MODE_PRIVATE)
        gameView.applySkin(CandySkins.byId(SettingsPrefs.skinId(settingsPrefs)))
        gameView.applyFrame(CandyFrames.byId(SettingsPrefs.frameId(settingsPrefs)))
        gameView.applyTargetFps(SettingsPrefs.targetFps(settingsPrefs))
        gameView.onScoreChanged = { score -> runOnUiThread { scoreText.text = getString(R.string.score_format, score) } }
        gameView.onLevelResult = { score -> runOnUiThread { showResult(level, score, prefs, coinsText) } }
        gameView.onPowerUpChanged = { powerUp -> runOnUiThread {
            powerUpText.text = when (powerUp) {
                GameView.PowerUp.ROCKET -> getString(R.string.powerup_rocket)
                GameView.PowerUp.BOMB -> getString(R.string.powerup_bomb)
                GameView.PowerUp.PADDLE -> getString(R.string.powerup_paddle)
                GameView.PowerUp.NONE -> getString(R.string.powerup_off)
            }
            selectedBooster = when (powerUp) {
                GameView.PowerUp.ROCKET -> BoosterType.ROCKET
                GameView.PowerUp.BOMB -> BoosterType.BOMB
                GameView.PowerUp.PADDLE -> BoosterType.PADDLE
                GameView.PowerUp.NONE -> null
            }
            updateBoosterHighlight()
        } }
        gameView.onBoosterConsumed = { used ->
            val type = when (used) {
                GameView.PowerUp.ROCKET -> BoosterType.ROCKET
                GameView.PowerUp.BOMB -> BoosterType.BOMB
                GameView.PowerUp.PADDLE -> BoosterType.PADDLE
                GameView.PowerUp.NONE -> null
            }
            if (type != null) {
                BoosterInventory.consume(boosterPrefs, type)
                runOnUiThread { refreshBoosterButtons() }
            }
        }

        rocketButton.setOnClickListener {
            if (BoosterInventory.count(boosterPrefs, BoosterType.ROCKET) <= 0) return@setOnClickListener
            gameView.selectPowerUp(if (selectedBooster == BoosterType.ROCKET) GameView.PowerUp.NONE else GameView.PowerUp.ROCKET)
        }
        bombButton.setOnClickListener {
            if (BoosterInventory.count(boosterPrefs, BoosterType.BOMB) <= 0) return@setOnClickListener
            gameView.selectPowerUp(if (selectedBooster == BoosterType.BOMB) GameView.PowerUp.NONE else GameView.PowerUp.BOMB)
        }
        paddleButton.setOnClickListener {
            if (BoosterInventory.count(boosterPrefs, BoosterType.PADDLE) <= 0) return@setOnClickListener
            gameView.selectPowerUp(if (selectedBooster == BoosterType.PADDLE) GameView.PowerUp.NONE else GameView.PowerUp.PADDLE)
        }

        gameView.configureLevel(level.targetScore)
    }

    override fun onResume() {
        super.onResume()
        refreshBoosterButtons()
    }

    /** Booster buttons show how many of each the player currently owns, and disable at zero. */
    private fun refreshBoosterButtons() {
        val rocketCount = BoosterInventory.count(boosterPrefs, BoosterType.ROCKET)
        val bombCount = BoosterInventory.count(boosterPrefs, BoosterType.BOMB)
        val paddleCount = BoosterInventory.count(boosterPrefs, BoosterType.PADDLE)

        rocketBadgeText.text = getString(R.string.booster_count_format, rocketCount)
        bombBadgeText.text = getString(R.string.booster_count_format, bombCount)
        paddleBadgeText.text = getString(R.string.booster_count_format, paddleCount)

        rocketButton.isEnabled = rocketCount > 0
        bombButton.isEnabled = bombCount > 0
        paddleButton.isEnabled = paddleCount > 0

        rocketButton.alpha = if (rocketCount > 0) 1f else 0.4f
        bombButton.alpha = if (bombCount > 0) 1f else 0.4f
        paddleButton.alpha = if (paddleCount > 0) 1f else 0.4f
    }

    private fun updateBoosterHighlight() {
        highlightButton(rocketButton, selectedBooster == BoosterType.ROCKET)
        highlightButton(bombButton, selectedBooster == BoosterType.BOMB)
        highlightButton(paddleButton, selectedBooster == BoosterType.PADDLE)
    }

    /** Static, motion-free selection highlight — no scale/animate so the icon never jumps or vibrates. */
    private fun highlightButton(button: FrameLayout, selected: Boolean) {
        button.elevation = if (selected) 10f * resources.displayMetrics.density else 0f
    }

    private fun showResult(level: Level, score: Int, prefs: SharedPreferences, coinsText: TextView) {
        if (resultShown) return
        resultShown = true
        val hasNext = level.number < Levels.all.size
        LevelProgress.unlockNext(prefs, level.number)
        val earned = LevelProgress.addCoinsForScore(prefs, score)
        coinsText.text = getString(R.string.coins_format, LevelProgress.coins(prefs))
        val message = getString(R.string.level_complete_message) + "\n" + getString(R.string.level_complete_coins_message, earned)
        val builder = AlertDialog.Builder(this).setTitle(getString(R.string.level_complete_title)).setMessage(message).setCancelable(false)
        if (hasNext) builder.setPositiveButton(R.string.next_level) { _, _ ->
            startActivity(Intent(this, GameActivity::class.java).putExtra(EXTRA_LEVEL, level.number + 1)); finish()
        }
        builder.setNegativeButton(R.string.back_to_levels) { _, _ -> finish() }
        builder.show()
    }
}
