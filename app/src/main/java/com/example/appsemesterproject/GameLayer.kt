package com.example.appsemesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF

class GameLayer(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    // Load platform and ground images
    private var platformBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platformb)
    private var groundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platformb)
    private val platforms = mutableListOf<RectF>()
    private val groundHeight = 300f  // Height of the ground image
    private var groundOffset = 0f  // The current offset of the ground for scrolling

    // Define a fixed set of platform positions (adjustable as needed)
    private val platformPositions = listOf(
        RectF(200f, 1100f, 600f, 1150f),  // Platform 1
        RectF(800f, 1200f, 1000f, 1250f), // Platform 2
        RectF(1200f, 1300f, 1400f, 1350f), // Platform 3
        RectF(1600f, 1500f, 1800f, 1550f)  // Platform 4
    )

    init {
        // Resize the ground image to fit the screen width and a fixed height
        groundBitmap = Bitmap.createScaledBitmap(groundBitmap, screenWidth, groundHeight.toInt(), true)
        // Resize the platform image to a fixed width and height
        platformBitmap = Bitmap.createScaledBitmap(platformBitmap, 200, 50, true)

        // Add platforms based on the predefined positions
        platforms.addAll(platformPositions)
    }

    fun update(playerSpeed: Float) {
        // Scroll platforms with the background
        for (platform in platforms) {
            platform.offset(-playerSpeed, 0f)
        }

        // Check and reset platforms when they move off the left or right side of the screen
        for (platform in platforms) {
            if (platform.right < 0) {
                // Move platform to the right side of the screen to be revisitable
                platform.offset(screenWidth + platform.width(), 0f)
            } else if (platform.left > screenWidth) {
                // Move platform to the left side of the screen to be revisitable
                platform.offset(-screenWidth - platform.width(), 0f)
            }
        }

        // Scroll the ground by updating its offset based on the player's movement
        groundOffset -= playerSpeed

        // Reset the ground offset when it goes off the screen on the left side
        if (groundOffset <= -groundBitmap.width.toFloat()) {
            groundOffset = 0f
        }
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

        // Draw the platforms
        for (platform in platforms) {
            canvas.drawBitmap(platformBitmap, platform.left, platform.top, null)
        }
    }

    fun checkCollision(player: Player): Boolean {
        // Check if the player collides with any platform
        for (platform in platforms) {
            // Check for horizontal overlap: player must be within the platform’s horizontal bounds
            val isHorizontallyAligned = player.x + player.size > platform.left && player.x < platform.right

            // Check if the player is falling and within the horizontal bounds of the platform
            if (isHorizontallyAligned && RectF.intersects(platform, player.getBoundingRect())) {
                // If the player is falling (dy > 0) and is near the platform (bottom of player is just above platform's top)
                if (player.dy > 0 && player.getBoundingRect().bottom <= platform.top + 10f) {
                    if (player.dx > 0 && player.getBoundingRect().right <= platform.left + 10f) {
                        if (player.dx > 0 && player.getBoundingRect().left <= platform.right + 10f) {
                            // Adjust player's position to sit on top of the platform
                            player.y = platform.top - player.size
                            player.dy =
                                0f  // Stop downward velocity (player "lands" on the platform)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }


}
