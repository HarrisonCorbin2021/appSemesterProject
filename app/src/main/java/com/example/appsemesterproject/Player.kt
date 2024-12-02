package com.example.appsemesterproject

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Color
import android.graphics.Paint

class Player(
    private val initialX: Float,
    private val initialY: Float,
    private val width: Int,
    private val height: Int,
    private val screenHeight: Int
) {
    var x: Float = initialX
    var y: Float = initialY
    private val speedY: Float = 0f // Vertical speed

    // Paint object for drawing
    private val paint = Paint().apply {
        color = Color.MAGENTA // Pink color
        style = Paint.Style.FILL
    }

    val hitbox: Rect
        get() = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())

    fun update(deltaTime: Float) {
        // Update the player's position based on physics
        y += speedY * deltaTime
    }

    fun draw(canvas: Canvas) {
        // Draw the player as a pink square
        canvas.drawRect(x, y, x + width, y + height, paint)
    }

    fun onGroundCollision() {
        // Logic when the player collides with the ground
        y = (screenHeight - 100 - height).toFloat()
    }

    fun onPlatformCollision(platform: Rect) {
        // Logic when the player collides with a platform
        y = platform.top - height.toFloat()
    }
}
