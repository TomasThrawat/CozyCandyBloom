package com.cozy.candybloom

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---- Game state ----
    private val engine = Match3Engine(rows = 8, cols = 8)
    private val sound = SoundManager()
    var onScoreChanged: ((Int) -> Unit)? = null
    var onLevelResult: ((Int) -> Unit)? = null

    // ---- Level config ----
    private var targetScore: Int = Int.MAX_VALUE
    private var levelEnded: Boolean = false

    enum class PowerUp { NONE, ROCKET, BOMB, PADDLE }
    private var selectedPowerUp = PowerUp.NONE
    var onPowerUpChanged: ((PowerUp) -> Unit)? = null

    /** Fired right after a booster is actually used (a candy was cleared), so the
     *  host Activity can decrement the purchased inventory it came from. */
    var onBoosterConsumed: ((PowerUp) -> Unit)? = null

    // ---- Appearance ----
    private var currentSkin: CandySkin = CandySkins.COZY_BLOOM
    private var currentFrame: CandyFrame = CandyFrames.NORMAL

    // ---- Layout ----
    private var cellSize = 0f
    private var boardLeft = 0f
    private var boardTop = 0f
    private val boardPadding = 12f
    private val candyInset = 2f

    // ---- Animation state machine ----
    private enum class State { IDLE, SWAP_FORWARD, SWAP_INVALID_BACK, REMOVING, FALLING }
    private var state = State.IDLE

    private var swapA = 0 to 0
    private var swapB = 0 to 0
    private var swapProgress = 0f
    private val swapDurationNs = 150_000_000L

    private var removingCells: Map<Pair<Int, Int>, CandyType> = emptyMap()
    private var removeProgress = 0f
    private val removeDurationNs = 170_000_000L
    private var isFirstCycleInChain = true

    private data class FallAnim(val row: Int, val col: Int, val fallRows: Int, val type: CandyType)
    private var fallAnims: List<FallAnim> = emptyList()
    private var fallProgress = 0f
    private val fallDurationNs = 230_000_000L

    // ---- Touch ----
    private var touchStartCell: Pair<Int, Int>? = null
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var swipeHandled = false
    private val swipeThresholdDp = 18f

    // ---- Paint objects (created once, reused every frame) ----
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    private val iconBitmapCache = HashMap<Int, Bitmap>()
    private val boardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ---- Cached colors/shaders (avoid re-parsing/re-allocating every single frame) ----
    private val cellColorLight = Color.parseColor("#FFF8F0E3")
    private val cellColorDark = Color.parseColor("#FFF0E2CE")
    private val backdropColorTop = Color.parseColor("#FFF8F0E3")
    private val backdropColorBottom = Color.parseColor("#FFFDE8EC")
    private var backdropShader: LinearGradient? = null

    private var lastFrameTimeNs = 0L
    private val choreographer = Choreographer.getInstance()
    private var running = false
    private var frameScheduled = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNs: Long) {
            frameScheduled = false
            if (!running) return
            if (lastFrameTimeNs == 0L) lastFrameTimeNs = frameTimeNs
            var deltaNs = frameTimeNs - lastFrameTimeNs
            val maxStepNs = 33_000_000L // ~2 frames at 60fps
            if (deltaNs > maxStepNs) deltaNs = maxStepNs
            lastFrameTimeNs = frameTimeNs

            advanceAnimations(deltaNs)
            invalidate()

            // Keep animating only while something is actually moving. An idle board
            // never changes, so redrawing it every 16ms forever was pure waste.
            if (state != State.IDLE) {
                requestAnimationFrame()
            }
        }
    }

    /** Schedules the next animation frame if one isn't already pending. */
    private fun requestAnimationFrame() {
        if (!running || frameScheduled) return
        frameScheduled = true
        lastFrameTimeNs = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    init {
        onScoreChanged?.invoke(engine.score)
    }

    /** Swaps the emoji/color set used to draw every candy type. */
    fun applySkin(skin: CandySkin) {
        currentSkin = skin
        invalidate()
    }

    /**
     * Swaps the cosmetic border ("frame") drawn around every candy tile.
     * Purely visual — free to change from Settings any time, no limit on use.
     */
    fun applyFrame(frame: CandyFrame) {
        currentFrame = frame
        invalidate()
    }

    /** Hints the system to switch the hosting window's display refresh rate, if supported. */
    fun applyTargetFps(fps: Int) {
        val activity = context as? Activity ?: return
        val window = activity.window ?: return
        val attrs = window.attributes
        attrs.preferredRefreshRate = fps.toFloat()
        window.attributes = attrs
    }

    /** Selects the power-up that will be applied to the next tapped cell. */
    fun selectPowerUp(powerUp: PowerUp) {
        selectedPowerUp = powerUp
        onPowerUpChanged?.invoke(powerUp)
    }

    /** Sets up the target score for the level, and resets level-end state. Swaps are unlimited. */
    fun configureLevel(targetScore: Int) {
        this.targetScore = targetScore
        this.levelEnded = false
        onScoreChanged?.invoke(engine.score)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        lastFrameTimeNs = 0L
        if (state != State.IDLE) requestAnimationFrame()
        onScoreChanged?.invoke(engine.score)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running = false
        frameScheduled = false
        choreographer.removeFrameCallback(frameCallback)
        sound.release()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val usableW = w - boardPadding * 2
        val usableH = h - boardPadding * 2
        cellSize = min(usableW / engine.cols, usableH / engine.rows)
        val boardW = cellSize * engine.cols
        boardLeft = (w - boardW) / 2f
        boardTop = boardPadding

        backdropShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            backdropColorTop, backdropColorBottom,
            Shader.TileMode.CLAMP
        )
    }

    // ---------------------------------------------------------------------
    // Animation stepping
    // ---------------------------------------------------------------------

    private fun advanceAnimations(deltaNs: Long) {
        when (state) {
            State.SWAP_FORWARD -> {
                swapProgress += deltaNs / swapDurationNs.toFloat()
                if (swapProgress >= 1f) {
                    swapProgress = 1f
                    onSwapForwardComplete()
                }
            }
            State.SWAP_INVALID_BACK -> {
                swapProgress += deltaNs / swapDurationNs.toFloat()
                if (swapProgress >= 1f) {
                    swapProgress = 1f
                    state = State.IDLE
                }
            }
            State.REMOVING -> {
                removeProgress += deltaNs / removeDurationNs.toFloat()
                if (removeProgress >= 1f) {
                    removeProgress = 1f
                    beginFall()
                }
            }
            State.FALLING -> {
                fallProgress += deltaNs / fallDurationNs.toFloat()
                if (fallProgress >= 1f) {
                    fallProgress = 1f
                    onFallComplete()
                }
            }
            State.IDLE -> { /* nothing to advance */ }
        }
    }

    private fun onSwapForwardComplete() {
        val (r1, c1) = swapA
        val (r2, c2) = swapB
        engine.swap(r1, c1, r2, c2)
        val matches = engine.findMatches()
        if (matches.isEmpty()) {
            engine.swap(r1, c1, r2, c2)
            sound.playInvalid()
            state = State.SWAP_INVALID_BACK
            swapProgress = 0f
        } else {
            isFirstCycleInChain = true
            resolveMatches(matches)
        }
    }

    private fun resolveMatches(matches: Set<Pair<Int, Int>>) {
        removingCells = matches.associateWith { (r, c) -> engine.grid[r][c]!! }
        engine.clearMatches(matches)
        onScoreChanged?.invoke(engine.score)
        if (isFirstCycleInChain) sound.playMatch(matches.size) else sound.playCascade()
        removeProgress = 0f
        state = State.REMOVING
    }

    private fun beginFall() {
        val rows = engine.rows
        val cols = engine.cols
        val oldGrid = Array(rows) { r -> Array(cols) { c -> engine.grid[r][c] } }
        val spawned = engine.applyGravityAndRefill()

        val anims = mutableListOf<FallAnim>()
        for (c in 0 until cols) {
            val survivorsBottomUp = mutableListOf<Pair<Int, CandyType>>() // (oldRow, type), bottom-most first
            for (r in rows - 1 downTo 0) {
                val t = oldGrid[r][c]
                if (t != null) survivorsBottomUp.add(r to t)
            }
            for (k in survivorsBottomUp.indices) {
                val newRow = rows - 1 - k
                val (oldRow, type) = survivorsBottomUp[k]
                val fallRows = newRow - oldRow
                if (fallRows > 0) anims.add(FallAnim(newRow, c, fallRows, type))
            }
            val spawnCount = spawned[c]
            for (row in 0 until spawnCount) {
                val type = engine.grid[row][c] ?: continue
                anims.add(FallAnim(row, c, spawnCount, type))
            }
        }
        fallAnims = anims
        fallProgress = 0f
        state = if (anims.isEmpty()) State.IDLE else State.FALLING
        if (anims.isEmpty()) checkForFurtherCascade()
    }

    private fun onFallComplete() {
        checkForFurtherCascade()
    }

    private fun checkForFurtherCascade() {
        val matches = engine.findMatches()
        if (matches.isEmpty()) {
            state = State.IDLE
            evaluateLevelEnd()
        } else {
            isFirstCycleInChain = false
            resolveMatches(matches)
        }
    }

    /** Fires the win callback once the board is settled and the target score is reached. Swaps are unlimited, so a level only ends by winning. */
    private fun evaluateLevelEnd() {
        if (levelEnded) return
        if (targetScore == Int.MAX_VALUE) return
        if (engine.score >= targetScore) {
            levelEnded = true
            onLevelResult?.invoke(engine.score)
        }
    }

    // ---------------------------------------------------------------------
    // Touch handling — swipe a candy into a neighbor to attempt a swap,
    // or (if a booster is selected) tap a cell to use it.
    // ---------------------------------------------------------------------

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (cellSize <= 0f) return true
        val density = resources.displayMetrics.density
        val swipeThresholdPx = swipeThresholdDp * density

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (state != State.IDLE || levelEnded) return true
                val tapped = cellAt(event.x, event.y)
                if (selectedPowerUp != PowerUp.NONE && tapped != null) {
                    activatePowerUp(tapped)
                    return true
                }
                touchStartCell = tapped
                touchStartX = event.x
                touchStartY = event.y
                swipeHandled = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (swipeHandled || state != State.IDLE) return true
                val start = touchStartCell ?: return true
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                if (abs(dx) < swipeThresholdPx && abs(dy) < swipeThresholdPx) return true

                val target = if (abs(dx) > abs(dy)) {
                    if (dx > 0) start.first to start.second + 1 else start.first to start.second - 1
                } else {
                    if (dy > 0) start.first + 1 to start.second else start.first - 1 to start.second
                }
                swipeHandled = true
                tryStartSwap(start, target)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchStartCell = null
            }
        }
        return true
    }

    private fun cellAt(x: Float, y: Float): Pair<Int, Int>? {
        val col = ((x - boardLeft) / cellSize).toInt()
        val row = ((y - boardTop) / cellSize).toInt()
        if (row !in 0 until engine.rows || col !in 0 until engine.cols) return null
        return row to col
    }

    private fun activatePowerUp(cell: Pair<Int, Int>) {
        val (row, col) = cell
        val used = selectedPowerUp
        val cells = when (used) {
            PowerUp.ROCKET -> engine.rocketCells(row, col)
            PowerUp.BOMB -> engine.bombCells(row, col)
            PowerUp.PADDLE -> engine.paddleCells(row, col)
            PowerUp.NONE -> emptySet()
        }
        val existing = cells.filter { (r, c) -> engine.grid[r][c] != null }.toSet()
        if (existing.isEmpty()) return
        removingCells = existing.associateWith { (r, c) -> engine.grid[r][c]!! }
        engine.clearCells(existing)
        onScoreChanged?.invoke(engine.score)
        sound.playMatch(existing.size)
        selectedPowerUp = PowerUp.NONE
        onPowerUpChanged?.invoke(selectedPowerUp)
        onBoosterConsumed?.invoke(used)
        removeProgress = 0f
        state = State.REMOVING
        requestAnimationFrame()
    }

    private fun tryStartSwap(a: Pair<Int, Int>, b: Pair<Int, Int>) {
        if (levelEnded) return
        if (b.first !in 0 until engine.rows || b.second !in 0 until engine.cols) return
        if (!engine.isAdjacent(a.first, a.second, b.first, b.second)) return
        swapA = a
        swapB = b
        swapProgress = 0f
        state = State.SWAP_FORWARD
        sound.playSwap()
        requestAnimationFrame()
    }

    // ---------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackdrop(canvas)
        drawBoard(canvas)
        drawCandies(canvas)
    }

    private fun drawBackdrop(canvas: Canvas) {
        boardBgPaint.shader = backdropShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardBgPaint)
    }

    private fun drawBoard(canvas: Canvas) {
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                cellPaint.color = if ((r + c) % 2 == 0) cellColorLight else cellColorDark
                val left = boardLeft + c * cellSize
                val top = boardTop + r * cellSize
                canvas.drawRoundRect(
                    RectF(left + 2f, top + 2f, left + cellSize - 2f, top + cellSize - 2f),
                    14f, 14f, cellPaint
                )
            }
        }
    }

    private fun drawCandies(canvas: Canvas) {
        val fallByPos = fallAnims.associateBy { it.row to it.col }
        val fallEase = easeOutCubic(fallProgress)

        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                if (isCellCurrentlySwapping(r, c)) continue

                val fa = if (state == State.FALLING) fallByPos[r to c] else null
                val type: CandyType? = fa?.type ?: engine.grid[r][c]
                if (type == null) continue

                var drawRow = r.toFloat()
                if (fa != null) {
                    val startRow = r - fa.fallRows
                    drawRow = startRow + (r - startRow) * fallEase
                }
                drawCandyAt(canvas, drawRow, c.toFloat(), type, 1f)
            }
        }

        if (state == State.SWAP_FORWARD || state == State.SWAP_INVALID_BACK) {
            drawSwappingPair(canvas)
        }

        if (state == State.REMOVING) {
            drawRemovingCandies(canvas)
        }
    }

    private fun isCellCurrentlySwapping(r: Int, c: Int): Boolean {
        if (state != State.SWAP_FORWARD && state != State.SWAP_INVALID_BACK) return false
        return (r to c) == swapA || (r to c) == swapB
    }

    private fun drawSwappingPair(canvas: Canvas) {
        val (r1, c1) = swapA
        val (r2, c2) = swapB
        val typeA = engine.grid[r1][c1] ?: return
        val typeB = engine.grid[r2][c2] ?: return
        val ease = easeOutCubic(swapProgress)
        val forward = state == State.SWAP_FORWARD

        val fromRowA = r1.toFloat(); val fromColA = c1.toFloat()
        val toRowA = r2.toFloat(); val toColA = c2.toFloat()
        val fromRowB = r2.toFloat(); val fromColB = c2.toFloat()
        val toRowB = r1.toFloat(); val toColB = c1.toFloat()

        if (forward) {
            drawCandyAt(canvas, lerp(fromRowA, toRowA, ease), lerp(fromColA, toColA, ease), typeA, 1f)
            drawCandyAt(canvas, lerp(fromRowB, toRowB, ease), lerp(fromColB, toColB, ease), typeB, 1f)
        } else {
            drawCandyAt(canvas, lerp(toRowA, fromRowA, ease), lerp(toColA, fromColA, ease), typeA, 1f)
            drawCandyAt(canvas, lerp(toRowB, fromRowB, ease), lerp(toColB, fromColB, ease), typeB, 1f)
        }
    }

    private fun drawRemovingCandies(canvas: Canvas) {
        val shrink = 1f - easeOutCubic(removeProgress)
        for ((pos, type) in removingCells) {
            val (r, c) = pos
            drawCandyAt(canvas, r.toFloat(), c.toFloat(), type, shrink)
        }
    }

    /** row/col may be fractional (mid-animation). scale in [0,1] shrinks the tile (used for removal). */
    private fun drawCandyAt(canvas: Canvas, row: Float, col: Float, type: CandyType, scale: Float) {
        if (scale <= 0.01f) return
        val cx = boardLeft + col * cellSize + cellSize / 2f
        val cy = boardTop + row * cellSize + cellSize / 2f

        val tileSize = (cellSize - candyInset * 2f) * scale
        if (tileSize <= 0f) return
        val half = tileSize / 2f

        val visual = currentSkin.visuals.getValue(type)

        tilePaint.color = visual.bgColor
        tilePaint.alpha = (255 * scale).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(
            RectF(cx - half, cy - half, cx + half, cy + half),
            tileSize * 0.24f, tileSize * 0.24f, tilePaint
        )

        // Optional colorful border ("frame") — cosmetic only, unlimited use, chosen in Settings.
        val frameColors = currentFrame.colors
        if (frameColors != null && tileSize > 8f) {
            val density = resources.displayMetrics.density
            framePaint.strokeWidth = 3f * density
            framePaint.alpha = (255 * scale).toInt().coerceIn(0, 255)
            framePaint.shader = SweepGradient(cx, cy, frameColors.toIntArray(), null)
            val inset = framePaint.strokeWidth / 2f
            canvas.drawRoundRect(
                RectF(cx - half + inset, cy - half + inset, cx + half - inset, cy + half - inset),
                tileSize * 0.22f, tileSize * 0.22f, framePaint
            )
            framePaint.shader = null
        }

        val iconRes = visual.iconRes
        if (iconRes != null) {
            val bmp = getIconBitmap(iconRes)
            iconPaint.alpha = (255 * scale).toInt().coerceIn(0, 255)
            val iconHalf = half * 0.86f
            canvas.drawBitmap(
                bmp, null,
                RectF(cx - iconHalf, cy - iconHalf, cx + iconHalf, cy + iconHalf),
                iconPaint
            )
        } else {
            emojiPaint.textSize = tileSize * 0.78f
            emojiPaint.alpha = (255 * scale).toInt().coerceIn(0, 255)
            val fm = emojiPaint.fontMetrics
            val textY = cy - (fm.ascent + fm.descent) / 2f
            canvas.drawText(visual.emoji, cx, textY, emojiPaint)
        }
    }


    private fun getIconBitmap(resId: Int): Bitmap {
        iconBitmapCache[resId]?.let { return it }
        val size = 128
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val drawable = context.getDrawable(resId)
        drawable?.setBounds(0, 0, size, size)
        drawable?.draw(c)
        iconBitmapCache[resId] = bmp
        return bmp
    }
    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun easeOutCubic(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        val f = clamped - 1f
        return f * f * f + 1f
    }
}
