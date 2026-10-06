package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.domain.AppTheme
import com.example.myapplication.domain.DifficultyLevel
import com.example.myapplication.domain.GameMode

/**
 * Gestor de persistencia SharedPreferences ttt_prefs.
 */
class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ttt_prefs"
        private const val KEY_HUMAN_WINS = "mHumanWins"
        private const val KEY_COMPUTER_WINS = "mComputerWins"
        private const val KEY_TIES = "mTies"
        private const val KEY_DIFFICULTY = "mDifficultyLevel"
        private const val KEY_GAME_MODE = "mGameMode"
        private const val KEY_SOUND_ENABLED = "mSoundEnabled"
        private const val KEY_THEME = "mThemeChoice"
    }

    fun saveScores(humanWins: Int, computerWins: Int, ties: Int) {
        sharedPreferences.edit()
            .putInt(KEY_HUMAN_WINS, humanWins)
            .putInt(KEY_COMPUTER_WINS, computerWins)
            .putInt(KEY_TIES, ties)
            .apply()
    }

    fun getHumanWins(): Int = sharedPreferences.getInt(KEY_HUMAN_WINS, 0)
    fun getComputerWins(): Int = sharedPreferences.getInt(KEY_COMPUTER_WINS, 0)
    fun getTies(): Int = sharedPreferences.getInt(KEY_TIES, 0)

    fun saveDifficulty(difficulty: DifficultyLevel) {
        sharedPreferences.edit()
            .putString(KEY_DIFFICULTY, difficulty.name)
            .apply()
    }

    fun getDifficulty(): DifficultyLevel {
        val savedName = sharedPreferences.getString(KEY_DIFFICULTY, DifficultyLevel.EXPERT.name)
        return try {
            DifficultyLevel.valueOf(savedName ?: DifficultyLevel.EXPERT.name)
        } catch (e: Exception) {
            DifficultyLevel.EXPERT
        }
    }

    fun saveGameMode(gameMode: GameMode) {
        sharedPreferences.edit()
            .putString(KEY_GAME_MODE, gameMode.name)
            .apply()
    }

    fun getGameMode(): GameMode {
        val savedMode = sharedPreferences.getString(KEY_GAME_MODE, GameMode.ONE_PLAYER.name)
        return try {
            GameMode.valueOf(savedMode ?: GameMode.ONE_PLAYER.name)
        } catch (e: Exception) {
            GameMode.ONE_PLAYER
        }
    }

    fun saveSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SOUND_ENABLED, enabled)
            .apply()
    }

    fun isSoundEnabled(): Boolean = sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true)

    fun saveTheme(theme: AppTheme) {
        sharedPreferences.edit()
            .putString(KEY_THEME, theme.name)
            .apply()
    }

    fun getTheme(): AppTheme {
        val savedTheme = sharedPreferences.getString(KEY_THEME, AppTheme.CLASSIC.name)
        return try {
            AppTheme.valueOf(savedTheme ?: AppTheme.CLASSIC.name)
        } catch (e: Exception) {
            AppTheme.CLASSIC
        }
    }

    fun resetScores() {
        sharedPreferences.edit()
            .putInt(KEY_HUMAN_WINS, 0)
            .putInt(KEY_COMPUTER_WINS, 0)
            .putInt(KEY_TIES, 0)
            .apply()
    }
}
