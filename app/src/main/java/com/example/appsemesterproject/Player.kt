package com.example.appsemesterproject

import android.content.res.Resources
import android.graphics.RectF

class Player(var x: Float, var y: Float) {

    var dx = 0f
    var dy = 0f
    val size = 50f  // Player size (square)
    private val gravity = 0.5f  // Gravity strength
    private val jumpStrength = 15f  // Jump strength
    private val maxFallSpeed = 10f  // Maximum falling speed
    var isInAir = false
    private val displayMetrics = Resources.getSystem().displayMetrics
    private val screenHeight = displayMetrics.heightPixels

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

        // Debugging: Log player's vertical position and ground height for comparison

        // Check for landing (collision with ground)
        if (y > screenHeight - 300f - size) {
            y = screenHeight - 300f - size
            dy = 0f
            if (isInAir) { // Only update if the player was in the air
                isInAir = false  // Player is on the ground, can jump again
            }
        }
    }

    fun handlePlatformCollision(platform: RectF) {
        // If the player is falling and intersects with the platform
        if (dy > 0 && RectF.intersects(platform, getBoundingRect())) {
            // Adjust the player to sit on top of the platform
            y = platform.top - size
            dy = 0f  // Stop the downward movement
            isInAir = false  // Player is now on the platform
        }
    }

    // Return the player's bounding rectangle for collision detection
    fun getBoundingRect(): RectF {
        return RectF(x, y, x + size, y + size)
    }
}
