package com.example.myapplication.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.audio.SoundManager
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.data.network.FirebaseMultiplayerService
import com.example.myapplication.data.network.GameRoom
import com.example.myapplication.domain.AppTheme
import com.example.myapplication.domain.BoardTile
import com.example.myapplication.domain.DifficultyLevel
import com.example.myapplication.domain.GameMode
import com.example.myapplication.domain.GameState
import com.example.myapplication.domain.GameWinner
import com.example.myapplication.domain.TicTacToeGameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicTacToeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val gameEngine = TicTacToeGameEngine()
    private val soundManager = SoundManager()
    private val multiplayerService = FirebaseMultiplayerService()

    private var roomObserveJob: Job? = null

    private val _uiState = MutableStateFlow(
        GameState(
            humanWins = preferencesManager.getHumanWins(),
            computerWins = preferencesManager.getComputerWins(),
            ties = preferencesManager.getTies(),
            difficulty = preferencesManager.getDifficulty(),
            gameMode = preferencesManager.getGameMode(),
            soundEnabled = preferencesManager.isSoundEnabled(),
            selectedTheme = preferencesManager.getTheme()
        )
    )

    val uiState: StateFlow<GameState> = _uiState.asStateFlow()
    
    // Flujo expuesto para el diálogo de lobby
    val availableRooms: StateFlow<List<GameRoom>> = MutableStateFlow(emptyList())

    init {
        // Observa la lista de salas disponibles
        viewModelScope.launch {
            multiplayerService.getAvailableRooms().collect { rooms ->
                (availableRooms as MutableStateFlow).value = rooms
            }
        }
    }

    fun onTileClicked(index: Int) {
        val currentState = _uiState.value

        if (currentState.isGameOver ||
            currentState.isCpuThinking ||
            currentState.board[index] != BoardTile.EMPTY
        ) {
            return
        }

        if (currentState.gameMode == GameMode.ONLINE_MULTIPLAYER) {
            // Lógica Online
            if (!currentState.isOnlineMyTurn || currentState.onlineRoomId == null || currentState.isWaitingForOpponent) return

            // El jugador actual marca, si es su turno. Depende de si creó la sala o se unió
            val activeTile = if (multiplayerService.currentUserId == currentState.isHumanTurn.toString()) BoardTile.PLAYER_X else BoardTile.PLAYER_O // Ajuste necesario, lo hacemos simplificado abajo

            val updatedBoard = currentState.board.toMutableList().apply {
                set(index, if(currentState.isHumanTurn) BoardTile.PLAYER_X else BoardTile.PLAYER_O)
            }
            
            // Asignamos la cadena a enviar a Firebase
            val onlineBoard = updatedBoard.map { 
                when(it) {
                    BoardTile.PLAYER_X -> "X"
                    BoardTile.PLAYER_O -> "O"
                    else -> ""
                }
            }
            
            // Evaluamos si hubo gane
            val (winner, winningLine) = gameEngine.checkWinner(updatedBoard)
            val nextTurn = if(winner == GameWinner.NONE) "" else "" // Placeholder para simplificar lógica de turnos arriba en observeRoom

            // Actualizamos en Firebase
            multiplayerService.updateBoard(currentState.onlineRoomId, onlineBoard, nextTurn)
            
            if (winner != GameWinner.NONE) {
                 // Dejamos que el observer (observeRoom) finalice la partida en BD o lo hacemos aquí
                 multiplayerService.finishGame(currentState.onlineRoomId, if(currentState.isHumanTurn) multiplayerService.currentUserId else "opponentId")
            }

            return
        }

        if (currentState.gameMode == GameMode.ONE_PLAYER) {
            if (!currentState.isHumanTurn) return

            // Sonido de movimiento del jugador adaptado al tema actual
            soundManager.playHumanMove(currentState.selectedTheme, currentState.soundEnabled)

            val updatedBoard = currentState.board.toMutableList().apply {
                set(index, BoardTile.HUMAN)
            }

            val (winner, winningLine) = gameEngine.checkWinner(updatedBoard)

            if (winner != GameWinner.NONE) {
                handleGameEnd(updatedBoard, winner, winningLine)
            } else {
                _uiState.update {
                    it.copy(
                        board = updatedBoard,
                        isHumanTurn = false,
                        isCpuThinking = true
                    )
                }
                triggerCpuMove()
            }
        } else {
            val activePlayerTile = if (currentState.isHumanTurn) BoardTile.HUMAN else BoardTile.COMPUTER

            if (currentState.isHumanTurn) {
                soundManager.playHumanMove(currentState.selectedTheme, currentState.soundEnabled)
            } else {
                soundManager.playComputerMove(currentState.selectedTheme, currentState.soundEnabled)
            }

            val updatedBoard = currentState.board.toMutableList().apply {
                set(index, activePlayerTile)
            }

            val (winner, winningLine) = gameEngine.checkWinner(updatedBoard)

            if (winner != GameWinner.NONE) {
                handleGameEnd(updatedBoard, winner, winningLine)
            } else {
                _uiState.update {
                    it.copy(
                        board = updatedBoard,
                        isHumanTurn = !currentState.isHumanTurn,
                        isCpuThinking = false
                    )
                }
            }
        }
    }

    private fun triggerCpuMove() {
        viewModelScope.launch {
            delay(500)

            val currentState = _uiState.value
            val cpuMove = gameEngine.getCpuMove(currentState.board, currentState.difficulty)

            if (cpuMove != null) {
                // Sonido de movimiento de la computadora adaptado al tema actual
                soundManager.playComputerMove(currentState.selectedTheme, currentState.soundEnabled)

                val updatedBoard = currentState.board.toMutableList().apply {
                    set(cpuMove, BoardTile.COMPUTER)
                }

                val (winner, winningLine) = gameEngine.checkWinner(updatedBoard)

                if (winner != GameWinner.NONE) {
                    handleGameEnd(updatedBoard, winner, winningLine)
                } else {
                    _uiState.update {
                        it.copy(
                            board = updatedBoard,
                            isHumanTurn = true,
                            isCpuThinking = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isCpuThinking = false) }
            }
        }
    }

    private fun handleGameEnd(
        board: List<BoardTile>,
        winner: GameWinner,
        winningLine: List<Int>?
    ) {
        val current = _uiState.value
        var newHumanWins = current.humanWins
        var newComputerWins = current.computerWins
        var newTies = current.ties

        when (winner) {
            GameWinner.HUMAN -> {
                newHumanWins++
                soundManager.playWinSound(current.soundEnabled)
            }
            GameWinner.COMPUTER -> {
                newComputerWins++
                soundManager.playLoseSound(current.soundEnabled)
            }
            GameWinner.ONLINE_OPPONENT -> {
                newComputerWins++
                soundManager.playLoseSound(current.soundEnabled)
            }
            GameWinner.TIE -> {
                newTies++
                soundManager.playTieSound(current.soundEnabled)
            }
            else -> {}
        }

        preferencesManager.saveScores(newHumanWins, newComputerWins, newTies)

        _uiState.update {
            it.copy(
                board = board,
                winner = winner,
                winningLine = winningLine,
                humanWins = newHumanWins,
                computerWins = newComputerWins,
                ties = newTies,
                isCpuThinking = false
            )
        }
    }

    fun createOnlineGame() {
        val roomId = multiplayerService.createRoom("Jugador Anfitrión")
        _uiState.update { 
            it.copy(
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                onlineRoomId = roomId,
                isWaitingForOpponent = true,
                onlineStatusText = "Esperando oponente en sala: $roomId",
                isHumanTurn = true, // X empieza
                isOnlineMyTurn = true // Creador de sala empieza
            ) 
        }
        observeRoom(roomId)
    }

    fun joinOnlineGame(room: GameRoom) {
        multiplayerService.joinRoom(room.id, "Jugador Invitado")
        _uiState.update { 
            it.copy(
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                onlineRoomId = room.id,
                isWaitingForOpponent = false,
                onlineStatusText = "Jugando contra ${room.creatorName}",
                isHumanTurn = false, // X empieza, oponente es O
                isOnlineMyTurn = false // El creador empieza
            ) 
        }
        observeRoom(room.id)
    }

    private fun observeRoom(roomId: String) {
        roomObserveJob?.cancel()
        roomObserveJob = viewModelScope.launch {
            multiplayerService.observeRoom(roomId).collect { room ->
                if (room == null) {
                    _uiState.update { it.copy(onlineStatusText = "Sala cerrada/Error") }
                    return@collect
                }
                
                // Actualizar estado general
                val newBoard = room.board.map { 
                    when(it) {
                        "X" -> BoardTile.PLAYER_X
                        "O" -> BoardTile.PLAYER_O
                        else -> BoardTile.EMPTY
                    }
                }
                
                val iAmX = (room.creatorId == multiplayerService.currentUserId)
                val isMyTurnNow = (room.turn == multiplayerService.currentUserId)
                
                val gameIsOver = room.status == "finished"
                val winner = when (room.winner) {
                    multiplayerService.currentUserId -> GameWinner.HUMAN
                    "draw" -> GameWinner.TIE
                    null -> GameWinner.NONE
                    else -> GameWinner.ONLINE_OPPONENT
                }

                _uiState.update {
                    it.copy(
                        board = newBoard,
                        isWaitingForOpponent = room.status == "waiting",
                        onlineStatusText = if (room.status == "waiting") "Esperando oponente..." else if (gameIsOver) "Juego terminado" else "Tu turno: $isMyTurnNow",
                        isOnlineMyTurn = isMyTurnNow,
                        isHumanTurn = (room.turn == room.creatorId),
                        winner = winner
                    )
                }
            }
        }
    }
    
    fun toggleOnlineLobby(show: Boolean) {
        _uiState.update { it.copy(showOnlineLobby = show) }
    }

    fun resetBoard() {
        _uiState.update {
            it.copy(
                board = List(9) { BoardTile.EMPTY },
                isHumanTurn = true,
                winner = GameWinner.NONE,
                winningLine = null,
                isCpuThinking = false
            )
        }
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        preferencesManager.saveDifficulty(difficulty)
        _uiState.update { it.copy(difficulty = difficulty) }
        resetBoard()
    }

    fun setGameMode(gameMode: GameMode) {
        preferencesManager.saveGameMode(gameMode)
        _uiState.update { it.copy(gameMode = gameMode) }
        resetBoard()
    }

    fun toggleSound() {
        val newSoundState = !_uiState.value.soundEnabled
        preferencesManager.saveSoundEnabled(newSoundState)
        _uiState.update { it.copy(soundEnabled = newSoundState) }
    }

    fun setTheme(theme: AppTheme) {
        preferencesManager.saveTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun showGameModeDialog(show: Boolean) {
        _uiState.update { it.copy(showGameModeDialog = show) }
    }

    fun showDifficultyDialog(show: Boolean) {
        _uiState.update { it.copy(showDifficultyDialog = show) }
    }

    fun showThemeDialog(show: Boolean) {
        _uiState.update { it.copy(showThemeDialog = show) }
    }

    fun showAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }

    fun resetScores() {
        preferencesManager.resetScores()
        _uiState.update {
            it.copy(
                humanWins = 0,
                computerWins = 0,
                ties = 0
            )
        }
        resetBoard()
    }
}
