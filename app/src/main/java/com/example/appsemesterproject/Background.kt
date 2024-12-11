package com.example.appsemesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

class Background(context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    private val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sketchvalleybg)
    private var offsetX: Float = 0f
    private val scrollSpeedFactor = 5.0f // Controls the parallax effect speed

    // Store the original width and height of the background image
    private val bitmapWidth = bitmap.width
    private val bitmapHeight = bitmap.height

    fun update(playerSpeed: Float) {
        // Move the background in the opposite direction of the player movement
        offsetX -= playerSpeed * scrollSpeedFactor

        // Instead of abruptly resetting the offset, ensure smooth scrolling in both directions
        if (offsetX <= -bitmapWidth) {
            offsetX += bitmapWidth
        }
        if (offsetX >= bitmapWidth) {
            offsetX -= bitmapWidth
        }
    }

    fun draw(canvas: Canvas) {
        // Draw the background multiple times to fill the screen
        var xPosition = offsetX
        while (xPosition < screenWidth) {
            canvas.drawBitmap(bitmap, xPosition, 0f, null)
            xPosition += bitmapWidth
        }

        // If part of the background is off-screen to the left, we need to draw it again to fill the screen
        if (xPosition < 0) {
            canvas.drawBitmap(bitmap, xPosition + bitmapWidth, 0f, null)
        }
    }
}
