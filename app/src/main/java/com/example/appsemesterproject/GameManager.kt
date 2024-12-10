package com.example.appsemesterproject

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log

// Data class representing a level
data class Level(
    val act: Int,
    val levelNumber: Int,
    val title: String,
    val backgroundResId: Int,
    val platformImageResId: Int,
    val groundImageResId: Int,
    val platformPos: List<RectF>? = null,
    val bossResId: Int? = null
)

// Define a fixed set of platform positions (adjustable as needed)
private val platformChalkSketch = listOf(
    RectF(200f, 1100f, 600f, 1150f),  // Platform 1
    RectF(800f, 1200f, 1000f, 1250f), // Platform 2
    RectF(1200f, 1300f, 1400f, 1350f), // Platform 3
    RectF(1600f, 1500f, 1800f, 1550f)  // Platform 4
)

class GameManager(private val context: Context, private val gameLayer: GameLayer, private val player: Player, private val gameThread: GameThread) : GameLayer.LevelTransitionListener {

    private val levels = listOf(
        // Act 1
        Level(1, 1, "Chalk Sketch Valley", R.drawable.sketchvalleybg, R.drawable.platformdflat, R.drawable.platforma, platformChalkSketch)
    )

    private var currentLevelIndex = 0
    private var hasPlayerCollidedWithDoor = false // Flag to prevent continuous collision

    init {
        // Set GameManager as the listener to GameLayer
        gameLayer.setLevelTransitionListener(this)
    }

    fun loadLevel(levelIndex: Int) {
        val level = levels[levelIndex]
        currentLevelIndex = levelIndex

        // Load background, ground, and platforms
        val backgroundBitmap = BitmapFactory.decodeResource(context.resources, level.backgroundResId)
        val platformBitmap = BitmapFactory.decodeResource(context.resources, level.platformImageResId)
        val groundBitmap = BitmapFactory.decodeResource(context.resources, level.groundImageResId)

        Log.d("GameManager", "GameManager: backgroundBitmap is set: ${backgroundBitmap != null}")
        Log.d("GameManager", "GameManager: platformBitmap is set: ${platformBitmap != null}")
        Log.d("GameManager", "GameManager: groundBitmap is set: ${groundBitmap != null}")

        gameLayer.setBackground(backgroundBitmap)
        gameLayer.setPlatformImage(platformBitmap)
        gameLayer.setGroundImage(groundBitmap)
    }

    fun nextLevel() {
        if (currentLevelIndex < levels.size - 1) {
            currentLevelIndex++
            loadLevel(currentLevelIndex)
        } else {
            // Game complete logic
            showGameCompleteScreen()
        }
    }

    private fun showGameCompleteScreen() {
        // Create an intent to navigate to the GameCompleteActivity
        val intent = Intent(context, GameCompleteActivity::class.java)
        //intent.putExtra("score", player.score) // Send score data
        intent.putExtra("level", currentLevelIndex) // Send level data
        context.startActivity(intent)
    }

    // Handle player's collision with the door
    fun checkCollisionWithDoor(door: RectF) {
        if (hasPlayerCollidedWithDoor) {
            return // Prevent further collision detection if player already collided
        }

        if (RectF.intersects(player.getBoundingRect(), door)) {
            // Set flag to prevent future collisions
            hasPlayerCollidedWithDoor = true
            // Trigger the game complete screen or next level
            showGameCompleteScreen()
        }
    }

    // Implement the LevelTransitionListener method
    override fun onLevelComplete() {
        // Handle level transition logic here, for example:
        Log.d("GameManager", "Level completed, transitioning to the next level!")
        nextLevel()
    }

    // Reset collision flag when the game is reset or a new level is loaded
    fun resetCollisionFlag() {
        hasPlayerCollidedWithDoor = false
    }
}
