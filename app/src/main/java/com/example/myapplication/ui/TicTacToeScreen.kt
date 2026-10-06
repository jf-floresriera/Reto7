package com.example.myapplication.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.myapplication.domain.GameMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.GameState
import com.example.myapplication.domain.GameWinner
import com.example.myapplication.ui.components.AboutDialog
import com.example.myapplication.ui.components.DifficultyDialog
import com.example.myapplication.ui.components.GameBoardView
import com.example.myapplication.ui.components.GameModeDialog
import com.example.myapplication.ui.components.ScoreBoardCard
import com.example.myapplication.ui.components.ThemeDialog
import com.example.myapplication.ui.components.VictoryDialog
import com.example.myapplication.ui.components.OnlineLobbyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    viewModel: TicTacToeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableRooms by viewModel.availableRooms.collectAsState()

    // Manejar el despliegue del Lobby Online cuando se elige ese modo
    LaunchedEffect(uiState.gameMode) {
        if (uiState.gameMode == GameMode.ONLINE_MULTIPLAYER && uiState.onlineRoomId == null) {
            viewModel.toggleOnlineLobby(true)
        }
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val colorScheme = lightColorScheme(
        primary = Color(0xFF6200EE),
        primaryContainer = Color(0xFF6200EE),
        onPrimaryContainer = Color.White
    )

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tres en Raya - Reto 6",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.toggleSound() }) {
                            Icon(
                                imageVector = if (uiState.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Sound Toggle",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFFAFAFA)
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.resetBoard() },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "New Game", tint = Color(0xFF6200EE)) },
                        label = { Text("New Game", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showGameModeDialog(true) },
                        icon = { Icon(Icons.Default.Share, contentDescription = "Game Mode") },
                        label = { Text("Game Mode", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showDifficultyDialog(true) },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Difficulty") },
                        label = { Text("Difficulty", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showThemeDialog(true) },
                        icon = { Icon(Icons.Default.Palette, contentDescription = "Tema") },
                        label = { Text("Tema", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showAboutDialog(true) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                        label = { Text("About", fontSize = 11.sp) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLandscape) {
                    LandscapeContent(
                        uiState = uiState,
                        onTileClick = viewModel::onTileClicked,
                        onResetBoard = viewModel::resetBoard,
                        onResetScores = viewModel::resetScores,
                        onDifficultyClick = { viewModel.showDifficultyDialog(true) },
                        onSoundToggleClick = viewModel::toggleSound,
                        onThemeClick = { viewModel.showThemeDialog(true) }
                    )
                } else {
                    PortraitContent(
                        uiState = uiState,
                        onTileClick = viewModel::onTileClicked,
                        onResetBoard = viewModel::resetBoard,
                        onResetScores = viewModel::resetScores,
                        onDifficultyClick = { viewModel.showDifficultyDialog(true) },
                        onSoundToggleClick = viewModel::toggleSound,
                        onThemeClick = { viewModel.showThemeDialog(true) }
                    )
                }

                if (uiState.winner != GameWinner.NONE) {
                    VictoryDialog(
                        winner = uiState.winner,
                        onPlayAgain = { viewModel.resetBoard() },
                        onDismiss = { }
                    )
                }

                if (uiState.showGameModeDialog) {
                    GameModeDialog(
                        currentGameMode = uiState.gameMode,
                        onGameModeSelected = { mode -> viewModel.setGameMode(mode) },
                        onDismiss = { viewModel.showGameModeDialog(false) }
                    )
                }

                if (uiState.showDifficultyDialog) {
                    DifficultyDialog(
                        currentDifficulty = uiState.difficulty,
                        onDifficultySelected = { level -> viewModel.setDifficulty(level) },
                        onDismiss = { viewModel.showDifficultyDialog(false) }
                    )
                }

                if (uiState.showThemeDialog) {
                    ThemeDialog(
                        currentTheme = uiState.selectedTheme,
                        onThemeSelected = { theme -> viewModel.setTheme(theme) },
                        onDismiss = { viewModel.showThemeDialog(false) }
                    )
                }

                if (uiState.showAboutDialog) {
            AboutDialog(
                onDismiss = { viewModel.showAboutDialog(false) }
            )
        }
        
        if (uiState.showOnlineLobby) {
            OnlineLobbyDialog(
                theme = uiState.selectedTheme,
                availableRooms = availableRooms,
                onCreateRoom = { 
                    viewModel.createOnlineGame()
                    viewModel.toggleOnlineLobby(false)
                },
                onJoinRoom = { room -> 
                    viewModel.joinOnlineGame(room)
                    viewModel.toggleOnlineLobby(false)
                },
                onDismiss = {
                    viewModel.toggleOnlineLobby(false)
                    if(uiState.onlineRoomId == null) {
                        viewModel.setGameMode(GameMode.ONE_PLAYER) // Fallback si cancela
                    }
                }
            )
        }
            }
        }
    }
}

@Composable
private fun PortraitContent(
    uiState: GameState,
    onTileClick: (Int) -> Unit,
    onResetBoard: () -> Unit,
    onResetScores: () -> Unit,
    onDifficultyClick: () -> Unit,
    onSoundToggleClick: () -> Unit,
    onThemeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TurnStatusCard(uiState = uiState)

        Spacer(modifier = Modifier.height(12.dp))

        GameBoardView(
            board = uiState.board,
            winningLine = uiState.winningLine,
            selectedTheme = uiState.selectedTheme,
            onTileClick = onTileClick,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onResetBoard,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32)
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = "REINICIAR JUEGO",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScoreBoardCard(
            humanWins = uiState.humanWins,
            computerWins = uiState.computerWins,
            ties = uiState.ties,
            difficulty = uiState.difficulty,
            soundEnabled = uiState.soundEnabled,
            selectedTheme = uiState.selectedTheme,
            onDifficultyClick = onDifficultyClick,
            onSoundToggleClick = onSoundToggleClick,
            onThemeClick = onThemeClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onResetScores,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(text = "Reiniciar Marcador", fontSize = 13.sp)
        }
    }
}

@Composable
private fun LandscapeContent(
    uiState: GameState,
    onTileClick: (Int) -> Unit,
    onResetBoard: () -> Unit,
    onResetScores: () -> Unit,
    onDifficultyClick: () -> Unit,
    onSoundToggleClick: () -> Unit,
    onThemeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TurnStatusCard(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onResetBoard,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = "REINICIAR JUEGO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ScoreBoardCard(
                humanWins = uiState.humanWins,
                computerWins = uiState.computerWins,
                ties = uiState.ties,
                difficulty = uiState.difficulty,
                soundEnabled = uiState.soundEnabled,
                selectedTheme = uiState.selectedTheme,
                onDifficultyClick = onDifficultyClick,
                onSoundToggleClick = onSoundToggleClick,
                onThemeClick = onThemeClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onResetScores,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(text = "Reiniciar Marcador", fontSize = 11.sp)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            GameBoardView(
                board = uiState.board,
                winningLine = uiState.winningLine,
                selectedTheme = uiState.selectedTheme,
                onTileClick = onTileClick,
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun TurnStatusCard(uiState: GameState) {
    val statusText = when {
        uiState.isGameOver -> "Partida Finalizada"
        uiState.isCpuThinking -> "Procesando movimiento de la CPU..."
        uiState.isHumanTurn -> "Tu turno (X)"
        else -> "Turno de O"
    }

    Card(
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        )
    ) {
        Text(
            text = statusText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1),
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
