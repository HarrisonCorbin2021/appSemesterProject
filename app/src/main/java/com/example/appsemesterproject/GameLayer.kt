package com.example.appsemesterproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class GameLayer(private val screenWidth: Int, private val screenHeight: Int) {

    private var backgroundBitmap: Bitmap? = null
    private var platformBitmap: Bitmap? = null
    private var groundBitmap: Bitmap? = null
    private var bossBitmap: Bitmap? = null

    private val platforms = mutableListOf<Rect>() // Rectangles representing platforms
    private val ground = Rect(0, screenHeight - 100, screenWidth, screenHeight) // Ground rectangle

    // Player reference
    var player: Player? = null

    // Setters for game elements
    fun setBackground(bitmap: Bitmap) {
        backgroundBitmap = bitmap
    }

    fun setPlatformImage(bitmap: Bitmap) {
        platformBitmap = bitmap
    }

    fun setGroundImage(bitmap: Bitmap) {
        groundBitmap = bitmap
    }

    fun setBoss(bitmap: Bitmap) {
        bossBitmap = bitmap
    }

    fun removeBoss() {
        bossBitmap = null
    }

    // Update function called every frame
    fun update(deltaTime: Float) {
        // Update the player's position
        player?.update(deltaTime)

        // Check for collisions
        player?.let { checkCollision(it) }
    }

    // Collision detection
     fun checkCollision(player: Player) {
        // Check collision with ground
        if (player.hitbox.intersect(ground)) {
            player.onGroundCollision()
        }

        // Check collision with platforms
        for (platform in platforms) {
            if (player.hitbox.intersect(platform)) {
                player.onPlatformCollision(platform)
            }
        }
    }


    // Add a platform to the game
    fun addPlatform(left: Int, top: Int, right: Int, bottom: Int) {
        platforms.add(Rect(left, top, right, bottom))
    }

    // Draw all game elements
    fun draw(canvas: Canvas) {
        // Draw background
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // Draw platforms
        platformBitmap?.let { bitmap ->
            for (platform in platforms) {
                canvas.drawBitmap(bitmap, null, platform, null)
            }
        }

        // Draw ground
        groundBitmap?.let {
            canvas.drawBitmap(it, null, ground, null)
        }

        // Draw player
        player?.draw(canvas)

        // Draw boss (if any)
        bossBitmap?.let {
            canvas.drawBitmap(it, screenWidth / 2f - it.width / 2f, screenHeight - it.height.toFloat(), null)
        }
    }
}
