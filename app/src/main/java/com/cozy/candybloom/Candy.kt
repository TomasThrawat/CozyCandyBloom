package com.cozy.candybloom

/**
 * The six candy "flavors" a board can contain. Visuals (emoji + tile color)
 * come from the selected CandySkin, so the same logical board works with
 * any skin the player picks in Settings.
 */
enum class CandyType {
    DONUT, CHOCOLATE, LOLLIPOP, CANDY, COOKIE, CUPCAKE;

    companion object {
        fun random(): CandyType = entries.random()
    }
}

/** A single grid cell: which candy sits there (null = empty, mid-cascade). */
data class Cell(var type: CandyType?)
