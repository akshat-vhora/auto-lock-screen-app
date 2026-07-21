package com.screen.autolocker.backup

import android.content.Context
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.SavedThemePreset
import com.screen.autolocker.history.HistoryItem
import com.screen.autolocker.history.HistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class BackupResult(
    val success: Boolean,
    val message: String,
    val path: String? = null
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) {

    private val backupDir: File
        get() {
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            return File(baseDir, "backups").apply { mkdirs() }
        }

    fun backupThemes(): BackupResult = runCatching {
        val settings = settingsRepository.snapshot()
        val file = File(backupDir, "themes_backup.json")
        val json = JSONObject()
            .put("version", 1)
            .put("theme", settings.theme)
            .put("amoledPolish", settings.amoledPolish)
            .put(
                "savedThemes",
                JSONArray().apply {
                    settings.savedThemes.forEach { theme ->
                        put(JSONObject().put("name", theme.name).put("value", theme.value))
                    }
                }
            )
        file.writeText(json.toString())
        BackupResult(true, "Themes backed up", file.absolutePath)
    }.getOrElse {
        BackupResult(false, it.message ?: "Theme backup failed")
    }

    fun restoreThemes(): BackupResult = runCatching {
        val file = File(backupDir, "themes_backup.json")
        if (!file.exists()) {
            return BackupResult(false, "No theme backup found")
        }
        val json = JSONObject(file.readText())
        validateVersion(json)
        val theme = json.optString("theme").takeIf { it.isNotBlank() }
            ?: throw JSONException("Missing theme")
        val savedThemesArray = json.optJSONArray("savedThemes")
            ?: throw JSONException("Missing savedThemes")
        val savedThemes = buildList {
            for (index in 0 until savedThemesArray.length()) {
                val item = savedThemesArray.optJSONObject(index)
                    ?: throw JSONException("Invalid saved theme entry")
                val name = item.optString("name").trim()
                val value = item.optString("value").trim()
                if (name.isBlank() || value.isBlank()) {
                    throw JSONException("Invalid saved theme data")
                }
                add(SavedThemePreset(name, value))
            }
        }
        runBlocking {
            settingsRepository.restoreThemeState(
                theme = theme,
                amoled = json.optBoolean("amoledPolish", false),
                savedThemes = savedThemes
            )
        }
        BackupResult(true, "Themes restored", file.absolutePath)
    }.getOrElse {
        BackupResult(false, it.message ?: "Theme restore failed")
    }

    fun backupHistory(): BackupResult = runCatching {
        val file = File(backupDir, "history_backup.json")
        val items = runBlocking { historyRepository.snapshot() }
        val json = JSONObject()
            .put("version", 1)
            .put(
                "history",
                JSONArray().apply {
                    items.forEach { item ->
                        put(
                            JSONObject()
                                .put("minutes", item.minutes)
                                .put("timestamp", item.timestamp)
                                .put("status", item.status)
                                .put("extendedMinutes", item.extendedMinutes)
                        )
                    }
                }
            )
        file.writeText(json.toString())
        BackupResult(true, "History backed up", file.absolutePath)
    }.getOrElse {
        BackupResult(false, it.message ?: "History backup failed")
    }

    fun restoreHistory(): BackupResult = runCatching {
        val file = File(backupDir, "history_backup.json")
        if (!file.exists()) {
            return BackupResult(false, "No history backup found")
        }
        val json = JSONObject(file.readText())
        validateVersion(json)
        val historyArray = json.optJSONArray("history")
            ?: throw JSONException("Missing history")
        val items = buildList {
            for (index in 0 until historyArray.length()) {
                val item = historyArray.optJSONObject(index)
                    ?: throw JSONException("Invalid history entry")
                val minutes = item.optInt("minutes", -1)
                val timestamp = item.optLong("timestamp", -1L)
                val status = item.optString("status").trim()
                val extendedMinutes = item.optInt("extendedMinutes", 0)
                if (minutes < 0 || timestamp <= 0L || status.isBlank() || extendedMinutes < 0) {
                    throw JSONException("Invalid history data")
                }
                add(
                    HistoryItem(
                        id = 0L,
                        minutes = minutes,
                        timestamp = timestamp,
                        status = status,
                        extendedMinutes = extendedMinutes
                    )
                )
            }
        }
        runBlocking { historyRepository.replaceAll(items) }
        BackupResult(true, "History restored", file.absolutePath)
    }.getOrElse {
        BackupResult(false, it.message ?: "History restore failed")
    }

    private fun validateVersion(json: JSONObject) {
        if (json.optInt("version", -1) != 1) {
            throw JSONException("Unsupported backup version")
        }
    }
}
