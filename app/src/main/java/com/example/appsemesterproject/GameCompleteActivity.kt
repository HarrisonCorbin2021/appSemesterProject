package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.appsemesterproject.ui.theme.AppSemesterProjectTheme

class GameCompleteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppSemesterProjectTheme {
                GameCompleteScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GameCompleteScreen() {
        val context = LocalContext.current
        val score = intent.getIntExtra("score", 0) // Retrieve score
        val level = intent.getIntExtra("level", 0) // Retrieve level

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TopAppBar with title
            TopAppBar(
                title = { Text("Game Complete") },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display message for game completion
            Text(
                text = "Congratulations! You've completed the game! Score: $score, Level: $level",
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Button to go to the main menu (MainActivity)
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
            {
                Text("Go to Main Menu")
            }
        }
    }
}
