package com.example.appsemesterproject

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.pow
import kotlin.math.sqrt

class GameView(context: Context, private val gameLayer: GameLayer, private val player: Player) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    var isRunning = false
    private lateinit var thread: Thread

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val screenWidth = displayMetrics.widthPixels
    private val screenHeight = displayMetrics.heightPixels
    private val background = Background(context, screenWidth, screenHeight)

    private var isMovingLeft = false
    private var isMovingRight = false
    private var isJumping = false

    // Control button positions
    private val buttonSize = 200f
    private val buttonPaint = Paint().apply { color = Color.GRAY }
    private val leftButton = RectF(50f, screenHeight - buttonSize - 50f, 50f + buttonSize, screenHeight - 50f)
    private val rightButton = RectF(leftButton.right + 50f, leftButton.top, leftButton.right + 50f + buttonSize, leftButton.bottom)
    private val jumpButton = RectF(screenWidth - buttonSize - 50f, screenHeight - buttonSize - 50f, screenWidth - 50f, screenHeight - 50f)

    // Gear button (settings button)
    private val settingsButton = RectF(50f, 50f, 50f + 75f, 50f + 75f) // Top left corner
    private val gearIcon = BitmapFactory.decodeResource(context.resources, R.drawable.gear) // Gear icon

    init {
        Log.d("GameView", "GameView: GameView Initializing")
        Log.d("GameView", "GameView: Player at")
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("GameView", "GameView: Surface created")
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    fun startGame() {
        if (!isRunning) {
            isRunning = true
            thread = Thread(this)
            thread.start()
        }
    }

    fun stopGame() {
        isRunning = false
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (isRunning) {
            if (holder.surface.isValid) {
                val canvas = holder.lockCanvas()
                if (canvas != null) {  // Ensure the canvas is valid before proceeding
                    try {
                        update()
                        drawGame(canvas)
                    } finally {
                        // Unlock the canvas and post it back
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }
    }

    private fun update() {
        // Update player, background, and game layer
        player.update(isMovingLeft, isMovingRight, isJumping)

        // Prevent player from moving off the screen horizontally
        if (player.x < 0) {
            player.x = 0f
        }
        if (player.x + player.size > screenWidth) {
            player.x = screenWidth - player.size
        }

        // Update background and game layer for scrolling
        background.update(player.dx)
        gameLayer.update(player.dx, player.x)
        // Always check for star collisions
        gameLayer.checkCollision(player)

        // Check for collisions with platforms and stars
        if (!gameLayer.checkCollision(player)) {
            // Apply gravity if no collision
            player.dy += 0.5f
            if (player.dy > 10f) {
                player.dy = 10f // Clamp falling speed
            }
        }

        // Update player position
        player.y += player.dy

        // Ensure the player does not fall below the ground
        if (player.y + player.size > screenHeight - gameLayer.groundHeight) {
            player.y = screenHeight - gameLayer.groundHeight - player.size
            player.dy = 0f
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.sketchvalleybg), 0f, 0f, null)

        // Draw the background
        background.draw(canvas)

        // Draw the game elements (platforms, etc.)
        gameLayer.draw(canvas)

        // Draw the player
        val paint = Paint().apply { color = Color.YELLOW }
        canvas.drawRect(
            player.x,
            player.y,
            player.x + player.size,
            player.y + player.size,
            paint
        )

        // Draw the red line at the ground height
        val groundLinePaint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f  // Line thickness
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, screenHeight - gameLayer.groundHeight.toFloat(), screenWidth.toFloat(), screenHeight - gameLayer.groundHeight.toFloat(), groundLinePaint)

        // Draw the control buttons
        canvas.drawRoundRect(leftButton, 20f, 20f, buttonPaint)
        canvas.drawRoundRect(rightButton, 20f, 20f, buttonPaint)
        canvas.drawRoundRect(jumpButton, 20f, 20f, buttonPaint)

        // Add text to buttons
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Left", leftButton.centerX(), leftButton.centerY() + 15f, textPaint)
        canvas.drawText("Right", rightButton.centerX(), rightButton.centerY() + 15f, textPaint)
        canvas.drawText("Jump", jumpButton.centerX(), jumpButton.centerY() + 15f, textPaint)

        // Draw grappling points
        val grapplePaint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        for (point in gameLayer.getGrapplePoints()) {
            canvas.drawCircle(point.x, point.y, 20f, grapplePaint)
        }

        // Draw the grappling hook line if active
        if (player.grappleTarget != null) {
            val grappleLinePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 4f
            }
            canvas.drawLine(
                player.x + player.size / 2,
                player.y + player.size / 2,
                player.grappleTarget!!.x,
                player.grappleTarget!!.y,
                grappleLinePaint
            )
        }

        // Draw the settings button (gear icon)
        canvas.drawBitmap(gearIcon, null, settingsButton, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovingLeft = x < leftButton.right && x > leftButton.left && y < leftButton.bottom && y > leftButton.top
                isMovingRight = x < rightButton.right && x > rightButton.left && y < rightButton.bottom && y > rightButton.top
                if (x < jumpButton.right && x > jumpButton.left && y < jumpButton.bottom && y > jumpButton.top) {
                    isJumping = !player.isInAir  // Set jumping to true if player is not in air
                }

                // Check if the settings button (gear) was clicked
                if (x < settingsButton.right && x > settingsButton.left && y < settingsButton.bottom && y > settingsButton.top) {
                    navigateToSettings()
                }

                val touchRadius = 50f // Define a reasonable radius for touch detection
                val activeGrapple = gameLayer.getActiveGrapple()
                if (activeGrapple != null) {
                    val distance = sqrt(
                        (activeGrapple.x - x).toDouble().pow(2.0) +
                                (activeGrapple.y - y).toDouble().pow(2.0)
                    ).toFloat()

                    if (distance <= touchRadius) {
                        // Set grapple target
                        player.grappleTo(PointF(activeGrapple.x, activeGrapple.y))
                    }
                }

            }
            MotionEvent.ACTION_UP -> {
                isMovingLeft = false
                isMovingRight = false
                isJumping = false
            }
        }
        return true
    }

    // Function to navigate to SettingsActivity with the game data
    private fun navigateToSettings() {
        val intent = Intent(context, SettingsActivity::class.java)

        // Pass the player data (level and position) to SettingsActivity
        val playerLevel = 1  // Replace with actual player level
        val playerPositionX = player.x  // Player X position
        val playerPositionY = player.y  // Player Y position

        // Pass data via intent
        intent.putExtra("playerLevel", playerLevel)
        intent.putExtra("playerX", playerPositionX)
        intent.putExtra("playerY", playerPositionY)

        context.startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_A -> {
                isMovingLeft = true
                return true
            }
            KeyEvent.KEYCODE_D -> {
                isMovingRight = true
                return true
            }
            KeyEvent.KEYCODE_SPACE -> {
                isJumping = true
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_A -> {
                isMovingLeft = false
                return true
            }
            KeyEvent.KEYCODE_D -> {
                isMovingRight = false
                return true
            }
            KeyEvent.KEYCODE_SPACE -> {
                isJumping = false
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}
