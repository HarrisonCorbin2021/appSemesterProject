package com.example.appsemesterproject

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private lateinit var gameLayer: GameLayer
    private lateinit var player: Player

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    init {
        holder.addCallback(this)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        gameLayer.draw(canvas) // Draw platforms, ground, etc.
        player.draw(canvas)    // Draw the player
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        // Initialize game elements
        gameLayer = GameLayer(screenWidth, screenHeight)
        player = Player(
            initialX = 100f,
            initialY = screenHeight - 200f, // Start near the bottom of the screen
            width = 50,
            height = 50,
            screenHeight = screenHeight
        )
        gameLayer.player = player

        // Add example platforms
        gameLayer.addPlatform(200, screenHeight - 300, 400, screenHeight - 250) // Example platform
        gameLayer.addPlatform(500, screenHeight - 400, 700, screenHeight - 350) // Example platform

        // Start the game loop
        gameThread = GameThread(holder, gameLayer)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes (e.g., screen orientation changes)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Stop the game thread safely
        gameThread?.running = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
