package com.example.appsemesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF

class GameLayer(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    // Load platform and ground images
    private var platformBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platforma)
    private var groundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platformb)
    private val platforms = mutableListOf<RectF>()
    private val groundHeight = 300f  // Height of the ground image
    private var groundOffset = 0f  // The current offset of the ground for scrolling

    init {

        // Resize the ground image to fit the screen width and a fixed height
        groundBitmap = Bitmap.createScaledBitmap(groundBitmap, screenWidth, groundHeight.toInt(), true)
        // Resize the platform image to a fixed width and height
        platformBitmap = Bitmap.createScaledBitmap(platformBitmap, 200, 50, true)

        // Add initial platforms (RectF is still used for platform locations)
        platforms.add(RectF(200f, 500f, 600f, 550f))  // Example platform
        platforms.add(RectF(800f, 400f, 1000f, 450f)) // Example platform
    }

    fun update(playerSpeed: Float) {
        // Scroll platforms with the background
        for (platform in platforms) {
            platform.offset(-playerSpeed, 0f)
        }

        // Remove platforms that move out of the screen and add new ones if necessary
        if (platforms.isNotEmpty() && platforms.first().right < 0) {
            platforms.removeAt(0)
            addPlatform()
        }

        // Scroll the ground by updating its offset based on the player's movement
        groundOffset -= playerSpeed

        // Reset the ground offset when it goes off the screen on the left side
        if (groundOffset <= -groundBitmap.width.toFloat()) {
            groundOffset = 0f
        }
    }

    private fun addPlatform() {
        // Add a new platform at a random position beyond the screen width
        val width = (200..400).random().toFloat()
        val height = 50f
        val left = (screenWidth + 200).toFloat() // Platform is created just off-screen
        val top = (300..700).random().toFloat()
        platforms.add(RectF(left, top, left + width, top + height))
    }

    fun draw(canvas: Canvas) {
        // Tile the ground image across the bottom of the screen and scroll it
        var xOffset = groundOffset
        while (xOffset < screenWidth) {
            canvas.drawBitmap(groundBitmap, xOffset, screenHeight - groundHeight, null)
            xOffset += groundBitmap.width.toFloat() // Move to the next tile position
        }

        // If the ground has scrolled past the left edge, reset and draw again to fill the screen
        if (groundOffset < 0) {
            canvas.drawBitmap(groundBitmap, xOffset, screenHeight - groundHeight, null)
        }

        // Draw the platforms as a tiled sequence
        for (platform in platforms) {
            var platformXOffset = platform.left
            while (platformXOffset < platform.right) {
                canvas.drawBitmap(platformBitmap, platformXOffset, platform.top, null)
                platformXOffset += platformBitmap.width.toFloat() // Move to the next platform tile position
            }
        }
    }

    fun checkCollision(player: Player): Boolean {
        // Check if the player collides with any platform
        for (platform in platforms) {
            if (RectF.intersects(platform, player.getBoundingRect())) {
                // Adjust player's position to sit on top of the platform
                player.y = platform.top - player.size
                player.dy = 0f
                return true
            }
        }
        return false
    }
}
