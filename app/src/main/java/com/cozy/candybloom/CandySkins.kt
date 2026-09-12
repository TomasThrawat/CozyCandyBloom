package com.cozy.candybloom

import android.graphics.Color

/** The look of one candy type: its emoji glyph and rounded-tile background color. */
data class CandyVisual(val emoji: String, val bgColor: Int, val iconRes: Int? = null)

/** A full set of visuals for all six candy types — a selectable "skin". */
data class CandySkin(val id: String, val displayName: String, val visuals: Map<CandyType, CandyVisual>)

object CandySkins {

    val COZY_BLOOM = CandySkin(
        id = "cozy_bloom",
        displayName = "Cozy Bloom (MCP)",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("", Color.parseColor("#FFE1E8"), R.drawable.candy_heart),
            CandyType.CHOCOLATE to CandyVisual("", Color.parseColor("#EFE1D3"), R.drawable.candy_chest),
            CandyType.LOLLIPOP to CandyVisual("", Color.parseColor("#FFF3D6"), R.drawable.candy_star),
            CandyType.CANDY to CandyVisual("", Color.parseColor("#F0E1FF"), R.drawable.candy_gem),
            CandyType.COOKIE to CandyVisual("", Color.parseColor("#DFF6E9"), R.drawable.candy_potion),
            CandyType.CUPCAKE to CandyVisual("", Color.parseColor("#FFE9D6"), R.drawable.candy_coin)
        )
    )

    val CLASSIC = CandySkin(
        id = "classic",
        displayName = "Candy Shop",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🍩", Color.parseColor("#F6C89A")),
            CandyType.CHOCOLATE to CandyVisual("🍫", Color.parseColor("#B99476")),
            CandyType.LOLLIPOP to CandyVisual("🍭", Color.parseColor("#CBB8EC")),
            CandyType.CANDY to CandyVisual("🍬", Color.parseColor("#F8B8CB")),
            CandyType.COOKIE to CandyVisual("🍪", Color.parseColor("#F6D673")),
            CandyType.CUPCAKE to CandyVisual("🧁", Color.parseColor("#A8E6C4"))
        )
    )

    val FRUITS = CandySkin(
        id = "fruits",
        displayName = "Fruit Salad",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🍎", Color.parseColor("#F6A9A0")),
            CandyType.CHOCOLATE to CandyVisual("🍇", Color.parseColor("#C6ACE0")),
            CandyType.LOLLIPOP to CandyVisual("🍊", Color.parseColor("#F9C583")),
            CandyType.CANDY to CandyVisual("🍓", Color.parseColor("#F7AEC0")),
            CandyType.COOKIE to CandyVisual("🍋", Color.parseColor("#F5E28C")),
            CandyType.CUPCAKE to CandyVisual("🍒", Color.parseColor("#F0A6AE"))
        )
    )

    val SHAPES = CandySkin(
        id = "shapes",
        displayName = "Bright Shapes",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🔴", Color.parseColor("#F6C2C2")),
            CandyType.CHOCOLATE to CandyVisual("🟠", Color.parseColor("#F8D9B8")),
            CandyType.LOLLIPOP to CandyVisual("🟡", Color.parseColor("#F7EFB8")),
            CandyType.CANDY to CandyVisual("🟢", Color.parseColor("#C4E9C4")),
            CandyType.COOKIE to CandyVisual("🔵", Color.parseColor("#BFDCF2")),
            CandyType.CUPCAKE to CandyVisual("🟣", Color.parseColor("#D9C4EE"))
        )
    )

    val BAKERY = CandySkin(
        id = "bakery",
        displayName = "Bakery Box",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🥐", Color.parseColor("#EFCB9C")),
            CandyType.CHOCOLATE to CandyVisual("🍰", Color.parseColor("#F3C6D3")),
            CandyType.LOLLIPOP to CandyVisual("🧇", Color.parseColor("#EBC27E")),
            CandyType.CANDY to CandyVisual("🍮", Color.parseColor("#F2DB9A")),
            CandyType.COOKIE to CandyVisual("🥨", Color.parseColor("#D8A968")),
            CandyType.CUPCAKE to CandyVisual("🍩", Color.parseColor("#F6C89A"))
        )
    )

    val SPACE = CandySkin(
        id = "space",
        displayName = "Space Snacks",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🪐", Color.parseColor("#C9B8EC")),
            CandyType.CHOCOLATE to CandyVisual("⭐", Color.parseColor("#F5E28C")),
            CandyType.LOLLIPOP to CandyVisual("🌙", Color.parseColor("#B8C7EC")),
            CandyType.CANDY to CandyVisual("☄️", Color.parseColor("#F0A6AE")),
            CandyType.COOKIE to CandyVisual("👽", Color.parseColor("#A8E6C4")),
            CandyType.CUPCAKE to CandyVisual("🚀", Color.parseColor("#F6C89A"))
        )
    )

    val GARDEN = CandySkin(
        id = "garden",
        displayName = "Garden Treats",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🌻", Color.parseColor("#F7E27A")),
            CandyType.CHOCOLATE to CandyVisual("🍄", Color.parseColor("#E8B4A0")),
            CandyType.LOLLIPOP to CandyVisual("🌷", Color.parseColor("#F4A6C8")),
            CandyType.CANDY to CandyVisual("🐝", Color.parseColor("#F5D273")),
            CandyType.COOKIE to CandyVisual("🍀", Color.parseColor("#A8D8A0")),
            CandyType.CUPCAKE to CandyVisual("🦋", Color.parseColor("#B8D4F0"))
        )
    )

    val WINTER = CandySkin(
        id = "winter",
        displayName = "Winter Wonders",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("❄️", Color.parseColor("#C7E8F5")),
            CandyType.CHOCOLATE to CandyVisual("⛄", Color.parseColor("#E8EEF2")),
            CandyType.LOLLIPOP to CandyVisual("🎄", Color.parseColor("#A8D5B5")),
            CandyType.CANDY to CandyVisual("🧊", Color.parseColor("#B3E0EC")),
            CandyType.COOKIE to CandyVisual("🦌", Color.parseColor("#D4B896")),
            CandyType.CUPCAKE to CandyVisual("🧣", Color.parseColor("#E8A0A8"))
        )
    )

    val GEMS = CandySkin(
        id = "gems",
        displayName = "Jewel Box",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("💎", Color.parseColor("#C7E8F5")),
            CandyType.CHOCOLATE to CandyVisual("🔷", Color.parseColor("#BFDCF2")),
            CandyType.LOLLIPOP to CandyVisual("🔶", Color.parseColor("#F9D9A8")),
            CandyType.CANDY to CandyVisual("⭐", Color.parseColor("#F7EFB8")),
            CandyType.COOKIE to CandyVisual("🔺", Color.parseColor("#F6C2C2")),
            CandyType.CUPCAKE to CandyVisual("🟪", Color.parseColor("#D9C4EE"))
        )
    )

    val OCEAN = CandySkin(
        id = "ocean",
        displayName = "Ocean Treats",
        visuals = mapOf(
            CandyType.DONUT to CandyVisual("🐚", Color.parseColor("#F6E9D8")),
            CandyType.CHOCOLATE to CandyVisual("🐠", Color.parseColor("#B8D4F0")),
            CandyType.LOLLIPOP to CandyVisual("🦀", Color.parseColor("#F6A9A0")),
            CandyType.CANDY to CandyVisual("🐡", Color.parseColor("#F9C583")),
            CandyType.COOKIE to CandyVisual("🦑", Color.parseColor("#E8C4EE")),
            CandyType.CUPCAKE to CandyVisual("🌊", Color.parseColor("#A8D8E8"))
        )
    )

    /** No fixed cap — add more skins here any time; every one shows up in Settings automatically. */
    val all: List<CandySkin> = listOf(COZY_BLOOM, CLASSIC, FRUITS, SHAPES, BAKERY, SPACE, GARDEN, WINTER, GEMS, OCEAN)

    fun byId(id: String): CandySkin = all.firstOrNull { it.id == id } ?: CLASSIC
}
