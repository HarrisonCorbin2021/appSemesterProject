package com.example.appsemesterproject

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var gameLayer: GameLayer
    private lateinit var gameThread: GameThread
    private lateinit var surfaceView: SurfaceView
    private lateinit var gameManager: GameManager
    private lateinit var gameView: GameView
    private lateinit var player: Player

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

        // Initialize GameManager and pass the gameLayer
        gameManager = GameManager(this, gameLayer)

        // Load the first level
        gameManager.loadLevel()

        //Initialize the player
        player = Player(0f,0f)

        // Initialize GameView and pass the gameLayer to it
        gameView = GameView(this, gameLayer)  // Pass the GameLayer to GameView

        // Set SurfaceView as content view
        setContentView(gameView)

        // Initialize GameThread
        gameThread = GameThread(surfaceHolder, gameLayer, player)

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

    override fun onPause() {
        super.onPause()
        gameView.stopGame()  // Stop the game when the activity is paused
    }

    override fun onResume() {
        super.onResume()
        gameView.startGame()  // Restart the game when the activity is resumed
    }
}
