package com.example.appsemesterproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect

class Background(context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    private val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.skybox)
    private var offsetX: Float = 0f
    private val scrollSpeedFactor = 5.0f // Controls the parallax effect speed

    // Scale the background to fit the screen
    private val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true)

    fun update(playerSpeed: Float) {
        // Move the background in the opposite direction of the player movement
        offsetX -= playerSpeed * scrollSpeedFactor

        // Reset the offset to create a seamless scrolling effect
        if (offsetX <= -screenWidth) {
            offsetX += screenWidth
        }
    }

    fun draw(canvas: Canvas) {
        // Draw the background twice for seamless scrolling
        canvas.drawBitmap(scaledBitmap, offsetX, 0f, null)
        canvas.drawBitmap(scaledBitmap, offsetX + screenWidth, 0f, null)
    }
}
