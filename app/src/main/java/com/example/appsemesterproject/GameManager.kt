package com.example.appsemesterproject

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.RectF

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


// GameManager handles level progression and theme setup
class GameManager(private val context: Context, private val gameLayer: GameLayer) {

    private val levels = listOf(
        // Act 1
        Level(1, 1, "Chalk Sketch Valley", R.drawable.sketchvalleybg, R.drawable.platformb, R.drawable.platforma, platformChalkSketch),
//        Level(1, 2, "Galactic Gold Mine", R.drawable.cave_bg, R.drawable.cave_platform, R.drawable.cave_ground),
//        Level(1, 3, "Skyview City", R.drawable.waterfall_bg, R.drawable.waterfall_platform, R.drawable.waterfall_ground, R.drawable.act1_boss),

        // Act 2
//        Level(2, 1, "Light City", R.drawable.desert_bg, R.drawable.desert_platform, R.drawable.desert_ground),
//        Level(2, 2, "The Lightning Factory", R.drawable.ruins_bg, R.drawable.ruins_platform, R.drawable.ruins_ground),
//        Level(2, 3, "Crystal Cove", R.drawable.volcano_bg, R.drawable.volcano_platform, R.drawable.volcano_ground, R.drawable.act2_boss),

        // Act 3
//        Level(3, 1, "The Rosemarian Kingdom", R.drawable.sky_bg, R.drawable.sky_platform, R.drawable.sky_ground),
//        Level(3, 2, "Campus Valley", R.drawable.space_bg, R.drawable.space_platform, R.drawable.space_ground),
//        Level(3, 3, "Sunset City", R.drawable.fortress_bg, R.drawable.fortress_platform, R.drawable.fortress_ground, R.drawable.act3_boss)
    )

    private var currentLevelIndex = 0

    fun loadLevel() {
        val level = levels[currentLevelIndex]

        // Load background, ground, and platforms
        val backgroundBitmap = BitmapFactory.decodeResource(context.resources, level.backgroundResId)
        val platformBitmap = BitmapFactory.decodeResource(context.resources, level.platformImageResId)
        val groundBitmap = BitmapFactory.decodeResource(context.resources, level.groundImageResId)

        gameLayer.setBackground(backgroundBitmap)
        gameLayer.setPlatformImage(platformBitmap)
        gameLayer.setGroundImage(groundBitmap)

        // Load the boss if the level has one
//        level.bossResId?.let {
//            val bossBitmap = BitmapFactory.decodeResource(context.resources, it)
//            gameLayer.setBoss(bossBitmap)
//        } ?: gameLayer.removeBoss()

        // Apply level-specific mechanics
        //applyThemeMechanics(level.theme)
    }

    fun nextLevel() {
        if (currentLevelIndex < levels.size - 1) {
            currentLevelIndex++
            loadLevel()
        } else {
            // Game complete logic
            showGameCompleteScreen()
        }
    }

//    private fun applyThemeMechanics(theme: String) {
//        when (title) {
//            //"Final Boss" -> gameLayer.enableFinalBossMechanics()
//        }
//    }

    private fun showGameCompleteScreen() {
        // Display "Game Complete" UI or transition to the main menu
        println("Congratulations! You've completed the game!")
    }
}