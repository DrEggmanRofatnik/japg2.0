import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.coroutines.delay

// Enum to manage game states
enum class GameState {
    TITLE_SCREEN,
    PLAYING,
    PAUSED
}

data class Paddle(val width: Float, val height: Float, var position: Float)

data class Ball(var x: Float, var y: Float, var dx: Float, var dy: Float)

@Composable
fun PongGameWithTitleScreen(
    onButtonSound: () -> Unit,
    onBackgroundMusic: () -> Unit,
    stopBackgroundMusic: () -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.TITLE_SCREEN) }
    var paddleLeft = remember { mutableStateOf(Paddle(20f, 100f, 0f)) }
    var paddleRight = remember { mutableStateOf(Paddle(20f, 100f, 0f)) }
    var ball = remember { mutableStateOf(Ball(300f, 400f, 5f, 5f)) }

    // Game Loop to move ball
    LaunchedEffect(ball) {
        while (gameState == GameState.PLAYING) {
            delay(16L) // Approx 60fps

            // Ball movement
            ball.value.x += ball.value.dx
            ball.value.y += ball.value.dy

            // Ball collision with top/bottom walls
            if (ball.value.y <= 0 || ball.value.y >= 800) ball.value.dy *= -1

            // Ball collision with paddles
            if (ball.value.x <= paddleLeft.value.position + paddleLeft.value.width && ball.value.y in (paddleLeft.value.position..(paddleLeft.value.position + paddleLeft.value.height))) {
                ball.value.dx *= -1
            }
            if (ball.value.x >= paddleRight.value.position - paddleRight.value.width && ball.value.y in (paddleRight.value.position..(paddleRight.value.position + paddleRight.value.height))) {
                ball.value.dx *= -1
            }

            // Ball out of bounds (scoring)
            if (ball.value.x <= 0 || ball.value.x >= 600) {
                ball.value.x = 300f
                ball.value.y = 400f
                ball.value.dx = 5f
                ball.value.dy = 5f
            }
        }
    }

    when (gameState) {
        GameState.TITLE_SCREEN -> TitleScreen(
            onStartGame = {
                onButtonSound()
                gameState = GameState.PLAYING
                onBackgroundMusic()
            }
        )
        GameState.PLAYING -> PongGame(
            paddleLeft = paddleLeft,
            paddleRight = paddleRight,
            ball = ball,
            onPauseGame = {
                gameState = GameState.PAUSED
                stopBackgroundMusic()
            },
            onQuitGame = {
                onButtonSound()
                gameState = GameState.TITLE_SCREEN
                stopBackgroundMusic()
            },
            onBackgroundMusic = onBackgroundMusic
        )
        GameState.PAUSED -> PauseMenu(
            onResumeGame = {
                onButtonSound()
                gameState = GameState.PLAYING
                onBackgroundMusic()
            },
            onQuitGame = {
                onButtonSound()
                gameState = GameState.TITLE_SCREEN
                stopBackgroundMusic()
            }
        )
    }
}

@Composable
fun TitleScreen(onStartGame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pong Game",
                style = TextStyle(color = Color.White, fontSize = 32.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onStartGame) {
                Text(text = "Start Game")
            }
        }
    }
}

@Composable
fun PongGame(
    paddleLeft: MutableState<Paddle>,
    paddleRight: MutableState<Paddle>,
    ball: MutableState<Ball>,
    onPauseGame: () -> Unit,
    onQuitGame: () -> Unit,
    onBackgroundMusic: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pong Game is Playing",
                style = TextStyle(color = Color.White, fontSize = 24.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row {
                Button(onClick = onPauseGame) {
                    Text(text = "Pause")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onQuitGame) {
                    Text(text = "Quit")
                }
            }

            // Paddles
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .width(paddleLeft.value.width.dp)
                        .height(paddleLeft.value.height.dp)
                        .background(Color.White)
                        .align(Alignment.CenterStart)
                        .offset(x = paddleLeft.value.position.dp)
                )
                Box(
                    modifier = Modifier
                        .width(paddleRight.value.width.dp)
                        .height(paddleRight.value.height.dp)
                        .background(Color.White)
                        .align(Alignment.CenterEnd)
                        .offset(x = paddleRight.value.position.dp)
                )

                // Ball
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White)
                        .align(Alignment.Center)
                        .offset(x = ball.value.x.dp, y = ball.value.y.dp)
                )
            }
        }
    }
}

@Composable
fun PauseMenu(onResumeGame: () -> Unit, onQuitGame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Game Paused",
                style = TextStyle(color = Color.White, fontSize = 24.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row {
                Button(onClick = onResumeGame) {
                    Text(text = "Resume")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onQuitGame) {
                    Text(text = "Quit")
                }
            }
        }
    }
}

// Preview Function for the PongGameWithTitleScreen
@Preview(showBackground = true)
@Composable
fun PreviewPongGameWithTitleScreen() {
    PongGameWithTitleScreen(
        onButtonSound = {},
        onBackgroundMusic = {},
        stopBackgroundMusic = {}
    )
}
