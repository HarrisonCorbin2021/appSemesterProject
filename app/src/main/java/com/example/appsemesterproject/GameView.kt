package com.example.appsemesterproject

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, private val gameLayer: GameLayer) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    var isRunning = false
    private lateinit var thread: Thread

    private val player = Player(100f, 300f)
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
                try {
                    update()
                    drawGame(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun update() {
        //Log.d("GameView", "GameView: update called")
        // Update player, background, and game layer
        player.update(isMovingLeft, isMovingRight, isJumping)
        //Log.d("GameView", "GameView: Player position: x=${player.x}, y=${player.y}")

        // Prevent player from moving off the screen horizontally
        if (player.x < 0) {
            player.x = 0f
        }
        if (player.x + player.size > screenWidth) {
            player.x = screenWidth - player.size
        }

        // Update background and game layer for scrolling
        background.update(player.dx)
        gameLayer.update(player.dx, player)

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
        //Log.d("GameView", "GameView: drawGame called")
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

                val touchRadius = 50f // Define a reasonable radius for touch detection
                val activeGrapple = gameLayer.getActiveGrapple()
                if (activeGrapple != null) {
                    val distance = Math.sqrt(
                        Math.pow((activeGrapple.x - x).toDouble(), 2.0) +
                                Math.pow((activeGrapple.y - y).toDouble(), 2.0)
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
