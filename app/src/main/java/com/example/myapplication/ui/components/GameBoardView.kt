package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.AppTheme
import com.example.myapplication.domain.BoardTile

@Composable
fun GameBoardView(
    board: List<BoardTile>,
    winningLine: List<Int>?,
    selectedTheme: AppTheme,
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isWinningTile = winningLine?.contains(index) == true

                        TileCell(
                            tile = board[index],
                            selectedTheme = selectedTheme,
                            isWinningTile = isWinningTile,
                            onClick = { onTileClick(index) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCell(
    tile: BoardTile,
    selectedTheme: AppTheme,
    isWinningTile: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (tile != BoardTile.EMPTY) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TileScale"
    )

    val player1Color = Color(0xFF1E88E5)
    val player2Color = Color(0xFFE53935)
    val winningGold = Color(0xFFFFD700)

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isWinningTile -> winningGold.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "TileBgColor"
    )

    val borderColor = when {
        isWinningTile -> winningGold
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val displaySymbol = when (tile) {
        BoardTile.HUMAN, BoardTile.PLAYER_X -> selectedTheme.player1Symbol
        BoardTile.COMPUTER, BoardTile.PLAYER_O -> selectedTheme.player2Symbol
        BoardTile.EMPTY -> ""
    }

    val symbolColor = when (tile) {
        BoardTile.HUMAN, BoardTile.PLAYER_X -> player1Color
        BoardTile.COMPUTER, BoardTile.PLAYER_O -> player2Color
        BoardTile.EMPTY -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isWinningTile) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displaySymbol,
            color = symbolColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.scale(scale)
        )
    }
}
