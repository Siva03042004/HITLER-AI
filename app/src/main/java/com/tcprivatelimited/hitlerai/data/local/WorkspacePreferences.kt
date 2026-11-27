package com.tcprivatelimited.hitlerai.data.local

import android.content.Context
import com.tcprivatelimited.hitlerai.ui.theme.WorkspaceBackgroundPreset

class WorkspacePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun loadBackgroundPreset(): WorkspaceBackgroundPreset {
        val id = prefs.getString(KEY_BACKGROUND_PRESET, WorkspaceBackgroundPreset.OBSIDIAN.id)
        return WorkspaceBackgroundPreset.fromId(id)
    }

    fun saveBackgroundPreset(preset: WorkspaceBackgroundPreset) {
        prefs.edit().putString(KEY_BACKGROUND_PRESET, preset.id).apply()
    }

    fun isAmbientGlowEnabled(): Boolean =
        prefs.getBoolean(KEY_AMBIENT_GLOW, true)

    fun setAmbientGlowEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMBIENT_GLOW, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "hitler_ai_workspace_prefs"
        private const val KEY_BACKGROUND_PRESET = "background_preset"
        private const val KEY_AMBIENT_GLOW = "ambient_glow_enabled"
    }
}
