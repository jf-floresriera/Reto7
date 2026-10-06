package com.example.myapplication.domain

enum class BoardTile(val symbol: String) {
    EMPTY(""),
    HUMAN("X"),
    COMPUTER("O"),
    PLAYER_X("X"), // Creador de la sala online (Jugador 1)
    PLAYER_O("O")  // Oponente en online (Jugador 2)
}
