package com.cozy.candybloom

/**
 * Pure game-state logic for an 8x8 candy-crush-style match-3 board.
 * Holds no Android/UI code so it stays simple to reason about and test.
 */
class Match3Engine(val rows: Int = 8, val cols: Int = 8) {

    val grid: Array<Array<CandyType?>> = Array(rows) { arrayOfNulls(cols) }
    var score: Int = 0
        private set

    init {
        fillInitialBoardWithoutMatches()
    }

    private fun fillInitialBoardWithoutMatches() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                var candidate: CandyType
                do {
                    candidate = CandyType.random()
                } while (createsImmediateMatch(r, c, candidate))
                grid[r][c] = candidate
            }
        }
    }

    private fun createsImmediateMatch(r: Int, c: Int, type: CandyType): Boolean {
        // Check two to the left
        if (c >= 2 && grid[r][c - 1] == type && grid[r][c - 2] == type) return true
        // Check two above
        if (r >= 2 && grid[r - 1][c] == type && grid[r - 2][c] == type) return true
        return false
    }

    fun isAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val dr = kotlin.math.abs(r1 - r2)
        val dc = kotlin.math.abs(c1 - c2)
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1)
    }

    fun swap(r1: Int, c1: Int, r2: Int, c2: Int) {
        val tmp = grid[r1][c1]
        grid[r1][c1] = grid[r2][c2]
        grid[r2][c2] = tmp
    }

    /** Returns every cell that is part of a run of 3+ same-type candies, horizontally or vertically. */
    fun findMatches(): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()

        // Horizontal runs
        for (r in 0 until rows) {
            var runStart = 0
            var c = 1
            while (c <= cols) {
                val sameAsRunStart = c < cols && grid[r][c] != null && grid[r][c] == grid[r][runStart]
                if (!sameAsRunStart) {
                    if (c - runStart >= 3) {
                        for (k in runStart until c) matched.add(r to k)
                    }
                    runStart = c
                }
                c++
            }
        }

        // Vertical runs
        for (c in 0 until cols) {
            var runStart = 0
            var r = 1
            while (r <= rows) {
                val sameAsRunStart = r < rows && grid[r][c] != null && grid[r][c] == grid[runStart][c]
                if (!sameAsRunStart) {
                    if (r - runStart >= 3) {
                        for (k in runStart until r) matched.add(k to c)
                    }
                    runStart = r
                }
                r++
            }
        }

        return matched
    }

    /** Clears cells and awards score (bigger groups worth more per candy). */
    fun clearCells(cells: Set<Pair<Int, Int>>) {
        val valid = cells.filter { (r, c) -> r in 0 until rows && c in 0 until cols && grid[r][c] != null }.toSet()
        val bonus = if (valid.size > 3) 10 else 0
        score += valid.size * (10 + bonus)
        for ((r, c) in valid) grid[r][c] = null
    }

    fun clearMatches(matches: Set<Pair<Int, Int>>) = clearCells(matches)

    /** Rocket clears the selected row and column together. */
    fun rocketCells(row: Int, col: Int): Set<Pair<Int, Int>> = buildSet {
        for (c in 0 until cols) add(row to c)
        for (r in 0 until rows) add(r to col)
    }

    /** Bomb clears a 5x5 area centered on the selected cell, clipped at board edges. */
    fun bombCells(row: Int, col: Int): Set<Pair<Int, Int>> = buildSet {
        for (r in row - 2..row + 2) for (c in col - 2..col + 2) {
            if (r in 0 until rows && c in 0 until cols) add(r to c)
        }
    }

    /** Paddle clears only the single selected candy. */
    fun paddleCells(row: Int, col: Int): Set<Pair<Int, Int>> = setOf(row to col)

    /**
     * Applies gravity: candies fall to fill empty cells below them, new random
     * candies spawn at the top of each column. Returns, per column, how many
     * new candies were spawned (useful for the view to animate a "fall from above").
     */
    fun applyGravityAndRefill(): IntArray {
        val spawned = IntArray(cols)
        for (c in 0 until cols) {
            var writeRow = rows - 1
            for (r in rows - 1 downTo 0) {
                if (grid[r][c] != null) {
                    grid[writeRow][c] = grid[r][c]
                    if (writeRow != r) grid[r][c] = null
                    writeRow--
                }
            }
            while (writeRow >= 0) {
                grid[writeRow][c] = CandyType.random()
                spawned[c]++
                writeRow--
            }
        }
        return spawned
    }

    fun resetScore() {
        score = 0
    }
}
