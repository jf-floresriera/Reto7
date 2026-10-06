package com.example.myapplication.data.network

import com.example.myapplication.domain.BoardTile

/**
 * Modelo de datos que representa una partida (sala) en Firebase Realtime Database.
 * Cada partida tiene un creador y un oponente.
 *
 * @property id Identificador único de la sala.
 * @property creatorId ID del usuario que crea la sala (Jugador X).
 * @property opponentId ID del usuario que se une a la sala (Jugador O).
 * @property creatorName Nombre del creador de la sala.
 * @property opponentName Nombre del oponente.
 * @property status Estado de la sala ("waiting", "playing", "finished").
 * @property board Representación plana del tablero de 3x3 (lista de 9 posiciones).
 * @property turn ID del jugador que tiene el turno actual.
 * @property winner ID del jugador ganador, o "draw" si es empate, o nulo si no ha terminado.
 */
data class GameRoom(
    val id: String = "",
    val creatorId: String = "",
    val opponentId: String? = null,
    val creatorName: String = "",
    val opponentName: String? = null,
    val status: String = "waiting", // "waiting", "playing", "finished"
    val board: List<String> = List(9) { "" }, // "" para vacío, "X" o "O"
    val turn: String = "", // Creador inicia siempre, turn = creatorId
    val winner: String? = null
) {
    // Función auxiliar para convertir el String plano al enum BoardTile usado en la App
    fun getTileAt(row: Int, col: Int): BoardTile {
        val index = row * 3 + col
        return when (board.getOrNull(index)) {
            "X" -> BoardTile.PLAYER_X
            "O" -> BoardTile.PLAYER_O
            else -> BoardTile.EMPTY
        }
    }
}