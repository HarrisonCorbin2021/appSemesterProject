package com.example.appsemesterproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.example.appsemesterproject.ui.theme.AppSemesterProjectTheme

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            AppSemesterProjectTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        val email = remember { androidx.compose.runtime.mutableStateOf("") }
        val password = remember { androidx.compose.runtime.mutableStateOf("") }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Login", color = Color.Black, modifier = Modifier.padding(bottom = 24.dp))

                TextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    modifier = Modifier.padding(bottom = 16.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        loginUser(email.value, password.value)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Login")
                }

                Button(
                    onClick = {
                        navigateToSignUp()
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Create Account")
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}
