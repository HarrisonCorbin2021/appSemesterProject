package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appsemesterproject.ui.theme.AppSemesterProjectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppSemesterProjectTheme {
                SettingsScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen() {
        val isLoggedIn = auth.currentUser != null
        val context = LocalContext.current // Use LocalContext to access the context

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TopAppBar with title and Close button
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    IconButton(onClick = {
                        (context as? SettingsActivity)?.finish() // Close the activity
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoggedIn) {
                // Show Save, Load, and Logout buttons
                SaveLoadLogoutButtons(context)
            } else {
                // Show Login button if user is not logged in
                LoginButton(context)
            }
        }
    }

    @Composable
    fun LoginButton(context: android.content.Context) {
        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Login")
        }
    }

    @Composable
    fun SaveLoadLogoutButtons(context: android.content.Context) {
        // Save button
        Button(
            onClick = {
                // Debounce the button clicks to avoid rapid successive clicks
                CoroutineScope(Dispatchers.Main).launch {
                    saveProgress(context)
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Save Progress")
        }

        // Load button
        Button(
            onClick = {
                // Debounce the button clicks to avoid rapid successive clicks
                CoroutineScope(Dispatchers.Main).launch {
                    loadProgress(context)
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Load Progress")
        }

        // Logout button
        Button(
            onClick = {
                // Debounce the button clicks to avoid rapid successive clicks
                CoroutineScope(Dispatchers.Main).launch {
                    auth.signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? SettingsActivity)?.finish() // Close the activity after logout
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Logout")
        }
    }

    private suspend fun saveProgress(context: android.content.Context) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val currentLevel = 5
                val currentScore = 1500
                val playerPosition = mapOf("x" to 100.0f, "y" to 200.0f)

                val gameData = mapOf(
                    "level" to currentLevel,
                    "score" to currentScore,
                    "position" to playerPosition
                )

                // Perform the Firestore save operation on a background thread
                db.collection("users").document(userId).set(gameData).await()

                // Show success message on the main thread
                Toast.makeText(context, "Game progress saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Show failure message on the main thread if something goes wrong
                Toast.makeText(context, "Failed to save progress: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadProgress(context: android.content.Context) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                // Perform the Firestore load operation on a background thread
                val document = db.collection("users").document(userId).get().await()

                if (document.exists()) {
                    val level = document.getLong("level") ?: 0
                    val score = document.getLong("score") ?: 0
                    val position = document.get("position") as? Map<*, *>
                    val x = position?.get("x") as? Float ?: 0f
                    val y = position?.get("y") as? Float ?: 0f

                    // Show loaded data on the main thread
                    Toast.makeText(context, "Loaded: Level $level, Score $score, Position X:$x Y:$y", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Show failure message on the main thread if something goes wrong
                Toast.makeText(context, "Failed to load progress: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
