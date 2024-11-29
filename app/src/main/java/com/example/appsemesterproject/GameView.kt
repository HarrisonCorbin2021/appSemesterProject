package com.example.appsemesterproject

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var isRunning = false
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
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("GameView", "Surface created")
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    private fun startGame() {
        if (!isRunning) {
            isRunning = true
            thread = Thread(this)
            thread.start()
        }
    }

    private fun stopGame() {
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
        // Update player and background
        player.update(isMovingLeft, isMovingRight, isJumping)
        background.update(player.dx)
    }

    private fun drawGame(canvas: Canvas) {
        // Draw the background
        background.draw(canvas)

        // Draw the player
        val paint = Paint().apply { color = Color.YELLOW }
        canvas.drawRect(
            player.x,
            player.y,
            player.x + player.size,
            player.y + player.size,
            paint
        )

        // Draw controls
        canvas.drawRoundRect(leftButton, 20f, 20f, buttonPaint)
        canvas.drawRoundRect(rightButton, 20f, 20f, buttonPaint)
        canvas.drawRoundRect(jumpButton, 20f, 20f, buttonPaint)

        // Add text to buttons for clarity
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Left", leftButton.centerX(), leftButton.centerY() + 15f, textPaint)
        canvas.drawText("Right", rightButton.centerX(), rightButton.centerY() + 15f, textPaint)
        canvas.drawText("Jump", jumpButton.centerX(), jumpButton.centerY() + 15f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                when {
                    leftButton.contains(event.x, event.y) -> {
                        isMovingLeft = true
                        isMovingRight = false
                    }
                    rightButton.contains(event.x, event.y) -> {
                        isMovingRight = true
                        isMovingLeft = false
                    }
                    jumpButton.contains(event.x, event.y) -> {
                        isJumping = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // Stop movement on button release
                isMovingLeft = false
                isMovingRight = false
                isJumping = false
            }
        }
        return true
    }
}