package com.example.myapplication.domain

/**
 * Temas visuales de fichas y símbolos para el juego Tres en Raya.
 */
enum class AppTheme(
    val displayName: String,
    val player1Symbol: String,
    val player2Symbol: String
) {
    CLASSIC("Clásico (X vs O)", "X", "O"),
    COSTA("Costa (🐚 vs ⭐)", "🐚", "⭐"),
    LLANO("Llanos (🐓 vs 🪇)", "🐓", "🪇"),
    VAQUERO("Vaquero Country (🤠 vs 🐎)", "🤠", "🐎")
}
