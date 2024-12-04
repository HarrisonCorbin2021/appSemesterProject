package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the custom GameView as the content view
        gameView = GameView(this)

        // Use a Box to layer Compose UI on top of the game
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { gameView },
                    modifier = Modifier.fillMaxSize()
                )

                // Icon button in the top-left corner
                IconButton(
                    onClick = { openSettingsActivity() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    // Use the ic_menu drawable resource as the button image
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu), // Replace with your drawable resource
                        contentDescription = "Settings Menu",
                        modifier = Modifier.size(40.dp) // Adjust size as needed
                    )
                }
            }
        }
    }

    private fun openSettingsActivity() {
        // Stop the game before switching to settings
        stopGame()

        // Intent to open SettingsActivity
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        stopGame() // Ensure game is stopped when the activity goes into the background
    }

    override fun onResume() {
        super.onResume()
        if (!gameView.isRunning) {
            startGame() // Restart the game when coming back to the activity
        }
    }

    fun startGame() {
        gameView.startGame()  // Start the game thread
    }

    fun stopGame() {
        gameView.stopGame()  // Stop the game thread
    }
}
