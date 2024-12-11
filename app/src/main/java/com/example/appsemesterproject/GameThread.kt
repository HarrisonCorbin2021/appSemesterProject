package com.example.appsemesterproject

import android.graphics.Canvas
import android.view.SurfaceHolder
import android.util.Log

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameLayer: GameLayer,
    private val player: Player
) : Thread() {

    var running: Boolean = true
    private var lastTime: Long = System.currentTimeMillis()

    override fun run() {
        Log.d("GameThread", "GameThread: gameLayer initialized: ${gameLayer != null}")
        while (running) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastTime) / 1000f
            lastTime = currentTime

            var canvas: Canvas? = null
            try {
                // Check if the surface is still valid before locking
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameLayer.update(player.dx, player.x)
                        gameLayer.draw(canvas)
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas) // Only unlock if canvas is valid
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Additional safety check if canvas wasn't locked properly
                canvas?.let {
                    surfaceHolder.unlockCanvasAndPost(it)
                }
            }

            // Throttle the loop to 60 FPS (16 ms per frame)
            try {
                sleep(16)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    // Stop the thread safely
    fun stopThread() {
        running = false
        interrupt()  // Interrupt the thread if it’s currently sleeping or waiting
    }
}
