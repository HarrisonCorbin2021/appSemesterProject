package com.example.appsemesterproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var isRunning = false
    private val thread = Thread(this)

    private val player = Player(100f, 300f, 0f, 0f)
    private val paint = Paint()

    // Controller bounds
    private val leftButtonRect = android.graphics.RectF(50f, 900f, 200f, 1050f)
    private val rightButtonRect = android.graphics.RectF(250f, 900f, 400f, 1050f)
    private val jumpButtonRect = android.graphics.RectF(750f, 900f, 900f, 1050f)

    // Player movement state
    private var isMovingLeft = false
    private var isMovingRight = false

    init {
        holder.addCallback(this)
        paint.isAntiAlias = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    private fun startGame() {
        isRunning = true
        thread.start()
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
                update()
                drawGame(canvas)
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun update() {
        // Update player movement based on controller state
        if (isMovingLeft) player.dx = -5f
        if (isMovingRight) player.dx = 5f
        if (!isMovingLeft && !isMovingRight) player.dx = 0f

        // Update the player's position
        player.update()
    }

    private fun drawGame(canvas: Canvas) {
        // Clear the screen
        canvas.drawColor(Color.BLACK)

        // Draw the player
        paint.color = Color.YELLOW
        canvas.drawRect(
            player.x,
            player.y,
            player.x + player.size,
            player.y + player.size,
            paint
        )

        // Draw the controller
        paint.color = Color.GRAY
        canvas.drawRect(leftButtonRect, paint)
        canvas.drawRect(rightButtonRect, paint)
        canvas.drawRect(jumpButtonRect, paint)

        // Add labels for buttons
        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("←", 100f, 990f, paint)
        canvas.drawText("→", 300f, 990f, paint)
        canvas.drawText("JUMP", 770f, 990f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                when {
                    leftButtonRect.contains(event.x, event.y) -> {
                        isMovingLeft = true
                        isMovingRight = false
                    }
                    rightButtonRect.contains(event.x, event.y) -> {
                        isMovingRight = true
                        isMovingLeft = false
                    }
                    jumpButtonRect.contains(event.x, event.y) -> {
                        player.jump()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // Stop movement when finger is lifted
                isMovingLeft = false
                isMovingRight = false
            }
        }
        return true
    }
}

data class Player(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    val gravity: Float = 0.5f,
    val size: Float = 50f
) {
    fun update() {
        // Apply gravity and update position
        dy += gravity
        x += dx
        y += dy

        // Prevent falling off the screen
        if (y > 800f) {
            y = 800f
            dy = 0f
        }
    }

    fun jump() {
        if (y >= 800f) { // Only jump if on the ground
            dy = -15f
        }
    }
}
