package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appsemesterproject.ui.theme.AppSemesterProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    public lateinit var gameLayer: GameLayer
    public lateinit var gameThread: GameThread
    public lateinit var surfaceView: SurfaceView
    public lateinit var gameManager: GameManager
    public lateinit var gameView: GameView
    public lateinit var player: Player

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference  // Initialize database reference

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val player = Player(100f, 1400f)
        gameLayer = GameLayer(screenWidth, screenHeight, player)
        gameView = GameView(this, gameLayer, player)

        setContent {
            AppSemesterProjectTheme {
                if (auth.currentUser == null) {
                    MainMenuScreen() // Show main menu when not logged in
                } else {
                    GameMenuScreen() // Show game options after login
                }
            }
        }
    }

    // Main Menu Composable
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainMenuScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = { Text("Main Menu") }
            )

            Button(
                onClick = { navigateToLogin() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Login")
            }

            Button(
                onClick = { navigateToSignUp() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Sign Up")
            }

            Button(
                onClick = { startGame() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Start Game")
            }
        }
    }

    // Game Menu after login
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GameMenuScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = { Text("Game Menu") }
            )

            Button(
                onClick = { startGame() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("New Game")
            }

            Button(
                onClick = { loadGame() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Load Game")
            }

            Button(
                onClick = { signOutUser() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Logout")
            }
        }
    }

    // Navigate to Settings screen
    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    // Start the game
    private fun startGame() {
        Log.d("GameStart", "Starting a new game...")
        surfaceView = SurfaceView(this)
        val surfaceHolder = surfaceView.holder

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val player = Player(100f, 1400f)
        player.update(false, false, false)
        gameLayer = GameLayer(screenWidth, screenHeight, player)

        gameManager = GameManager(this, gameLayer, player)
        gameManager.loadLevel(0)

        gameView = GameView(this, gameLayer, player)


        setContentView(gameView)

        // Initialize GameThread
        gameThread = GameThread(surfaceHolder, gameLayer, player)

        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d("GameStart", "Game surface created, starting game thread.")
                gameThread.running = true
                gameThread.start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("GameStart", "Game surface destroyed, stopping game thread.")
                gameThread.running = false
                try {
                    gameThread.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
    }

    // Load saved game from Firebase
    private fun loadGame() {
        Log.d("GameLoad", "Attempting to load saved game...")
        val userId = auth.currentUser?.uid ?: return

        // Retrieve saved game data from Firebase Realtime Database
        database.child("users").child(userId).child("savedGame").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Get the saved game data from the snapshot
                val savedLevel = snapshot.child("level").value as? Int ?: 1
                val savedPlayerPositionX = snapshot.child("playerX").value as? Float ?: 100f
                val savedPlayerPositionY = snapshot.child("playerY").value as? Float ?: 1400f

                Log.d("GameLoad", "Loaded saved game: Level $savedLevel, Player Position: ($savedPlayerPositionX, $savedPlayerPositionY)")

                // Create a new player with saved position
                val player = Player(savedPlayerPositionX, savedPlayerPositionY)
                gameLayer = GameLayer(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, player)

                gameManager = GameManager(this, gameLayer, player)
                gameManager.loadLevel(savedLevel)  // Pass saved level

                gameView = GameView(this, gameLayer, player)

                setContentView(gameView)

                gameThread = GameThread(gameView.holder, gameLayer, player)

                gameView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        Log.d("GameLoad", "Game surface created, starting game thread.")
                        gameThread.running = true
                        gameThread.start()
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        Log.d("GameLoad", "Game surface destroyed, stopping game thread.")
                        gameThread.running = false
                        try {
                            gameThread.join()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                })

                Toast.makeText(this, "Game Loaded", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("GameLoad", "No saved game found.")
                Toast.makeText(this, "No saved game found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d("GameLoad", "Failed to load game: ${it.message}")
            Toast.makeText(this, "Failed to load game: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Sign out user
    private fun signOutUser() {
        auth.signOut()
        setContent {
            AppSemesterProjectTheme {
                MainMenuScreen() // Show main menu after logging out
            }
        }
    }

    // Navigate to login screen
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("sourceActivity", "MainActivity")
        startActivityForResult(intent, 1)
    }

    // Navigate to sign-up screen
    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        intent.putExtra("sourceActivity", "MainActivity")
        startActivityForResult(intent, 1)
    }

    // Handle result from login/sign up
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            setContent {
                AppSemesterProjectTheme {
                    GameMenuScreen() // Show game menu after login/signup
                }
            }
        }
    }
}
