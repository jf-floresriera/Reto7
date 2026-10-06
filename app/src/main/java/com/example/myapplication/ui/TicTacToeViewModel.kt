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
            val iAmX = (multiplayerService.currentUserId == currentState.onlineRoomId) // Esto es un acercamiento, lo evaluaremos mejor con el turno actual
            
            // Asumiendo que el que mueve SIEMPRE coloca su ficha correspondiente a su rol.
            // Para simplificar, si isHumanTurn es true, es porque le tocaba a X.
            val activeTile = if (currentState.isHumanTurn) BoardTile.PLAYER_X else BoardTile.PLAYER_O

            val updatedBoard = currentState.board.toMutableList().apply {
                set(index, activeTile)
            }
            
            // Asignamos la cadena a enviar a Firebase
            val onlineBoard = updatedBoard.map { 
                when(it) {
                    BoardTile.PLAYER_X, BoardTile.HUMAN -> "X"
                    BoardTile.PLAYER_O, BoardTile.COMPUTER -> "O"
                    else -> ""
                }
            }
            
            // Evaluamos si hubo gane
            val (winner, winningLine) = gameEngine.checkWinner(updatedBoard)
            
            // Alternar turno para Firebase: Si era mi turno y yo acabo de jugar, el siguiente turno es del oponente.
            // Necesitamos el ID del oponente.
            // Esto lo maneja Firebase en el update, mandaremos "nextTurnId"
            
            // En vez de lidiar con turnos en cliente de forma compleja, delegamos el toggle al observeRoom.
            // Aquí solo subimos el tablero y avisamos de cambio de turno.
            val nextTurn = if(currentState.isHumanTurn) "OPPONENT" else "CREATOR" // Simplificación temporal

            // Actualizamos en Firebase
            multiplayerService.updateBoard(currentState.onlineRoomId, onlineBoard, nextTurn)
            
            if (winner != GameWinner.NONE) {
                 multiplayerService.finishGame(currentState.onlineRoomId, multiplayerService.currentUserId)
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
                soundManager.playWinSound(current.soundEnabled, current.selectedTheme)
            }
            GameWinner.COMPUTER -> {
                newComputerWins++
                soundManager.playLoseSound(current.soundEnabled, current.selectedTheme)
            }
            GameWinner.ONLINE_OPPONENT -> {
                newComputerWins++
                soundManager.playLoseSound(current.soundEnabled, current.selectedTheme)
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
        val roomId = multiplayerService.createRoom(_uiState.value.playerName)
        _uiState.update { 
            it.copy(
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                onlineRoomId = roomId,
                isWaitingForOpponent = true,
                onlineStatusText = "Esperando a un rival...",
                isHumanTurn = true, // X empieza (el creador)
                isOnlineMyTurn = true // Creador de sala empieza
            ) 
        }
        observeRoom(roomId)
    }

    fun joinOnlineGame(room: GameRoom) {
        multiplayerService.joinRoom(room.id, _uiState.value.playerName)
        _uiState.update { 
            it.copy(
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                onlineRoomId = room.id,
                isWaitingForOpponent = false,
                onlineStatusText = "Jugando contra ${room.creatorName}",
                isHumanTurn = false, // El creador es X (true), así que el que se une ve el turno inicial como del otro
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
                val isMyTurnNow = if (iAmX) room.turn == "CREATOR" else room.turn == "OPPONENT"
                
                val gameIsOver = room.status == "finished"
                val finalWinner = when {
                    room.winner == "draw" -> GameWinner.TIE
                    room.winner == null -> GameWinner.NONE
                    room.winner == multiplayerService.currentUserId -> GameWinner.HUMAN // Gané yo
                    else -> GameWinner.ONLINE_OPPONENT // Ganó el otro
                }

                val (_, calculatedWinningLine) = gameEngine.checkWinner(newBoard)
                val previousWinner = _uiState.value.winner

                _uiState.update {
                    it.copy(
                        board = newBoard,
                        isWaitingForOpponent = room.status == "waiting",
                        onlineStatusText = if (room.status == "waiting") "Esperando a un rival..." else if (gameIsOver) "Partida terminada" else if(isMyTurnNow) "¡Tu turno!" else "Esperando movimiento...",
                        isOnlineMyTurn = isMyTurnNow && !gameIsOver,
                        isHumanTurn = (room.turn == "CREATOR"),
                        winner = finalWinner,
                        winningLine = calculatedWinningLine
                    )
                }

                // Si la partida apenas terminó, emitimos sonidos y sumamos el score
                if (previousWinner == GameWinner.NONE && finalWinner != GameWinner.NONE) {
                    handleGameEnd(newBoard, finalWinner, calculatedWinningLine)
                }
            }
        }
    }
    
    fun toggleOnlineLobby(show: Boolean) {
        if (show && _uiState.value.playerName == "Jugador") {
            _uiState.update { it.copy(showNameInputDialog = true) }
        } else {
            _uiState.update { it.copy(showOnlineLobby = show) }
        }
    }

    fun setPlayerNameAndShowLobby(name: String) {
        val finalName = if (name.isBlank()) "Jugador" else name
        _uiState.update { 
            it.copy(
                playerName = finalName,
                showNameInputDialog = false,
                showOnlineLobby = true
            ) 
        }
    }
    
    fun dismissNameInput() {
        _uiState.update { it.copy(showNameInputDialog = false) }
        setGameMode(GameMode.ONE_PLAYER)
    }

    fun resetBoard() {
        val currentState = _uiState.value
        if (currentState.gameMode == GameMode.ONLINE_MULTIPLAYER && currentState.onlineRoomId != null) {
            multiplayerService.restartGame(currentState.onlineRoomId)
        } else {
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
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        preferencesManager.saveDifficulty(difficulty)
        _uiState.update { it.copy(difficulty = difficulty) }
        resetBoard()
    }

    fun setGameMode(gameMode: GameMode) {
        preferencesManager.saveGameMode(gameMode)
        if (gameMode != GameMode.ONLINE_MULTIPLAYER) {
            roomObserveJob?.cancel()
            _uiState.update { it.copy(onlineRoomId = null) }
        }
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
