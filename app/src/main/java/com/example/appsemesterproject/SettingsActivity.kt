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
                // Show Logout button when user is logged in
                LogoutButton(context)
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
    fun LogoutButton(context: android.content.Context) {
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
}
