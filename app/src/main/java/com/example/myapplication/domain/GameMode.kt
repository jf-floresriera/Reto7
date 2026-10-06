package com.example.myapplication.domain

/**
 * Modos de juego disponibles en Tres en Raya.
 */
enum class GameMode(val displayName: String) {
    /** Modo 1 Jugador contra la Inteligencia Artificial. */
    ONE_PLAYER("1 Player (vs AI)"),

    /** Modo 2 Jugadores local en el mismo dispositivo. */
    TWO_PLAYERS("2 Players (Local)"),
    
    /** Modo Multijugador Online a través de Firebase. */
    ONLINE_MULTIPLAYER("Multiplayer Online")
}
