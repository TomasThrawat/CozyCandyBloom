package com.cozy.candybloom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LevelAdapter
    private lateinit var coinsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        coinsText = findViewById(R.id.coinsText)
        recyclerView = findViewById(R.id.levelRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = LevelAdapter(emptyList()) { level ->
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(GameActivity.EXTRA_LEVEL, level.number)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.shopButton).setOnClickListener {
            startActivity(Intent(this, ShopActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(LevelProgress.PREFS_NAME, MODE_PRIVATE)
        val unlocked = LevelProgress.unlockedLevel(prefs)
        adapter.submitLevels(Levels.all, unlocked)
        coinsText.text = getString(R.string.coins_format, LevelProgress.coins(prefs))
    }
}
