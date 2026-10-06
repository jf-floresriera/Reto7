package com.example.myapplication.data.network

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Servicio para manejar la conexión con Firebase Realtime Database.
 * Permite crear partidas, unirse a ellas y escuchar cambios en tiempo real.
 */
class FirebaseMultiplayerService {
    // Referencia al nodo raíz de las salas en Firebase
    private val database = FirebaseDatabase.getInstance().getReference("rooms")
    
    // Identificador único de este dispositivo/jugador en la sesión actual
    val currentUserId = UUID.randomUUID().toString()

    /**
     * Flujo reactivo (Flow) que emite la lista de salas disponibles para unirse.
     * Una sala está disponible si su estado es "waiting".
     */
    fun getAvailableRooms(): Flow<List<GameRoom>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<GameRoom>()
                for (child in snapshot.children) {
                    val room = child.getValue(GameRoom::class.java)
                    if (room != null && room.status == "waiting") {
                        rooms.add(room)
                    }
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    /**
     * Crea una nueva sala y retorna su ID.
     */
    fun createRoom(creatorName: String): String {
        val roomId = database.push().key ?: UUID.randomUUID().toString()
        val room = GameRoom(
            id = roomId,
            creatorId = currentUserId,
            creatorName = creatorName,
            status = "waiting",
            turn = "CREATOR" // El creador siempre empieza
        )
        database.child(roomId).setValue(room)
        return roomId
    }

    /**
     * Permite a un segundo jugador unirse a una sala existente.
     */
    fun joinRoom(roomId: String, opponentName: String) {
        val updates = mapOf(
            "opponentId" to currentUserId,
            "opponentName" to opponentName,
            "status" to "playing",
            "turn" to "CREATOR" // Turno inicial del creador
        )
        database.child(roomId).updateChildren(updates)
    }

    /**
     * Escucha en tiempo real los cambios del estado de una sala específica.
     */
    fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(GameRoom::class.java)
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child(roomId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Actualiza el tablero en Firebase tras un movimiento válido de un jugador.
     */
    fun updateBoard(roomId: String, newBoard: List<String>, nextTurn: String) {
        val updates = mapOf(
            "board" to newBoard,
            "turn" to nextTurn
        )
        database.child(roomId).updateChildren(updates)
    }

    /**
     * Actualiza el ganador y marca la sala como finalizada.
     */
    fun finishGame(roomId: String, winnerId: String?) {
        val updates = mapOf(
            "status" to "finished",
            "winner" to (winnerId ?: "draw")
        )
        database.child(roomId).updateChildren(updates)
    }

    /**
     * Reinicia la partida en la misma sala para jugar de nuevo.
     */
    fun restartGame(roomId: String) {
        val updates = mapOf<String, Any?>(
            "board" to List(9) { "" },
            "status" to "playing",
            "winner" to null,
            "turn" to "CREATOR"
        )
        database.child(roomId).updateChildren(updates)
    }

    /**
     * Elimina la sala, útil cuando un jugador se desconecta o sale.
     */
    fun leaveRoom(roomId: String) {
        database.child(roomId).removeValue()
    }
}