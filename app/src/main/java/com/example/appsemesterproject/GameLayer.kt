package com.example.appsemesterproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Color
import android.util.Log
import kotlin.math.sqrt

class GameLayer(private val screenWidth: Int, private val screenHeight: Int)
{

    // Grapple-able objects or points
    private val grapplePoints = mutableListOf<RectF>()
    private val grappleReticlePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    // The current grapple target
    private var activeGrapple: RectF? = null

    // Load platform and ground images
    private var backgroundBitmap: Bitmap? = null
    private var platformBitmap: Bitmap? = null
    private var groundBitmap: Bitmap? = null
    //private var bossBitmap: Bitmap? = null

    private val platforms = mutableListOf<RectF>() // Rectangles representing platforms
    //private val ground = Rect(0, screenHeight - 100, screenWidth, screenHeight) // Ground rectangle

    val groundHeight = 300f  // Height of the ground image
    private var groundOffset = 0f  // The current offset of the ground for scrolling

    // Stars for scoring
    private val stars = mutableListOf<Star>()
    private val starRadius = 10f // Radius of the stars
    private val starPaint = Paint().apply { color = Color.YELLOW }

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

    init {
        Log.d("GameLayer", "GameLayer: GameLayer Initializing")
        // Ensure the groundBitmap is set properly
        if (groundBitmap != null) {
            groundBitmap = Bitmap.createScaledBitmap(groundBitmap!!, screenWidth, groundHeight.toInt(), true)
        }

        // Resize the platform image to a fixed width and height
        platformBitmap = platformBitmap?.let { Bitmap.createScaledBitmap(it, 200, 50, true) }

        // Add platforms based on the predefined positions
        // Ensure this part is initialized correctly (the platforms list is currently empty in the original code)
        platforms.add(RectF(100f, screenHeight - 400f, 300f, screenHeight - 350f))  // Example platform
        platforms.add(RectF(500f, screenHeight - 500f, 700f, screenHeight - 450f))  // Another platform
        // Add more platforms as needed

        //Test grappling points
        grapplePoints.add(RectF(400f, 800f, 450f, 850f))
        grapplePoints.add(RectF(900f, 700f, 950f, 750f))

        // Add stars centered above each platform
        platforms.forEach { platform ->
            // Calculate the center of the platform
            val starX = platform.centerX()  // Center the star over the platform
            val starY = platform.top - 30f  // Place the star slightly above the platform
            stars.add(Star(starX, starY, starRadius))
        }

        Log.d("GameLayer", "GameLayer: groundBitmap initialized: ${groundBitmap != null}")
        Log.d("GameLayer", "GameLayer: Drawing background: ${backgroundBitmap != null}")
    }

    fun update(playerSpeed: Float, player: Player)
    {
        //Log.d("GameLayer", "GameLayer: update called")
        // Scroll platforms with the background
        for (platform in platforms)
        {
            platform.offset(-playerSpeed, 0f)
        }

        // Scroll stars with the background
        for (star in stars)
        {
            star.x -= playerSpeed
        }

        // Check and reset platforms when they move off the left or right side of the screen
        for (platform in platforms)
        {
            if (platform.right < 0)
            {
                // Move platform to the right side of the screen to be revisitable
                platform.offset(screenWidth + platform.width(), 0f)
            } else if (platform.left > screenWidth)
            {
                // Move platform to the left side of the screen to be revisitable
                platform.offset(-screenWidth - platform.width(), 0f)
            }
        }

        // Scroll the ground by updating its offset based on the player's movement
        groundOffset -= playerSpeed

        // Reset the ground offset when it goes off the screen on the left side
        if (groundBitmap != null && groundOffset <= -groundBitmap!!.width.toFloat())
        {
            groundOffset = 0f
        }
        activeGrapple = grapplePoints.find { point ->
            RectF.intersects(player.getBoundingRect(), point)}
    }

    fun draw(canvas: Canvas)
    {
        //Log.d("GameLayer", "GameLayer: draw called")
        // Draw background first
        backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        // Tile the ground image across the bottom of the screen and scroll it
        var xOffset = groundOffset
        while (xOffset < screenWidth) {
            groundBitmap?.let { canvas.drawBitmap(it, xOffset, screenHeight - groundHeight, null) }
            xOffset += groundBitmap?.width?.toFloat() ?: 0f
        }

        // If the ground has scrolled past the left edge, reset and draw again to fill the screen
        if (groundOffset < 0) {
            groundBitmap?.let { canvas.drawBitmap(it, xOffset, screenHeight - groundHeight, null) }
        }

        // Draw the platforms
        for (platform in platforms)
        {
            platformBitmap?.let { canvas.drawBitmap(it, platform.left, platform.top, null) }

            // Draw the red outline around the platform (hitbox)
            val outlinePaint = Paint().apply {
                color = Color.RED
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(platform, outlinePaint)
        }

        // Draw the stars
        for (star in stars)
        {
            canvas.drawCircle(star.x, star.y, star.radius, starPaint)
        }

        for(grapple in grapplePoints)
        {
            canvas.drawRect(grapple, grappleReticlePaint)
        }
    }

    fun checkCollision(player: Player): Boolean
    {
        // Check if the player collides with any platform
        for (platform in platforms) {
            val isHorizontallyAligned = player.x + player.size > platform.left && player.x < platform.right

            if (isHorizontallyAligned && RectF.intersects(platform, player.getBoundingRect())) {
                player.handlePlatformCollision(platform)
                return true
            }
        }

        // Check for collisions with stars (allow collection regardless of state)
        val playerCenterX = player.x + player.size / 2
        val playerCenterY = player.y + player.size / 2
        val iterator = stars.iterator()

        while (iterator.hasNext()) {
            val star = iterator.next()
            val dx = star.x - playerCenterX
            val dy = star.y - playerCenterY
            val distance = sqrt((dx * dx + dy * dy).toDouble())

            // Check if the player is close enough to the star to collect it
            if (distance <= star.radius + player.size / 2) {
                iterator.remove()  // Remove the star when collected
                return true
            }
        }

        return false
    }


    // star data class
    data class Star(var x: Float, var y: Float, val radius: Float)

    //Returns what's being grappled
    fun getActiveGrapple(): RectF? = activeGrapple

}
