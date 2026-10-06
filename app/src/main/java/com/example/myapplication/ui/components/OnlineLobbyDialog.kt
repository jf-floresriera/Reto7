package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.network.GameRoom
import com.example.myapplication.domain.AppTheme

@Composable
fun OnlineLobbyDialog(
    theme: AppTheme,
    availableRooms: List<GameRoom>,
    onCreateRoom: () -> Unit,
    onJoinRoom: (GameRoom) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lobby Multijugador",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onCreateRoom() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Crear Nueva Partida")
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Salas Disponibles:", fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.height(8.dp))

                if (availableRooms.isEmpty()) {
                    Text(
                        "No hay partidas esperando oponente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(availableRooms) { room ->
                            RoomItem(room = room, onJoinRoom = onJoinRoom)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: GameRoom, onJoinRoom: (GameRoom) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onJoinRoom(room) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Host: ${room.creatorName}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "UNIRSE",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}