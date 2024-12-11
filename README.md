# Building an Android Game with Smooth Scrolling and Game Loop

This tutorial will guide you through building a simple Android game featuring a smooth scrolling background with a parallax effect, a custom game loop, and game logic. By following this guide, you will learn how to handle `SurfaceView` rendering, manage background scrolling, and create an efficient game loop for animation and updates.

## Demo App Overview

The demo app created in this tutorial includes the following features:
- **Parallax Scrolling Background**: A smooth scrolling background with a parallax effect, where multiple background layers scroll at different speeds.
- **Game Loop**: A custom game loop to continuously update the game state and redraw the view.
- **Game Elements**: Manage the game’s background and player object on the screen.

Here’s an overview of the app's expected functionality:

- **Background**: The background will scroll continuously, and we will implement a parallax effect where two layers move at different speeds.
- **Player**: A player object will move within the game and interact with the background.
- **Main Game Loop**: The custom game loop will handle the updating of game elements and their rendering to the screen.

---

## Getting Started

To complete this tutorial, you will need the following:

### Prerequisites

1. **Android Studio** (version 2023.1 or higher)
   - Download from [here](https://developer.android.com/studio).
2. **Kotlin** (included with Android Studio)
3. **Android SDK** (included with Android Studio)

### Setting Up the Project

1. Open **Android Studio**.
2. Create a new project with an empty activity.
3. Choose **Kotlin** as the programming language and **API 21** or higher as the minimum SDK.
4. Name your project (e.g., "Game Tutorial").

---

### Dependencies

No additional dependencies are required for this tutorial. We will be using **native Android SDK** components.

---

## Step-by-Step Instructions

Follow the instructions below to implement the game. Each section provides code snippets and explanations on how to integrate them into your project.

---

### 1. Setting up the Game View (`GameView.kt`)

The `GameView` class handles the rendering surface for the game. It uses `SurfaceView` to display the game content, and it interacts with the `GameThread` to ensure the game is continuously updated.

```kotlin
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private val gameThread: GameThread

    init {
        holder.addCallback(this)
        gameThread = GameThread(holder)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes (e.g., screen orientation)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread.stopThread()
    }

    fun update() {
        // Update game objects like background and player
    }

    fun draw(canvas: Canvas) {
        // Draw updated objects to the canvas
    }
}
```

Explanation:
- The `GameView` extends `SurfaceView` and implements `SurfaceHolder.Callback` to manage the game surface.
- The `gameThread` object manages the game loop.

---

### 2. Creating the Game Thread (`GameThread.kt`)

The `GameThread` class manages the game loop, which continuously updates and renders the game.

```kotlin
class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
    private var running: Boolean = false

    override fun run() {
        while (running) {
            val canvas = surfaceHolder.lockCanvas()
            canvas?.let {
                synchronized(surfaceHolder) {
                    updateGame()
                    drawGame(it)
                }
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }
    }

    fun start() {
        running = true
        super.start()
    }

    fun stopThread() {
        running = false
        interrupt()
    }

    private fun updateGame() {
        // Update game elements (e.g., player movement, background scroll)
    }

    private fun drawGame(canvas: Canvas) {
        // Draw the game elements on the canvas
    }
}
```

Explanation:
- The game loop runs inside the `run()` method, where it constantly updates and redraws the game content.
- `start()` begins the game thread, and `stopThread()` stops the thread when the surface is destroyed.

---

### 3. Handling Background Scrolling (`Background.kt`)

In this step, we'll create a scrolling background with a parallax effect, where the background layers move at different speeds.

```kotlin
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

Explanation:
- The background is drawn twice to create a seamless effect as it scrolls. When one part of the background has completely scrolled off-screen, it is repositioned to start over.
- `xOffset` is used to control the scrolling.

---

### 4. Managing the Game Layer (`GameLayer.kt` and `GameManager.kt`)

The `GameLayer` class manages all game objects, including the player and background. The `GameManager` coordinates the flow of the game.

#### **GameLayer.kt**

```kotlin
class GameLayer(context: Context) {
    private val background: Background
    private val player: Player

    init {
        background = Background(context)
        player = Player(context)
    }

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

#### **GameManager.kt**

```kotlin
class GameManager {
    private lateinit var gameLayer: GameLayer

    fun startGame(context: Context) {
        gameLayer = GameLayer(context)
    }

    fun update() {
        gameLayer.update()
    }

    fun draw(canvas: Canvas) {
        gameLayer.draw(canvas)
    }
}
```

Explanation:
- `GameLayer` manages the game's components (background and player) and updates them.
- `GameManager` controls the flow of the game, updating the game layer and drawing it to the canvas.

---

## Further Discussion

### Alternative Approaches

If you're interested in more complex game development, consider using game frameworks like:
- **LibGDX** or **Cocos2d-x** for advanced 2D game mechanics, as they offer more features and tools for game development.
- **Jetpack Compose** for more advanced UI and rendering techniques.

For features like collision detection and scoring, further tutorials or resources might be helpful.

---

## See Also

- [Android Game Development Documentation](https://developer.android.com/games)
- [LibGDX Game Development Framework](https://libgdx.com/)
- [Kotlin Android Extensions](https://kotlinlang.org/docs/android-extensions.html)

---

## How to Contribute

If you have suggestions or improvements, feel free to fork the repository and submit a pull request. Any contributions are welcome!

