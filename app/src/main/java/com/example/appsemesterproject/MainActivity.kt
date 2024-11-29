package com.example.appsemesterproject

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the custom GameView as the content view
        val gameView = GameView(this)
        setContentView(gameView)
    }
}
