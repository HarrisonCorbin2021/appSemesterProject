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
import androidx.compose.runtime.remember
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
        val gameThread = remember { (context as? MainActivity)?.gameThread }  // Retrieve the game thread from MainActivity

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
                text = "Congratulations! You've completed the game!",
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Button to go to the main menu (MainActivity)
            Button(
                onClick = {
                    // Stop the game thread before navigating
                    gameThread?.stopThread()  // Stop the game thread

                    // Start MainActivity
                    val intent = Intent(context, MainActivity::class.java) // Point to MainActivity
                    context.startActivity(intent)

                    // Close the current activity (GameCompleteActivity)
                    (context as? GameCompleteActivity)?.finish()
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Go to Main Menu")
            }
        }
    }

}
