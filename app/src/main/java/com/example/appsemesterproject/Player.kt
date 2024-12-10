package com.example.appsemesterproject

import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.sqrt

class Player(var x: Float, var y: Float) {
    var dx: Float = 0f
    var dy: Float = 0f
    var isInAir: Boolean = false
    var size: Float = 50f

    private var grappling: Boolean = false
    var grappleTarget: PointF? = null
    private val grappleSpeed = 15f

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val screenWidth = displayMetrics.widthPixels
    private val screenHeight = displayMetrics.heightPixels

    fun update(isMovingLeft: Boolean, isMovingRight: Boolean, isJumping: Boolean) {
        // Reset horizontal movement if no direction is pressed
        if (!isMovingLeft && !isMovingRight) {
            dx = 0f
        }

        if (grappling && grappleTarget != null) {
            // Grappling logic remains the same
            val directionX = grappleTarget!!.x - (x + size / 2)
            val directionY = grappleTarget!!.y - (y + size / 2)
            val distance = sqrt(directionX * directionX + directionY * directionY)

            if (distance < grappleSpeed) {
                grappling = false
                grappleTarget = null
            } else {
                dx = directionX / distance * grappleSpeed
                dy = directionY / distance * grappleSpeed
            }
        } else {
            // Normal movement logic
            if (isMovingLeft) dx = -15f
            if (isMovingRight) dx = 15f
            if (isJumping && !isInAir) {
                dy = -15f
                isInAir = true
            }
            dy += 0.5f // Gravity
        }

        // Apply movement
        x += dx
        y += dy

        // Check for landing (collision with ground)
        if (y > screenHeight - 300f - size) {
            y = screenHeight - 300f - size
            dy = 0f
            if (isInAir) {
                isInAir = false
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

    fun grappleTo(target: PointF) {
        grappling = true
        grappleTarget = target
        isInAir = true
    }

    fun isNear(grapplePoint: GameLayer.GrapplePoint, proximityThreshold: Float = 100f): Boolean {
        val pointX = grapplePoint.x
        val pointY = grapplePoint.y
        val dx = pointX - x
        val dy = pointY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= proximityThreshold
    }

    fun getBoundingRect(): RectF = RectF(x, y, x + size, y + size)
}
