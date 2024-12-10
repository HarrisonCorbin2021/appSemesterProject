package com.example.appsemesterproject

import android.graphics.Canvas
import android.view.SurfaceHolder
import android.util.Log

//Handles the game loop, calling update and draw on the GameLayer.
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
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    gameLayer.update(player.dx, player.x)
                    gameLayer.draw(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
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

    // Method to stop the game thread
    fun stopThread() {
        running = false
        interrupt()  // Interrupt the thread if it's currently sleeping or waiting
    }
}
