package com.example.appsemesterproject

import android.graphics.RectF

class Player(var x: Float, var y: Float) {

    var dx = 0f
    var dy = 0f
    val size = 50f  // Player size (square)
    private val gravity = 0.5f  // Gravity strength
    private val jumpStrength = 15f  // Jump strength
    private val maxFallSpeed = 10f  // Maximum falling speed
    private var isInAir = false

    // Update player position based on movement and gravity
    fun update(isMovingLeft: Boolean, isMovingRight: Boolean, isJumping: Boolean) {
        // Handle horizontal movement
        if (isMovingLeft) {
            dx = -5f  // Move left
        } else if (isMovingRight) {
            dx = 5f  // Move right
        } else {
            dx = 0f  // No horizontal movement
        }

        // Handle jumping (apply gravity if not jumping)
        if (isJumping && !isInAir) {
            dy = -jumpStrength
            isInAir = true
        }

        // Apply gravity when falling (if player is in the air)
        if (dy < maxFallSpeed) {
            dy += gravity
        }

        // Update position based on movement
        x += dx
        y += dy

        // Prevent player from falling through the ground
        if (y > 1080 - size) {  // Adjust this number based on your screen height
            y = 1080 - size
            dy = 0f
            isInAir = false
        }

        // Prevent player from moving off the screen horizontally
        if (x < 0) {
            x = 0f
        } else if (x + size > 1920) {  // Adjust based on screen width
            x = 1920 - size
        }
    }

    // Return the player's bounding rectangle for collision detection
    fun getBoundingRect(): RectF {
        return RectF(x, y, x + size, y + size)
    }
}
