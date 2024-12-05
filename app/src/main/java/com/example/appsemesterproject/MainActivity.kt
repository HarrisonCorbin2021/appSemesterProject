package com.example.appsemesterproject

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var gameLayer: GameLayer
    private lateinit var gameThread: GameThread
    private lateinit var surfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SurfaceView
        surfaceView = SurfaceView(this)
        val surfaceHolder = surfaceView.holder

        // Screen dimensions
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Initialize GameLayer
        gameLayer = GameLayer(screenWidth, screenHeight)

        // Set SurfaceView as content view
        setContentView(surfaceView)

        // Initialize GameThread
        gameThread = GameThread(surfaceHolder, gameLayer)

        // Manage the lifecycle of the SurfaceHolder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                gameThread.running = true
                gameThread.start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                gameThread.running = false
                try {
                    gameThread.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
