package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.TicTacToeScreen
import com.example.myapplication.ui.TicTacToeViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * ============================================================================
 * CONCEPTO EDUCATIVO: PUNTO DE ENTRADA PRINCIPAL (MAINACTIVITY)
 * ============================================================================
 * La 'MainActivity' actúa como el contenedor principal de la aplicación.
 *
 * Puntos clave:
 * 1. 'enableEdgeToEdge()': Permite un diseño moderno sin bordes negros en barras del sistema.
 * 2. 'viewModels<TicTacToeViewModel>()': Delegado de la librería de Jetpack Lifecycle que
 *    crea y conserva la instancia del ViewModel a través de los ciclos de vida de la Activity.
 * 3. 'setContent': Inicializa la jerarquía de vistas de Jetpack Compose en lugar de setContentView(R.layout...).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: TicTacToeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicTacToeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
