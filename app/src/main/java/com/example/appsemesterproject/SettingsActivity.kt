package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class SettingsActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var playerLevel: Int = 1
    private var playerPositionX: Float = 100f
    private var playerPositionY: Float = 1400f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve game data from Intent
        val intent = intent
        playerLevel = intent.getIntExtra("playerLevel", 1)  // Default to 1 if not passed
        playerPositionX = intent.getFloatExtra("playerX", 100f)  // Default to 100f if not passed
        playerPositionY = intent.getFloatExtra("playerY", 1400f)  // Default to 1400f if not passed

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
        val context = LocalContext.current

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
                // Show Save Game, Logout, and Return to Menu buttons when user is logged in
                LoggedInButtons(context)
            } else {
                // Show Login and Sign Up buttons if user is not logged in
                LoginSignUpButtons(context)
            }
        }
    }

    @Composable
    fun LoginSignUpButtons(context: android.content.Context) {
        // Login button
        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra("sourceActivity", "SettingsActivity")
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Login")
        }

        // Sign Up button
        Button(
            onClick = {
                val intent = Intent(context, SignUpActivity::class.java)
                intent.putExtra("sourceActivity", "SettingsActivity")
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Sign Up")
        }
    }

    @Composable
    fun LoggedInButtons(context: android.content.Context) {
        // Save Game button
        Button(
            onClick = {
                // Save game data to Firebase Firestore
                saveGameData(context)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Save Game")
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

        // Return to Menu button
        Button(
            onClick = {
                // Navigate to the main menu (or main activity)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as? SettingsActivity)?.finish() // Close the settings activity
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Return to Menu")
        }
    }

    // Function to save game data to Firebase Firestore
    private fun saveGameData(context: android.content.Context) {
        Log.d("SettingsActivity", "SettingsActivity: Attempting save...")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Use the player data passed from GameView
            val gameData = mapOf(
                "level" to playerLevel,
                "playerX" to playerPositionX,
                "playerY" to playerPositionY
            )

            val userGameRef = db.collection("users").document(currentUser.uid).collection("games")
                .document("current_game")

            // Check if the game data already exists
            userGameRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // If the document exists, update it
                        userGameRef.update(gameData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Game Updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error updating game: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // If the document does not exist, create it
                        userGameRef.set(gameData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Game Saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error saving game: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error checking game data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
