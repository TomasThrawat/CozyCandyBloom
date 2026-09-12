package com.cozy.candybloom

import android.graphics.Color

/**
 * A cosmetic border ("frame") drawn around every candy tile. Purely visual and
 * persisted like the skin choice: free to switch any time from Settings, with
 * no limit on how often or how long any frame can be used.
 */
data class CandyFrame(val id: String, val displayName: String, val colors: List<Int>?)

object CandyFrames {

    /** The original plain look — no border drawn around the tile. */
    val NORMAL = CandyFrame(id = "normal", displayName = "Normal (No Frame)", colors = null)

    val RAINBOW = CandyFrame(
        id = "rainbow",
        displayName = "Rainbow",
        colors = listOf(
            Color.parseColor("#FF5252"), Color.parseColor("#FFB74D"),
            Color.parseColor("#FFF176"), Color.parseColor("#81C784"),
            Color.parseColor("#64B5F6"), Color.parseColor("#BA68C8"),
            Color.parseColor("#FF5252")
        )
    )

    val GOLD_SHINE = CandyFrame(
        id = "gold_shine",
        displayName = "Gold Shine",
        colors = listOf(
            Color.parseColor("#FFF9C4"), Color.parseColor("#FFB300"), Color.parseColor("#FFF9C4")
        )
    )

    val NEON_POP = CandyFrame(
        id = "neon_pop",
        displayName = "Neon Pop",
        colors = listOf(
            Color.parseColor("#00E5FF"), Color.parseColor("#FF4081"),
            Color.parseColor("#76FF03"), Color.parseColor("#00E5FF")
        )
    )

    val CANDY_STRIPE = CandyFrame(
        id = "candy_stripe",
        displayName = "Candy Stripe",
        colors = listOf(
            Color.parseColor("#F44336"), Color.parseColor("#FFFFFF"), Color.parseColor("#F44336")
        )
    )

    val BUBBLEGUM = CandyFrame(
        id = "bubblegum",
        displayName = "Bubblegum Pop",
        colors = listOf(
            Color.parseColor("#F8BBD0"), Color.parseColor("#CE93D8"), Color.parseColor("#F8BBD0")
        )
    )

    val SUNSET_GLOW = CandyFrame(
        id = "sunset_glow",
        displayName = "Sunset Glow",
        colors = listOf(
            Color.parseColor("#FF8A65"), Color.parseColor("#FFB74D"),
            Color.parseColor("#F06292"), Color.parseColor("#BA68C8"),
            Color.parseColor("#FF8A65")
        )
    )

    val GALAXY = CandyFrame(
        id = "galaxy",
        displayName = "Galaxy",
        colors = listOf(
            Color.parseColor("#5C6BC0"), Color.parseColor("#AB47BC"),
            Color.parseColor("#26C6DA"), Color.parseColor("#5C6BC0")
        )
    )

    /** No fixed cap — add more frames here any time; every one is freely selectable in Settings. */
    val all: List<CandyFrame> = listOf(NORMAL, RAINBOW, GOLD_SHINE, NEON_POP, CANDY_STRIPE, BUBBLEGUM, SUNSET_GLOW, GALAXY)

    fun byId(id: String): CandyFrame = all.firstOrNull { it.id == id } ?: NORMAL
}
