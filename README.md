
---

# **Building an Android Game with Smooth Scrolling and Game Loop**

This tutorial will guide you through building a simple Android game featuring a smooth scrolling background with a parallax effect, a custom game loop, and game logic. By following this guide, you will learn how to handle `SurfaceView` rendering, manage background scrolling, and create an efficient game loop for animation and updates.

## **Demo App Overview**

The demo app created in this tutorial includes the following features:

- **Parallax Scrolling Background**: A smooth scrolling background with a parallax effect, where multiple background layers scroll at different speeds.
- **Game Loop**: A custom game loop to continuously update the game state and redraw the view.
- **Game Elements**: Manage the game’s background and player object on the screen.

Here’s an overview of the app's expected functionality:

- **Background**: The background scrolls continuously, and we implement a parallax effect with multiple layers.
- **Player**: A player object is controlled by simple input or automatically moves within the game.
- **Game Loop**: The custom game loop is responsible for constantly updating and rendering the game state.

---

## **Getting Started**

To complete this tutorial, you will need the following:

### **Prerequisites**

1. **Android Studio** (version 2023.1 or higher)  
   Download from [here](https://developer.android.com/studio).
2. **Kotlin** (included with Android Studio)
3. **Android SDK** (included with Android Studio)

### **Setting Up the Project**

1. Open **Android Studio**.
2. Create a new project with an empty activity.
3. Choose **Kotlin** as the programming language and **API 21** or higher as the minimum SDK.
4. Name your project (e.g., "Game Tutorial").

### **Dependencies**

For this tutorial, we use **Jetpack Compose** for UI rendering along with standard Android SDK components.

You can add the following dependencies to your `build.gradle` file if they aren't already present:

```gradle
dependencies {
    implementation "androidx.compose.ui:ui:1.5.0"
    implementation "androidx.compose.material:material:1.5.0"
    implementation "androidx.compose.ui:ui-tooling-preview:1.5.0"
    implementation "androidx.activity:activity-compose:1.6.0"
}
```

---

## **Step-by-Step Instructions**

Follow the instructions below to implement the game. Each section provides code snippets and explanations on how to integrate them into your project.

---

### **Managing the Game View (`GameView.kt`)**

The `GameView` class extends `SurfaceView` and handles the game rendering and user input. It updates and draws the game content, including the player, background, control buttons, and other game elements. It also responds to touch and key events for player movement and actions.

#### **1. Class Initialization**

The class initializes several properties, such as the game screen dimensions, background, control buttons, and movement flags. It also sets up the `SurfaceHolder.Callback` to handle the creation and destruction of the `SurfaceView`.

```kotlin
// Define screen width and height
private val displayMetrics = Resources.getSystem().displayMetrics
private val screenWidth = displayMetrics.widthPixels
private val screenHeight = displayMetrics.heightPixels
private val background = Background(context, screenWidth, screenHeight)
```

**Explanation**: The `GameView` uses the screen's width and height to correctly position game elements and control buttons. The `Background` class is initialized here, which will manage the scrolling background.

#### **2. Handling Game Loop (`startGame()`, `run()`, `stopGame()`)**

The game loop is started when the surface is created, and it continuously updates and draws the game content in a separate thread. The `isRunning` flag controls whether the game loop is active.

```kotlin
fun startGame() {
    if (!isRunning) {
        isRunning = true
        thread = Thread(this)
        thread.start()
    }
}

override fun run() {
    while (isRunning) {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                try {
                    update()
                    drawGame(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}
```

**Explanation**: The game loop runs in the background thread and constantly updates the game state and renders it to the screen. The `run()` method locks the canvas, updates the game elements, and then unlocks the canvas to display the changes.

#### **3. Handling User Input**

The `GameView` class supports both touch events (for mobile controls) and key events (for physical keyboard input). It allows the player to move left, right, and jump using on-screen buttons or keyboard keys.

```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x
    val y = event.y

    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            isMovingLeft = x < leftButton.right && x > leftButton.left && y < leftButton.bottom && y > leftButton.top
            isMovingRight = x < rightButton.right && x > rightButton.left && y < rightButton.bottom && y > rightButton.top
            if (x < jumpButton.right && x > jumpButton.left && y < jumpButton.bottom && y > jumpButton.top) {
                isJumping = !player.isInAir  // Set jumping if player is not already in the air
            }
        }
        MotionEvent.ACTION_UP -> {
            isMovingLeft = false
            isMovingRight = false
            isJumping = false
        }
    }
    return true
}
```

**Explanation**: The touch event listener detects where the user taps on the screen. It checks whether the tap occurred within the defined button areas (left, right, jump). It also handles the jump action when the player is not in the air.

#### **4. Updating and Drawing Game Elements**

The `update()` method updates the player's position, handles movement, applies gravity, and checks for collisions with platforms and obstacles. The `drawGame()` method draws all game elements, including the background, platforms, player, and control buttons.

```kotlin
private fun update() {
    // Update player, background, and game layer
    player.update(isMovingLeft, isMovingRight, isJumping)
    background.update(player.dx)  // Update background based on player's movement
    gameLayer.update(player.dx, player.x)  // Update game elements
}

private fun drawGame(canvas: Canvas) {
    // Draw the background, player, platforms, and control buttons
    gameLayer.draw(canvas)
    canvas.drawRect(player.x, player.y, player.x + player.size, player.y + player.size, paint)  // Draw player
}
```

**Explanation**: The `update()` method updates the player’s state based on movement inputs (left, right, jump). It also updates the background and other game elements. The `drawGame()` method renders the player, background, platforms, and other elements like control buttons and the ground line.

#### **5. Navigating to Settings**

The `GameView` includes a settings button (gear icon) that, when clicked, navigates to a settings screen. The player’s data (like position and level) is passed through an intent to the settings activity.

```kotlin
private fun navigateToSettings() {
    val intent = Intent(context, SettingsActivity::class.java)
    intent.putExtra("playerX", player.x)
    intent.putExtra("playerY", player.y)
    context.startActivity(intent)
}
```

**Explanation**: The `navigateToSettings()` function is triggered when the settings button is pressed. It passes the player's position to the `SettingsActivity` using an `Intent`.

---

### **Conclusion**

The `GameView` class is responsible for managing the game’s display and user interactions. It handles:
- **Game Loop**: Continuously updates and draws the game in a separate thread.
- **User Input**: Supports both touch and key events to control the player’s movement and actions.
- **Game Elements**: Updates and draws the player, background, platforms, and control buttons.
- **Navigation**: Handles navigation to the settings screen, passing player data along.

By understanding how each part of this class works, you can customize the game’s input, control layout, and rendering logic to fit your needs.

---

### **Managing the Game Loop (`GameThread.kt`)**

The `GameThread` class manages the game loop, which is responsible for continuously updating the game state and rendering the game at a fixed frame rate. It operates in a separate thread, ensuring that the game runs independently of the main UI thread.

#### **1. Thread Initialization**

The thread is initialized with the `SurfaceHolder`, `GameLayer`, and `Player` objects. These are essential for drawing and updating the game content.

```kotlin
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameLayer: GameLayer,
    private val player: Player
) : Thread() {
    var running: Boolean = true
    private var lastTime: Long = System.currentTimeMillis()
}
```

**Explanation**: The `GameThread` takes the `SurfaceHolder` to handle canvas drawing, the `GameLayer` to update and draw the game elements, and the `Player` to update player-specific actions. The `running` flag controls whether the thread continues running.

#### **2. Game Loop (`run()`)**

The `run()` method contains the game loop, which is responsible for continuously updating and drawing the game. The loop calculates the time between frames (`deltaTime`) and ensures that the game runs at a consistent speed.

```kotlin
override fun run() {
    Log.d("GameThread", "GameThread: gameLayer initialized: ${gameLayer != null}")
    while (running) {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 1000f
        lastTime = currentTime

        var canvas: Canvas? = null
        try {
            // Lock the canvas to start drawing
            canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                synchronized(surfaceHolder) {
                    gameLayer.update(player.dx, player.x)  // Update game layer
                    gameLayer.draw(canvas)  // Draw game elements to the canvas
                }
                surfaceHolder.unlockCanvasAndPost(canvas)  // Unlock the canvas and post the changes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            canvas?.let {
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }

        // Throttle the loop to 60 FPS (16 ms per frame)
        try {
            sleep(16)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
```

**Explanation**: The `run()` method contains the main game loop, which continuously updates and draws the game state:
- The `deltaTime` is calculated to manage frame timing and smooth updates.
- The `canvas` is locked for drawing the game elements, and once drawing is completed, the canvas is unlocked and posted back to the screen.
- The loop is throttled to 60 frames per second (FPS), which corresponds to approximately 16 milliseconds per frame (`sleep(16)`).

#### **3. Stopping the Game Loop (`stopThread()`)**

The `stopThread()` method safely stops the game loop by setting the `running` flag to `false` and interrupting the thread if it’s waiting or sleeping.

```kotlin
fun stopThread() {
    running = false
    interrupt()  // Interrupt the thread if it’s currently sleeping or waiting
}
```

**Explanation**: When the game needs to stop (e.g., when the activity is paused or finished), this method sets `running` to `false`, which stops the game loop, and interrupts the thread if it’s in a sleep state. This ensures that the thread stops safely.

---

### **Conclusion**

The `GameThread` class is a critical part of the game's performance. It manages the game loop, continuously updating and drawing the game state while ensuring smooth frame rendering at a fixed FPS. By running the game loop in a separate thread, the game remains responsive and smooth, independent of the main UI thread. This class also includes methods to stop the thread safely when needed, such as when the game is paused or the activity is destroyed.

---

### **Managing the Background (`Background.kt`)**

The `Background` class is responsible for handling the scrolling background effect, including the parallax effect and the movement of the background in response to player movement. It ensures that the background scrolls smoothly and seamlessly across the screen.

#### **1. Initialization and Background

Setup**

The background bitmap is loaded, and variables such as `offsetX`, `bitmapWidth`, and `bitmapHeight` are initialized. `offsetX` controls the horizontal scrolling of the background, and `scrollSpeedFactor` determines how fast the background scrolls relative to the player's movement.

```kotlin
class Background(context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    private val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sketchvalleybg)
    private var offsetX: Float = 0f
    private val scrollSpeedFactor = 5.0f // Controls the parallax effect speed

    // Store the original width and height of the background image
    private val bitmapWidth = bitmap.width
    private val bitmapHeight = bitmap.height
}
```

**Explanation**: The background image (`sketchvalleybg`) is loaded, and the `offsetX` is set to 0, meaning the background starts at its initial position. `scrollSpeedFactor` controls how quickly the background moves relative to the player's speed, enabling the parallax effect.

#### **2. Background Update (`update()`)**

The `update()` method updates the background’s position based on the player's speed. It moves the background in the opposite direction of the player’s movement and ensures that the background scrolls smoothly.

```kotlin
fun update(playerSpeed: Float) {
    // Move the background in the opposite direction of the player movement
    offsetX -= playerSpeed * scrollSpeedFactor

    // Instead of abruptly resetting the offset, ensure smooth scrolling in both directions
    if (offsetX <= -bitmapWidth) {
        offsetX += bitmapWidth
    }
    if (offsetX >= bitmapWidth) {
        offsetX -= bitmapWidth
    }
}
```

**Explanation**: The background’s horizontal position (`offsetX`) is updated based on the player's movement. The background scrolls in the opposite direction of the player's speed. To create a seamless effect, when the background moves off-screen, it is repositioned to continue scrolling without interruption.

#### **3. Drawing the Background (`draw()`)**

The `draw()` method draws the background multiple times across the screen to ensure that it fills the entire view. It also ensures that the background seamlessly wraps when scrolling.

```kotlin
fun draw(canvas: Canvas) {
    // Draw the background multiple times to fill the screen
    var xPosition = offsetX
    while (xPosition < screenWidth) {
        canvas.drawBitmap(bitmap, xPosition, 0f, null)
        xPosition += bitmapWidth
    }

    // If part of the background is off-screen to the left, we need to draw it again to fill the screen
    if (xPosition < 0) {
        canvas.drawBitmap(bitmap, xPosition + bitmapWidth, 0f, null)
    }
}
```

**Explanation**: The background is drawn repeatedly to fill the entire screen width. The `xPosition` variable controls where each instance of the background is drawn. If part of the background moves off-screen to the left, it is drawn again on the right side to create a seamless scrolling effect.

---

### **Conclusion**

The `Background` class is responsible for creating a scrolling background effect, which is a key component in achieving the parallax effect. It smoothly updates the background’s position based on the player's speed and ensures that the background continuously scrolls without any gaps. This class plays a critical role in the visual flow of the game, making the environment feel dynamic and responsive.

By using this approach, you can easily adjust the scrolling speed and implement more complex background effects as needed.

--- 

### **Managing the Game Layer (`GameLayer.kt`)**

The `GameLayer` class is responsible for managing all the game elements, including platforms, background, stars, grapple points, and the player’s interactions with these elements. It handles the game’s visual layer, updating and drawing objects, and checking for collisions.

#### **1. Initialization and Game Elements Setup**

The `GameLayer` is initialized with the screen width and height and the `Player` object. It sets up the background, platforms, stars, grapple points, and door. It also handles the resizing of images and the creation of game objects like platforms and stars.

```kotlin
// Initialize game elements
private val grapplePoints = mutableListOf<GrapplePoint>()
private val platforms = mutableListOf<RectF>() // Rectangles representing platforms
private val stars = mutableListOf<Star>()
private var door: RectF? = null
```

**Explanation**: The game elements like platforms, stars, and grapple points are initialized in the constructor. Platforms are defined using `RectF` objects, and each star is positioned above a platform.

#### **2. Game Update (`update()`)**

The `update()` method handles the movement of platforms, stars, and the ground, along with the player’s interactions (e.g., scrolling background and platform reset). It also checks for door appearance after the player reaches a certain point.

```kotlin
fun update(playerSpeed: Float, playerX: Float) {
    // Move platforms and stars based on the player's speed
    platforms.forEach { platform -> platform.offset(-playerSpeed, 0f) }
    stars.forEach { star -> star.x -= playerSpeed }

    // Scroll ground based on player’s speed
    groundOffset -= playerSpeed
    if (groundOffset <= -groundWidth) {
        groundOffset += groundWidth
    }

    // Check for door appearance after player moves 1000 pixels
    if (!doorAppeared && playerX > 1000f) {
        doorAppeared = true
        door = RectF(screenWidth - doorWidth - 200, screenHeight - groundHeight - doorHeight + 34, screenWidth.toFloat() - 200, screenHeight - groundHeight + 34)
    }
}
```

**Explanation**: This method handles the movement of the background, platforms, and stars. It also checks for the appearance of the door once the player moves past a certain threshold.

#### **3. Drawing Game Elements (`draw()`)**

The `draw()` method renders the game elements on the screen. It includes the background, platforms, stars, and player, as well as special elements like the grapple points and the door.

```kotlin
fun draw(canvas: Canvas) {
    backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

    // Draw platforms
    platforms.forEach { platform ->
        platformBitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(it, platform.width().toInt(), platform.height().toInt(), true)
            canvas.drawBitmap(scaledBitmap, platform.left, platform.top, null)
        }
    }

    // Draw the stars
    stars.forEach { star -> canvas.drawCircle(star.x, star.y, star.size, starPaint) }

    // Draw the grapple points
    grapplePoints.forEach { point ->
        if (player.isNear(point)) {
            canvas.drawCircle(point.x, point.y, 50f, grappleReticlePaint)
        }
    }
}
```

**Explanation**: The method draws each game element (background, platforms, stars) to the canvas. It also draws grapple points with a reticle if the player is near them. The ground and door are drawn based on their respective positions.

#### **4. Collision Detection (`checkCollision()`)**

The `checkCollision()` method detects collisions between the player and various game elements, including platforms, stars, and the door. It handles platform collisions by checking if the player’s bounding box intersects with any platform. It also checks for star collection and door collision.

```kotlin
fun checkCollision(player: Player): Boolean {
    // Check collision with platforms
    platforms.forEach { platform ->
        if (RectF.intersects(platform, player.getBoundingRect())) {
            player.handlePlatformCollision(platform)
            return true
        }
    }

    // Check collision with stars
    val playerCenterX = player.x + player.size / 2
    val playerCenterY = player.y + player.size / 2
    stars.forEach { star ->
        val distance = sqrt((star.x - playerCenterX).pow(2) + (star.y - playerCenterY).pow(2))
        if (distance <= star.size + player.size / 2) {
            onStarCollected()
            return true
        }
    }

    // Check collision with door
    door?.let {
        if (RectF.intersects(it, player.getBoundingRect())) {
            levelTransitionListener?.onLevelComplete()
            return true
        }
    }

    return false
}
```

**Explanation**: The method checks for collisions between the player and platforms, stars, and the door. If a collision with a platform is detected, it handles the collision. If a star is collected, it triggers the star collection logic. The door collision triggers the level completion.

#### **5. Game Elements: Grapple Points and Stars**

Grapple points allow the player to interact with certain areas in the game using a grappling hook. The stars are collectible objects scattered above platforms to add scoring or gameplay elements.

```kotlin
// GrapplePoint data class
data class GrapplePoint(val x: Float, val y: Float, val radius: Float = 50f)

// Star data class
data class Star(var x: Float, var y: Float, val size: Float)
```

**Explanation**: Grapple points and stars are used in the game. Grapple points are points where the player can grapple, and stars are collectible items used for scoring or game progression.

#### **6. Level Transition (`onLevelTransition()`)**

The `onLevelTransition()` method handles transitioning between levels once the player reaches the door.

```kotlin
private fun onLevelTransition() {
    levelTransitionListener?.onLevelComplete()
}
```

**Explanation**: This method is triggered when the player collides with the door, signaling the completion of the current level and transitioning to the next.

---

### **Conclusion**

The `GameLayer` class is responsible for managing the core game elements, including platforms, background, stars, grapple points, and the player's interactions with these elements. It updates the game state, handles player movement and collisions, and draws the game scene on the screen. This class integrates the visual elements of the game with the player's actions and triggers the level transition when necessary.

By understanding how this class functions, you can modify or extend the gameplay, such as adding new platforms, stars, or grapple points, and adjusting collision logic as needed.

---

### **Managing Game Progression and Level Transitions (`GameManager.kt`)**

The `GameManager` class handles game progression, including loading levels, detecting player collisions with the door, and transitioning between levels. It also manages the game flow, such as progressing to the next level or completing the game.

#### **1. Level Data and Initialization**

The class defines a `Level` data class that holds information about each level, including background, platform, and ground images. It also defines the positions of platforms and the door's location.

```kotlin
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

private val platformChalkSketch = listOf(
    RectF(200f, 1100f, 600f, 1150f),  // Platform 1
    RectF(800f, 1200f, 1000f, 1250f), // Platform 2
    RectF(1200f, 1300f, 1400f, 1350f), // Platform 3
    RectF(1600f, 1500f, 1800f, 1550f)  // Platform 4
)
```

**Explanation**: The `Level` data class defines each level's properties, including the level’s title, background, and platform images. Platforms are defined using `RectF` objects to specify their positions.

#### **2. Loading the Current Level (`loadLevel()`)**

The `loadLevel()` method loads the assets for the current level, such as the background, platform, and ground images, and updates the game layer with these assets.

```kotlin
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

    // Set the images in the game layer
    gameLayer.setBackground(backgroundBitmap)
    gameLayer.setPlatformImage(platformBitmap)
    gameLayer.setGroundImage(groundBitmap)
}
```

**Explanation**: This method loads the images for the current level based on the `Level` data class and updates the `gameLayer` with the appropriate background, platform, and ground images.

#### **3. Level Transition (`nextLevel()`)**

The `nextLevel()` method transitions to the next level. If there are no more levels, it triggers the game complete screen.

```kotlin
fun nextLevel() {
    if (currentLevelIndex < levels.size - 1) {
        currentLevelIndex++
        loadLevel(currentLevelIndex)
    } else {
        // Game complete logic
        showGameCompleteScreen()
    }
}
```

**Explanation**: This method checks if there are more levels to load. If so, it increments the `currentLevelIndex` and loads the next level. If no more levels are available, it triggers the game completion.

#### **4. Handling Game Completion (`showGameCompleteScreen()`)**

The `showGameCompleteScreen()` method navigates the player to the game complete screen, passing the current level data.

```kotlin
private fun showGameCompleteScreen() {
    // Create an intent to navigate to the GameCompleteActivity
    val intent = Intent(context, GameCompleteActivity::class.java)
    intent.putExtra("level", currentLevelIndex) // Send level data
    context.startActivity(intent)
}
```

**Explanation**: Once the game is completed (either by finishing all levels or colliding with the door), this method creates an `Intent` to navigate to the `GameCompleteActivity` and passes the current level data.

#### **5. Collision Detection with the Door (`checkCollisionWithDoor()`)**

This method detects when the player collides with the door, which signifies the completion of the level.

```kotlin
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
```

**Explanation**: The method checks if the player collides with the door. If a collision is detected, it triggers the game completion process by calling `showGameCompleteScreen()`.

#### **6. Level Transition Listener**

The `GameManager` implements the `LevelTransitionListener` interface, which listens for level completion events and handles the transition to the next level.

```kotlin
// Implement the LevelTransitionListener method
override fun onLevelComplete() {
    // Handle level transition logic here
    Log.d("GameManager", "Level completed, transitioning to the next level!")
    nextLevel()
}
```

**Explanation**: This method is triggered when the level is complete, and it calls `nextLevel()` to proceed to the next level.

#### **7. Resetting the Collision Flag**

The `resetCollisionFlag()` method resets the collision flag when the game is reset or when a new level is loaded.

```kotlin
fun resetCollisionFlag() {
    hasPlayerCollidedWithDoor = false
}
```

**Explanation**: This method is used to reset the collision flag, preventing the game from mistakenly detecting a previous door collision after transitioning to a new level.

---

### **Conclusion**

The `GameManager` class is responsible for managing the game’s progression, including loading levels, handling player collisions, and transitioning between levels. It ensures that the player moves through levels smoothly and manages game completion when the player reaches the door. By using this class, you can efficiently manage the game's flow and handle level transitions in a structured manner.

This class also communicates with the `GameLayer` to update the visual elements, and with the `Player` class to handle player actions, making it the central component for game logic and flow.

---

### **Managing the Player (`Player.kt`)**

The `Player` class is responsible for handling the player's movement, jumping, grappling, and interactions with the environment. It tracks the player's position, velocity, and state, and updates accordingly based on user input or game mechanics.

#### **1. Initialization and Player Properties**

The class defines several properties related to the player's state, such as position (`x`, `y`), velocity (`dx`, `dy`), size, and whether the player is in the air or grappling.

```kotlin
class Player(var x: Float, var y: Float) {
    var dx: Float = 0f
    var dy: Float = 0f
    var isInAir: Boolean = false
    var size: Float = 50f

    private var grappling: Boolean = false
    var grappleTarget: PointF? = null
    private val grappleSpeed = 15f
}
```

**Explanation**: The `Player` class tracks the player's horizontal (`dx`) and vertical (`dy`) speed, as well as whether the player is jumping (`isInAir`). The `grappling` state and the `grappleTarget` determine if and where the player is grappling.

#### **2. Player Movement and Update (`update()`)**

The `update()` method is responsible for updating the player's position based on user input (left, right, jump) and applying gravity. It also handles the logic for grappling if the player is in a grappling state.

```kotlin
fun update(isMovingLeft: Boolean, isMovingRight: Boolean, isJumping: Boolean) {
    // Reset horizontal movement if no direction is pressed
    if (!isMovingLeft && !isMovingRight) {
        dx = 0f
    }

    if (grappling && grappleTarget != null) {
        // Grappling logic remains the same
        val directionX = grappleTarget!!.x - (x + size / 2)
        val directionY = grappleTarget!!.y - (y + size / 2)
        val distance = sqrt(directionX * directionX + directionY * directionY)

        if (distance < grappleSpeed) {
            grappling = false
            grappleTarget = null
        } else {
            dx = directionX / distance * grappleSpeed
            dy = directionY / distance * grappleSpeed
        }
    } else {
        // Normal movement logic
        if (isMovingLeft) dx = -15f
        if (isMovingRight) dx = 15f
        if (isJumping && !isInAir) {
            dy = -15f
            isInAir = true
        }
        dy += 0.5f // Gravity
    }

    // Apply movement
    x += dx
    y += dy

    // Check for landing (collision with ground)
    if (y > screenHeight - 300f - size) {
        y = screenHeight - 300f - size
        dy = 0f
        if (isInAir) {
            isInAir = false
        }
    }
}
```

**Explanation**:
- The player's horizontal speed (`dx`) is updated based on whether the left or right movement buttons are pressed.
- If the player is jumping (`isJumping`), a vertical velocity (`dy`) is applied, and gravity is simulated by continuously increasing `dy`.
- The `grappling` logic moves the player toward the target if the player is grappling, otherwise the player moves normally.
- The player's vertical position (`y`) is constrained to the screen, ensuring the player doesn't fall through the ground.

#### **3. Platform Collision Handling (`handlePlatformCollision()`)**

This method ensures that the player properly lands on platforms by adjusting the player's vertical position when colliding with a platform.

```kotlin
fun handlePlatformCollision(platform: RectF) {
    if (dy > 0 && RectF.intersects(platform, getBoundingRect())) {
        y = platform.top - size
        dy = 0f
        isInAir = false
    }
}
```

**Explanation**: When the player is falling (`dy > 0`), this method checks for collisions with platforms. If the player's bounding box intersects with a platform, the player’s `y` position is adjusted to sit on top of the platform, and gravity (`dy`) is stopped.

#### **4. Grappling Logic (`grappleTo()` and `isNear()`)**

The `grappleTo()` method allows the player to initiate a grappling action, and the `isNear()` method checks if the player is near a grappling point.

```kotlin
fun grappleTo(target: PointF) {
    grappling = true
    grappleTarget = target
    isInAir = true
}

fun isNear(grapplePoint: GameLayer.GrapplePoint, proximityThreshold: Float = 100f): Boolean {
    val pointX = grapplePoint.x
    val pointY = grapplePoint.y
    val dx = pointX - x
    val dy = pointY - y
    val distance = sqrt(dx * dx + dy * dy)
    return distance <= proximityThreshold
}
```

**Explanation**:
- The `grappleTo()` method sets the player to a grappling state and assigns a target (`grappleTarget`).
- The `isNear()` method checks if the player is within a certain distance of a grapple point to determine if the player can grapple to it.

#### **5. Bounding Box and Collision Detection (`getBoundingRect()`)**

The `getBoundingRect()` method returns the player's bounding rectangle, which is used for collision detection.

```kotlin
fun getBoundingRect(): RectF = RectF(x, y, x + size, y + size)
```

**Explanation**: The method returns a `RectF` object representing the player's bounding box, which is used to detect collisions with platforms, stars, and other game elements.

---

### **Conclusion**

The `Player` class is central to managing the player's movement, jumping, and grappling behavior in the game. It handles both normal movement and special actions like grappling, updating the player’s position based on input or physics (gravity). The class also manages collision detection with platforms and stars, ensuring that the player can interact with the environment correctly.

This class can be extended with additional features, such as more complex movement mechanics, interactions with objects, or advanced physics behaviors.

---

### **Managing the Main Activity

(`MainActivity.kt`)**

The `MainActivity` class is the entry point of the game. It manages user authentication, the main game loop, and transitions between screens, such as the main menu, game menu, and login/signup screens.

#### **1. Initialization and Firebase Setup**

The `onCreate()` method initializes Firebase authentication, the game view, and the game layer. It also checks if a user is logged in and displays the appropriate screen based on the authentication status.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    auth = FirebaseAuth.getInstance()
    database = FirebaseDatabase.getInstance().reference

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
```

**Explanation**: The `onCreate()` method initializes the Firebase authentication (`auth`) and sets up the game view (`gameView`) and game layer (`gameLayer`). If the user is logged in, it shows the game menu; otherwise, it shows the main menu.

#### **2. Main Menu Screen (`MainMenuScreen()`)**

The main menu screen provides options to log in, sign up, or start a new game. This is presented using **Jetpack Compose** UI components.

```kotlin
@Composable
fun MainMenuScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Main Menu") })
        
        Button(onClick = { navigateToLogin() }) { Text("Login") }
        Button(onClick = { navigateToSignUp() }) { Text("Sign Up") }
        Button(onClick = { startGame() }) { Text("Start Game") }
    }
}
```

**Explanation**: This screen displays three buttons: one for logging in, one for signing up, and one to start a new game. Each button triggers the corresponding action (navigate to login, sign up, or start the game).

#### **3. Game Menu Screen (`GameMenuScreen()`)**

Once the user logs in, the game menu screen shows options to start a new game, load a saved game, or log out.

```kotlin
@Composable
fun GameMenuScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Game Menu") })
        
        Button(onClick = { startGame() }) { Text("New Game") }
        Button(onClick = { loadGame() }) { Text("Load Game") }
        Button(onClick = { signOutUser() }) { Text("Logout") }
    }
}
```

**Explanation**: The game menu provides buttons to either start a new game, load a saved game from Firebase, or log out of the account. These options manage game states, such as starting a fresh game or resuming a previous game session.

#### **4. Starting a New Game (`startGame()`)**

The `startGame()` method initializes the game by setting up the game view and game layer, and starts the game thread. It also loads the first level of the game.

```kotlin
private fun startGame() {
    Log.d("GameStart", "Starting a new game...")
    surfaceView = SurfaceView(this)
    val surfaceHolder = surfaceView.holder

    val player = Player(100f, 1400f)
    player.update(false, false, false)
    gameLayer = GameLayer(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, player)

    gameView = GameView(this, gameLayer, player)
    setContentView(gameView)

    gameThread = GameThread(surfaceHolder, gameLayer, player)
    gameManager = GameManager(this, gameLayer, player, gameThread)
    gameManager.loadLevel(0)

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
```

**Explanation**: The `startGame()` method creates a new player, sets up the `GameLayer`, and initializes the game view. The `GameThread` is started, and the first level is loaded. The game surface is set up to draw and update the game continuously.

#### **5. Loading a Saved Game (`loadGame()`)**

The `loadGame()` method retrieves saved game data from Firebase, such as the player's level and position, and loads the game state accordingly.

```kotlin
private fun loadGame() {
    Log.d("GameLoad", "Attempting to load saved game...")
    val userId = auth.currentUser?.uid ?: return

    database.child("users").child(userId).child("savedGame").get().addOnSuccessListener { snapshot -> 
        if (snapshot.exists()) { 
            val savedLevel = snapshot.child("level").value as? Int ?: 1 
            val savedPlayerPositionX = snapshot.child("playerX").value as? Float ?: 100f 
            val savedPlayerPositionY = snapshot.child("playerY").value as? Float ?: 1400f
            Log.d("GameLoad", "Loaded saved game: Level $savedLevel, Player Position: ($savedPlayerPositionX, $savedPlayerPositionY)")
            
            val player = Player(savedPlayerPositionX, savedPlayerPositionY) 
            gameLayer = GameLayer(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, player) 
            gameView = GameView(this, gameLayer, player)
            
            setContentView(gameView)
            
            gameThread = GameThread(gameView.holder, gameLayer, player) 
            gameManager = GameManager(this, gameLayer, player, gameThread) 
            gameManager.loadLevel(savedLevel)  // Pass saved level
            
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
```

**Explanation**: This method loads the saved game data (level, position) from Firebase Realtime Database and updates the game state accordingly. If no saved data exists, it notifies the user.

#### **6. Sign Out and Navigation (`signOutUser()`, `navigateToLogin()`, `navigateToSignUp()`)**

These methods handle user sign-out and navigation to the login and sign-up screens.

```kotlin
private fun signOutUser() {
    auth.signOut()
    setContent {
        AppSemesterProjectTheme {
            MainMenuScreen() // Show main menu after logging out
        }
    }
}
```

**Explanation**: The `signOutUser()` method signs the user out of Firebase and displays the main menu screen.

---

### **Conclusion**

The `MainActivity` class is the central activity in the game, handling user authentication, displaying different menus (main menu, game menu), and managing game state (starting new games, loading saved games, logging out). It integrates Firebase for storing and retrieving game data and uses **Jetpack Compose** to manage the UI components. The game loop, game view, and game manager are initialized here, and the activity ensures smooth transitions between different states of the game.

--- 

## **Further Discussion**

### **Jetpack Compose Integration**

While this game uses `SurfaceView` for most of the rendering, you can combine it with **Jetpack Compose** to build more complex UIs. If you'd like to integrate Compose for modern UI components, here’s a simple example:

```kotlin
@Composable
fun GameScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Place your game components here
        Text("Game Running", modifier = Modifier.align(Alignment.Center))
    }
}
```

---

## **How to Contribute**

If you have suggestions or improvements, feel free to fork the repository and submit a pull request. Any contributions are welcome!

---