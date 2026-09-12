package com.cozy.candybloom

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences(SettingsPrefs.PREFS_NAME, MODE_PRIVATE)
        val skinGroup = findViewById<RadioGroup>(R.id.skinGroup)
        val frameGroup = findViewById<RadioGroup>(R.id.frameGroup)
        val fpsGroup = findViewById<RadioGroup>(R.id.fpsGroup)

        val currentSkinId = SettingsPrefs.skinId(prefs)
        for (skin in CandySkins.all) {
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = skin.displayName
                textSize = 16f
                isChecked = skin.id == currentSkinId
                tag = skin.id
            }
            skinGroup.addView(radio)
        }
        skinGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio = group.findViewById<RadioButton>(checkedId)
            val skinId = radio?.tag as? String ?: return@setOnCheckedChangeListener
            SettingsPrefs.setSkinId(prefs, skinId)
        }

        // Tile frame: purely cosmetic, unlimited — switch freely any time, no cap on use.
        val currentFrameId = SettingsPrefs.frameId(prefs)
        for (frame in CandyFrames.all) {
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = frame.displayName
                textSize = 16f
                isChecked = frame.id == currentFrameId
                tag = frame.id
            }
            frameGroup.addView(radio)
        }
        frameGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio = group.findViewById<RadioButton>(checkedId)
            val frameId = radio?.tag as? String ?: return@setOnCheckedChangeListener
            SettingsPrefs.setFrameId(prefs, frameId)
        }

        val currentFps = SettingsPrefs.targetFps(prefs)
        val fps60 = findViewById<RadioButton>(R.id.fps60)
        val fps90 = findViewById<RadioButton>(R.id.fps90)
        if (currentFps >= 90) fps90.isChecked = true else fps60.isChecked = true

        fpsGroup.setOnCheckedChangeListener { _, checkedId ->
            val fps = if (checkedId == R.id.fps90) 90 else 60
            SettingsPrefs.setTargetFps(prefs, fps)
        }
    }
}
