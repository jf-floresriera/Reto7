package com.example.myapplication.domain

import kotlin.random.Random

/**
 * Motor de lógica de negocio para Tres en Raya.
 */
class TicTacToeGameEngine {

    companion object {
        val WINNING_COMBINATIONS = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
    }

    fun checkWinner(board: List<BoardTile>): Pair<GameWinner, List<Int>?> {
        for (combination in WINNING_COMBINATIONS) {
            val (a, b, c) = combination
            if (board[a] != BoardTile.EMPTY && board[a] == board[b] && board[b] == board[c]) {
                val winner = if (board[a] == BoardTile.HUMAN) GameWinner.HUMAN else GameWinner.COMPUTER
                return Pair(winner, combination)
            }
        }

        if (board.none { it == BoardTile.EMPTY }) {
            return Pair(GameWinner.TIE, null)
        }

        return Pair(GameWinner.NONE, null)
    }

    fun getAvailableMoves(board: List<BoardTile>): List<Int> {
        return board.indices.filter { board[it] == BoardTile.EMPTY }
    }

    fun getCpuMove(board: List<BoardTile>, difficulty: DifficultyLevel): Int? {
        val availableMoves = getAvailableMoves(board)
        if (availableMoves.isEmpty()) return null

        return when (difficulty) {
            DifficultyLevel.EASY -> getEasyMove(availableMoves)
            DifficultyLevel.HARDER -> getMediumMove(board, availableMoves)
            DifficultyLevel.EXPERT -> getHardMove(board, availableMoves)
        }
    }

    private fun getEasyMove(availableMoves: List<Int>): Int {
        return availableMoves[Random.nextInt(availableMoves.size)]
    }

    private fun getMediumMove(board: List<BoardTile>, availableMoves: List<Int>): Int {
        val winningMove = findWinningMove(board, BoardTile.COMPUTER, availableMoves)
        if (winningMove != null) return winningMove

        val blockingMove = findWinningMove(board, BoardTile.HUMAN, availableMoves)
        if (blockingMove != null) return blockingMove

        return getEasyMove(availableMoves)
    }

    private fun getHardMove(board: List<BoardTile>, availableMoves: List<Int>): Int {
        val winningMove = findWinningMove(board, BoardTile.COMPUTER, availableMoves)
        if (winningMove != null) return winningMove

        val blockingMove = findWinningMove(board, BoardTile.HUMAN, availableMoves)
        if (blockingMove != null) return blockingMove

        if (availableMoves.contains(4)) return 4

        val corners = listOf(0, 2, 6, 8).filter { availableMoves.contains(it) }
        if (corners.isNotEmpty()) {
            return corners[Random.nextInt(corners.size)]
        }

        return getEasyMove(availableMoves)
    }

    private fun findWinningMove(
        board: List<BoardTile>,
        tileType: BoardTile,
        availableMoves: List<Int>
    ): Int? {
        for (move in availableMoves) {
            val simulatedBoard = board.toMutableList().apply { set(move, tileType) }
            val (winner, _) = checkWinner(simulatedBoard)
            if ((tileType == BoardTile.COMPUTER && winner == GameWinner.COMPUTER) ||
                (tileType == BoardTile.HUMAN && winner == GameWinner.HUMAN)
            ) {
                return move
            }
        }
        return null
    }
}
