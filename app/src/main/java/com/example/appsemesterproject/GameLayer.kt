package com.example.appsemesterproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Color
import android.util.Log
import kotlin.math.sqrt

class GameLayer(private val screenWidth: Int, private val screenHeight: Int, private val player: Player) {

    // Grapple-able objects or points
    private val grapplePoints = mutableListOf<GrapplePoint>()
    private val grappleReticlePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    // The current grapple target
    private var activeGrapple: GrapplePoint? = null

    interface LevelTransitionListener {
        fun onLevelComplete()
    }

    // Load platform and ground images
    private var backgroundBitmap: Bitmap? = null
    private var platformBitmap: Bitmap? = null
    private var groundBitmap: Bitmap? = null

    private val platforms = mutableListOf<RectF>() // Rectangles representing platforms

    val groundHeight = 300f  // Height of the ground image
    private var groundOffset = 0f  // The current offset of the ground for scrolling

    // Stars for scoring
    private val stars = mutableListOf<Star>()
    private val starRadius = 15f
    private val starPaint = Paint().apply { color = Color.YELLOW }

    // Level boundary
    private val levelWidth = 1000f
    private var door: RectF? = null
    private val doorWidth = 50f
    private val doorHeight = 100f
    private var doorAppeared = false
    private var doorCollided = false  // Flag to track door collision

    private var levelTransitionListener: LevelTransitionListener? = null

    // Set the listener from GameManager or other components
    fun setLevelTransitionListener(listener: LevelTransitionListener) {
        this.levelTransitionListener = listener
    }

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
        platforms.add(RectF(100f, screenHeight - 400f, 300f, screenHeight - 350f))  // Example platform
        platforms.add(RectF(500f, screenHeight - 500f, 700f, screenHeight - 450f))  // Another platform

        //Test grappling points
        grapplePoints.add(GrapplePoint(400f, 1200f))
        grapplePoints.add(GrapplePoint(650f, 1000f))
        grapplePoints.add(GrapplePoint(900f, 1000f))

        // Add stars centered above each platform
        platforms.forEach { platform ->
            val starX = platform.centerX()  // Center the star over the platform
            val starY = platform.top - 30f  // Place the star slightly above the platform
            stars.add(Star(starX, starY, starRadius))
        }

        Log.d("GameLayer", "GameLayer: groundBitmap initialized: ${groundBitmap != null}")
        Log.d("GameLayer", "GameLayer: Drawing background: ${backgroundBitmap != null}")
    }

    fun update(playerSpeed: Float, playerX: Float) {

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
                platform.offset(screenWidth + platform.width(), 0f)
            } else if (platform.left > screenWidth) {
                platform.offset(-screenWidth - platform.width(), 0f)
            }
        }

        // Scroll the ground by updating its offset based on the player's movement
        groundOffset -= playerSpeed

        // Wrap ground tiles when moving left or right
        if (groundBitmap != null) {
            val groundWidth = groundBitmap!!.width.toFloat()
            if (groundOffset <= -groundWidth) {
                groundOffset += groundWidth
            } else if (groundOffset >= groundWidth) {
                groundOffset -= groundWidth
            }
        }

        // Check if the player has passed 1000 pixels and create the door
        if (!doorAppeared && playerX > 1000f) {
            doorAppeared = true
            door = RectF(
                screenWidth - doorWidth - 200,
                screenHeight - groundHeight - doorHeight + 34,
                screenWidth.toFloat() - 200,
                screenHeight - groundHeight + 34
            )
        }

        // Find the closest grapple point within range
        activeGrapple = grapplePoints.minByOrNull { point ->
            val distanceX = point.x - (player.x + player.size / 2)
            val distanceY = point.y - (player.y + player.size / 2)
            val distance = sqrt(distanceX * distanceX + distanceY * distanceY)
            distance // return the distance for comparison
        }?.takeIf { point ->
            val distanceX = point.x - (player.x + player.size / 2)
            val distanceY = point.y - (player.y + player.size / 2)
            val distance = sqrt(distanceX * distanceX + distanceY * distanceY)
            distance <= (point.radius + player.size / 2) * 8 // check if within range
        }
    }


    fun draw(canvas: Canvas) {

        backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        // Offset to adjust the vertical placement of the ground
        val groundYOffset = 50f

        val groundWidth = groundBitmap?.width?.toFloat() ?: 0f
        var xOffset = groundOffset % groundWidth
        if (xOffset > 0) xOffset -= groundWidth

        while (xOffset < screenWidth) {
            groundBitmap?.let {
                canvas.drawBitmap(it, xOffset, screenHeight - groundHeight + groundYOffset, null)
            }
            xOffset += groundWidth
        }

        if (groundOffset < 0) {
            groundBitmap?.let {
                canvas.drawBitmap(it, xOffset, screenHeight - groundHeight + groundYOffset, null)
            }
        }

        door?.let {
            val doorPaint = Paint().apply {
                color = Color.GREEN
            }
            canvas.drawRect(it, doorPaint)
        }

        // Draw the platforms
        for (platform in platforms) {
            platformBitmap?.let {
                val scaledBitmap = Bitmap.createScaledBitmap(
                    it,
                    platform.width().toInt(),
                    platform.height().toInt(),
                    true
                )
                canvas.drawBitmap(scaledBitmap, platform.left, platform.top, null)
            }

        }

        // Draw the stars
        for (star in stars) {
            canvas.drawCircle(star.x, star.y, star.size, starPaint)
        }

        for (grapplePoint in grapplePoints) {
            if (player.isNear(grapplePoint)) {
                canvas.drawCircle(grapplePoint.x, grapplePoint.y, 50f, grappleReticlePaint)
            }
        }
    }

    fun checkCollision(player: Player): Boolean {

        // Check if the player collides with any platform
        for (platform in platforms) {
            val isHorizontallyAligned = player.x + player.size > platform.left && player.x < platform.right

            if (isHorizontallyAligned && RectF.intersects(platform, player.getBoundingRect())) {
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

            if (distance <= star.size + player.size / 2) {
                iterator.remove()
                onStarCollected()
                return true
            }
        }

        // Check for collision with door
        door?.let {
            if (!doorCollided && RectF.intersects(it, player.getBoundingRect())) {
                doorCollided = true
                levelTransitionListener?.onLevelComplete()
                return true
            }
        }

        return false
    }

    // star data class
    data class Star(var x: Float, var y: Float, val size: Float)

    // grapplepoint data class
    data class GrapplePoint(val x: Float, val y: Float, val radius: Float = 50f)

    // Returns what's being grappled
    fun getActiveGrapple(): GrapplePoint? = activeGrapple

    fun getGrapplePoints(): List<GrapplePoint> {
        return grapplePoints
    }

    // Callback for when a star is collected
    private fun onStarCollected() {
        Log.d("GameLayer", "Star collected!")
    }

    private fun onLevelTransition() {
        levelTransitionListener?.onLevelComplete()
    }
}
