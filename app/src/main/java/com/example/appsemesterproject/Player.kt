package com.example.appsemesterproject

class Player(var x: Float, var y: Float) {
    var dx: Float = 0f
    var dy: Float = 0f
    val size = 100f
    private val gravity = 5.0f
    private val jumpSpeed = -50f
    private var isOnGround = true

    fun update(isMovingLeft: Boolean, isMovingRight: Boolean, isJumping: Boolean) {
        if (isMovingLeft) dx = -10f
        if (isMovingRight) dx = 10f
        if (!isMovingLeft && !isMovingRight) dx = 0f

        if (isJumping && isOnGround) {
            dy = jumpSpeed
            isOnGround = false
        }

        // Apply gravity
        dy += gravity

        // Update position
        x += dx
        y += dy

        // Simulate ground collision
        if (y >= 1300f) { // The ground level
            y = 1300f
            dy = 0f
            isOnGround = true
        }
    }
}
