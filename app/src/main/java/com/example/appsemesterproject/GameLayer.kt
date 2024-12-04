package com.example.appsemesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.sqrt

class GameLayer(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    // Load platform and ground images
    private var platformBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platformb)
    private var groundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.platformb)
    private val platforms = mutableListOf<RectF>()
    val groundHeight = 300f  // Height of the ground image
    private var groundOffset = 0f  // The current offset of the ground for scrolling

    // Stars for scoring
    private val stars = mutableListOf<Star>()
    private val starRadius = 10f // Radius of the stars
    private val starPaint = Paint().apply { color = Color.YELLOW }

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

        // Add stars centered above each platform
        platforms.forEach { platform ->
            // Calculate the center of the platform
            val starX = platform.centerX()  // Center the star over the platform
            val starY = platform.top - 50f  // Place the star slightly above the platform
            stars.add(Star(starX, starY, starRadius))
        }
    }

    fun update(playerSpeed: Float) {
        // Scroll platforms with the background
        for (platform in platforms) {
            platform.offset(-playerSpeed, 0f)
        }

        // Scroll stars with the background
        for (star in stars) {
            star.x -= playerSpeed
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
            // Draw the platform image
            canvas.drawBitmap(platformBitmap, platform.left, platform.top, null)

            // Set up the paint for the outline (red color)
            val outlinePaint = Paint().apply {
                color = Color.RED
                strokeWidth = 5f // Set the thickness of the outline
                style = Paint.Style.STROKE // Set the style to just outline, not fill
            }

            // Draw the red outline around the platform (hitbox)
            canvas.drawRect(platform, outlinePaint)
        }

        // Draw the stars
        for (star in stars) {
            canvas.drawCircle(star.x, star.y, star.radius, starPaint)
        }
    }


    fun checkCollision(player: Player): Boolean {
        // Check if the player collides with any platform
        for (platform in platforms) {
            // Check for horizontal overlap: player must be within the platform’s horizontal bounds
            val isHorizontallyAligned = player.x + player.size > platform.left && player.x < platform.right

            // Check if the player is falling and within the horizontal bounds of the platform
            if (isHorizontallyAligned && RectF.intersects(platform, player.getBoundingRect())) {
                // Handle the platform collision
                player.handlePlatformCollision(platform)
                return true
            }
        }

        // Check for collisions with stars
        val playerCenterX = player.x + player.size / 2
        val playerCenterY = player.y + player.size / 2
        val iterator = stars.iterator()

        while (iterator.hasNext()) {
            val star = iterator.next()
            val dx = star.x - playerCenterX
            val dy = star.y - playerCenterY
            val distance = sqrt((dx * dx + dy * dy).toDouble())

            if (distance <= star.radius + player.size / 2) {
                iterator.remove() // Remove the star when collected
                // Increment the score or handle the collection logic here
                return true
            }
        }

        return false
    }


    // star data class
    data class Star(var x: Float, var y: Float, val radius: Float)
}
