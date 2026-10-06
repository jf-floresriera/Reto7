package com.example.myapplication.domain

enum class GameWinner {
    NONE,
    HUMAN,
    COMPUTER,
    PLAYER_2, // Para 2 players locales
    ONLINE_OPPONENT, // Para online
    TIE
}

data class GameState(
    val board: List<BoardTile> = List(9) { BoardTile.EMPTY },
    val isHumanTurn: Boolean = true,
    val winner: GameWinner = GameWinner.NONE,
    val winningLine: List<Int>? = null,
    val humanWins: Int = 0,
    val computerWins: Int = 0,
    val ties: Int = 0,
    val difficulty: DifficultyLevel = DifficultyLevel.EXPERT,
    val gameMode: GameMode = GameMode.ONE_PLAYER,
    val soundEnabled: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.CLASSIC,
    val showGameModeDialog: Boolean = false,
    val showDifficultyDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val isCpuThinking: Boolean = false,
    
    // -- Propiedades para modo Online --
    val showOnlineLobby: Boolean = false, // Mostrar el lobby
    val onlineRoomId: String? = null,     // ID de la sala actual si está conectado
    val isWaitingForOpponent: Boolean = false, // Creador esperando
    val onlineStatusText: String = "",    // Mensaje descriptivo de estado
    val isOnlineMyTurn: Boolean = false,  // Define si en online es el turno de este dispositivo
    val showNameInputDialog: Boolean = false, // Pide el nombre de usuario
    val playerName: String = "Jugador"    // Nombre de este usuario
) {
    val isGameOver: Boolean
        get() = winner != GameWinner.NONE
}
