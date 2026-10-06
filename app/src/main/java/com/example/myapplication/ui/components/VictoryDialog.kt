package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.GameWinner

/**
 * Diálogo flotante de notificación de resultado de partida.
 */
@Composable
fun VictoryDialog(
    winner: GameWinner,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    if (winner == GameWinner.NONE) return

    val (title, message) = when (winner) {
        GameWinner.HUMAN -> Pair("Victoria del Jugador", "Has derrotado a la Computadora.")
        GameWinner.COMPUTER -> Pair("Victoria de la CPU", "La computadora ha ganado esta partida.")
        GameWinner.TIE -> Pair("Empate", "La partida ha finalizado sin un ganador.")
        GameWinner.NONE -> Pair("", "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Jugar de Nuevo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ver Tablero")
            }
        }
    )
}
