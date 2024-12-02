package com.example.appsemesterproject

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var gameLayer: GameLayer
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        gameLayer = GameLayer(screenWidth, screenHeight)
        gameManager = GameManager(this, gameLayer)

        // Start the first level
        gameManager.loadLevel()
    }
}
