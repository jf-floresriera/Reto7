# Reto 7: Jugando Triqui Online

Este repositorio contiene la evolución del proyecto Triqui (Tres en Raya) para dispositivos móviles Android. En este reto, hemos extendido el juego base del Reto 6 para incorporar soporte multijugador en línea utilizando **Firebase Realtime Database**, además de refinar los sonidos y su integración con el motor de audio (`SoundManager`).

---

## 🎯 Objetivos Logrados

- **Multijugador Realtime**: Utilizando Firebase Realtime Database para la sincronización entre dispositivos.
- **Creación y Unión a Salas**: Un usuario puede crear una sala de juego. Los demás usuarios verán la sala en una lista en tiempo real y podrán unirse a ella.
- **Roles de Jugador**: El creador de la sala siempre inicia (Juega con X). Quien se une, juega con O.
- **Sonidos de Victoria/Derrota Adaptados**:
  - Victoria: Suena el tono victorioso (arpegio ascendente) al ganar vs CPU o al ganar Online.
  - Derrota: Suena el tono triste (arpegio descendente) al perder vs CPU o al perder Online.

---

## 🏗️ Arquitectura y Estructura para Estudiar

Esta sección está destinada al aprendizaje continuo, detallando qué hace cada archivo importante creado/modificado.

### 1. `GameRoom.kt` (Ubicado en `data/network/`)
* **Qué hace**: Es el "Modelo de Datos" (Data Class) que se almacena y sincroniza directamente en Firebase.
* **Por qué debes estudiarlo**: Es el ejemplo perfecto de cómo modelar datos NoSQL en Firebase. Note que las propiedades tienen valores por defecto (`= ""`) obligatoriamente para que Firebase SDK sepa cómo hidratar (construir) el objeto vacío antes de asignarle valores desde la red.
* **Propiedades a entender**: `status` (waiting, playing, finished), `board` (Lista Plana en vez de `BoardTile` debido a compatibilidad de mapeo en BD).

### 2. `FirebaseMultiplayerService.kt` (Ubicado en `data/network/`)
* **Qué hace**: Abstrae todas las llamadas a la base de datos de Firebase. Es el intermediario de red.
* **Por qué debes estudiarlo**: Utiliza `Kotlin Coroutines` y `Flow` (`callbackFlow`) para transformar los "Listeners" (callbacks) basados en Java de Firebase, a Streams de datos puramente reactivos de Kotlin.
* **Funciones Clave**: 
  - `getAvailableRooms()`: Escucha en un nodo específico para listar los juegos donde `status == "waiting"`.
  - `observeRoom(roomId)`: Se suscribe a un juego específico y emite un nuevo estado cada que alguien hace un movimiento.

### 3. `TicTacToeViewModel.kt` (Modificado)
* **Qué hace**: Centraliza la lógica de presentación.
* **Por qué debes estudiarlo**: Aprenderás cómo manejar dos ramificaciones lógicas distintas: el modo *offline* local (`triggerCpuMove`) frente al modo *online* (`observeRoom`).
* **Variables Clave Añadidas al State**: `onlineRoomId`, `isWaitingForOpponent`, `isOnlineMyTurn`. Son flags (banderas booleanas/nulas) que determinan qué debe pintar Compose.

### 4. `OnlineLobbyDialog.kt` (Ubicado en `ui/components/`)
* **Qué hace**: Dialogo en Jetpack Compose que renderiza la lista de GameRooms.
* **Por qué debes estudiarlo**: Muestra cómo renderizar listas de datos que vienen de la red utilizando `LazyColumn` en Compose, de manera dinámica.

### 5. `SoundManager.kt` (Modificado)
* **Qué hace**: Reproduce frecuencias sin un archivo físico.
* **Por qué debes estudiarlo**: Se han incluido frecuencias y métricas temporales (Hz y ms) directas al PCM 16BIT con `AudioTrack` para emular tonos en modo 8-bits retro. Aquí es donde debes ajustar si quisieras usar archivos `.mp3` reemplazando los `playTone` por un reproductor `MediaPlayer.create(...)`.

---

## 🚀 Pasos para Ejecutar Localmente

> **IMPORTANTE**: Este repositorio contiene la configuración base en el `build.gradle.kts` para Firebase (dependencias instaladas), pero por razones de seguridad, no contiene el archivo `google-services.json` del entorno de desarrollo.

Para poder compilar e interactuar entre dos emuladores:

1. Ve a la consola de [Firebase](https://console.firebase.google.com).
2. Crea un proyecto (o usa el existente `reto7-20ae5`).
3. Agrega una App de Android con el paquete exacto: `com.example.myapplication`.
4. Descarga el `google-services.json` y colócalo dentro de la carpeta `app/` en tu IDE.
5. Asegúrate de habilitar **Realtime Database** y que sus reglas de lectura y escritura estén en modo prueba (`true` y `true`).
6. Compila y ejecuta la App en **dos Emuladores diferentes**. 
7. En un emulador, selecciona el modo de juego "Multiplayer Online" y haz click en "Crear".
8. En el otro emulador, selecciona el mismo modo, verás la sala creada; dale click a "UNIRSE".