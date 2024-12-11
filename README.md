
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
   - Download from [here](https://developer.android.com/studio).
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

### **1. Setting up the Game View (`GameView.kt`)**

The `GameView` class handles the surface where the game is drawn. It uses `SurfaceView` to display the game content and interacts with the `GameThread` to ensure the game is continuously updated.

```kotlin
// file: GameView.kt
class GameView(context: Context, private val gameLayer: GameLayer, private val player: Player) : SurfaceView(context), SurfaceHolder.Callback {
    private lateinit var gameThread: GameThread

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder, gameLayer, player)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread.running = false
    }

    fun update() {
        gameLayer.update()
    }

    fun draw(canvas: Canvas) {
        gameLayer.draw(canvas)
    }
}
```

**Explanation:**
- `GameView` extends `SurfaceView` and implements `SurfaceHolder.Callback` to manage the game surface.
- `gameThread` manages the game loop.

---

### **2. Creating the Game Thread (`GameThread.kt`)**

The `GameThread` class controls the game loop, constantly updating the game state and rendering it to the screen.

```kotlin
// file: GameThread.kt
class GameThread(private val surfaceHolder: SurfaceHolder, private val gameLayer: GameLayer, private val player: Player) : Thread() {
    var running: Boolean = false

    override fun run() {
        while (running) {
            val canvas = surfaceHolder.lockCanvas()
            canvas?.let {
                synchronized(surfaceHolder) {
                    gameLayer.update()
                    gameLayer.draw(it)
                }
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }
    }
}
```

**Explanation:**
- The game loop runs inside the `run()` method, where it constantly updates and redraws the game content.

---

### **3. Handling Background Scrolling (`Background.kt`)**

The background scrolling logic implements the parallax effect, making the background move smoothly as the game progresses.

```kotlin
// file: Background.kt
class Background(private val context: Context) {
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.background)
    private var xOffset = 0

    fun update() {
        xOffset += 5
        if (xOffset >= backgroundBitmap.width) {
            xOffset = 0
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(backgroundBitmap, -xOffset.toFloat(), 0f, null)
        canvas.drawBitmap(backgroundBitmap, (backgroundBitmap.width - xOffset).toFloat(), 0f, null)
    }
}
```

**Explanation:**
- The background is drawn twice to create a seamless effect as it scrolls.

---

### **4. Managing the Game Layer (`GameLayer.kt` and `GameManager.kt`)**

The `GameLayer` class manages all game objects, including the player and background. The `GameManager` coordinates the flow of the game.

#### **GameLayer.kt**

```kotlin
// file: GameLayer.kt
class GameLayer(private val screenWidth: Int, private val screenHeight: Int, private val player: Player) {
    private val background: Background = Background(context)

    fun update() {
        background.update()
        player.update()
    }

    fun draw(canvas: Canvas) {
        background.draw(canvas)
        player.draw(canvas)
    }
}
```

**Explanation:**
- `GameLayer` manages the game's components (background and player) and updates them.

#### **GameManager.kt**

```kotlin
// file: GameManager.kt
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
class GameManager(private val context: Context, private val gameLayer: GameLayer, private val player: Player, private val gameThread: GameThread) {
   
    private val levels = listOf(
      Level(1, 1, "Chalk Sketch Valley", R.drawable.sketchvalleybg, R.drawable.platformdflat, R.drawable.platforma, platformChalkSketch)
   )
   
   fun loadLevel(levelIndex: Int) {
      val level = levels[levelIndex]
      currentLevelIndex = levelIndex

      // Load background, ground, and platforms
      val backgroundBitmap = BitmapFactory.decodeResource(context.resources, level.backgroundResId)
      val platformBitmap = BitmapFactory.decodeResource(context.resources, level.platformImageResId)
      val groundBitmap = BitmapFactory.decodeResource(context.resources, level.groundImageResId)
      
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
      // Create an intent to navigate to the game complete screen
      val intent = Intent(context, GameCompleteActivity::class.java)
      context.startActivity(intent)
   }

   // Implement the LevelTransitionListener method
   override fun onLevelComplete() {
      // Handle level transition logic here, for example:
      Log.d("GameManager", "Level completed, transitioning to the next level!")
      nextLevel()
   }
}
```

**Explanation:**
- `GameManager` controls the flow of the game, managing game state, updates, and rendering.

---

### **5. Handling the Player Object (`Player.kt`)**

This class defines the player object and handles the player's movement.

```kotlin
// file: Player.kt
class Player(var xPos: Float, var yPos: Float) {
    private val playerBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player)

    fun update() {
        // Handle player movement or behavior
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(playerBitmap, xPos, yPos, null)
    }
}
```

**Explanation:**
- `Player` manages the player's bitmap and position on the screen.

---

### **6. MainActivity (`MainActivity.kt`)**

The `MainActivity` manages the main game activity and integrates the game view into the app.

```kotlin
// file: MainActivity.kt
class MainActivity : ComponentActivity() {
    private lateinit var gameLayer: GameLayer
    private lateinit var gameThread: GameThread
    private lateinit var surfaceView: SurfaceView
    private lateinit var gameManager: GameManager
    private lateinit var gameView: GameView
    private lateinit var player: Player
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        player = Player(100f, 1400f)
        gameLayer = GameLayer(screenWidth, screenHeight, player)
        gameView = GameView(this, gameLayer, player)

        setContent {
            AppSemesterProjectTheme {
                if (auth.currentUser == null) {
                    MainMenuScreen() 
                } else {
                    GameMenuScreen()
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

            Button(onClick = { navigateToLogin() }) {
                Text("Login")
            }
            Button(onClick = { navigateToSignUp() }) {
                Text("Sign Up")
            }
            Button(onClick = { startGame() }) {
                Text("Start Game")
            }
        }
    }

    private fun startGame() {
        surfaceView = SurfaceView(this)
        val surfaceHolder = surfaceView.holder

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        player = Player(100f, 1400f)
        gameLayer = GameLayer(screenWidth, screenHeight, player)

        gameView = GameView(this, gameLayer, player)

        setContentView(gameView)

        gameThread = GameThread(surfaceHolder, gameLayer, player)
        gameManager = GameManager(this, gameLayer, player, gameThread)
        gameManager.loadLevel(0)

        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                gameThread.running = true
                gameThread.start()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                gameThread.running = false
                try {
                    gameThread.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
```

**Explanation:**
- `MainActivity` handles the UI, including login, game start, and game load, and manages the game lifecycle.

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
