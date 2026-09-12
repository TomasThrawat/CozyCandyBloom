package com.cozy.candybloom

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(
    private var levels: List<Level>,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    private var unlockedLevel: Int = 1

    fun submitLevels(newLevels: List<Level>, unlocked: Int) {
        levels = newLevels
        unlockedLevel = unlocked
        notifyDataSetChanged()
    }

    class LevelViewHolder(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val density = parent.resources.displayMetrics.density
        val text = TextView(parent.context).apply {
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(Color.parseColor("#5D4037"))
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (56 * density).toInt()
            ).apply {
                setMargins((6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt())
            }
        }
        return LevelViewHolder(text)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]
        val isUnlocked = level.number <= unlockedLevel
        holder.text.text = level.number.toString()
        holder.text.setBackgroundColor(Color.parseColor(if (isUnlocked) "#FFB74D" else "#D9CFC2"))
        holder.text.setTextColor(Color.parseColor(if (isUnlocked) "#5D4037" else "#8A8078"))
        holder.text.isEnabled = isUnlocked
        holder.text.setOnClickListener {
            if (isUnlocked) onLevelClick(level)
        }
    }

    override fun getItemCount(): Int = levels.size
}
