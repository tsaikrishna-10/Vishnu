package com.vishnu.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SavedTitleStore {
    private const val PREFS = "vishnu_library"
    private const val KEY = "saved_titles"
    private val gson = Gson()

    fun read(context: Context): List<TmdbTitle> {
        val values = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet()) ?: emptySet()
        return values.mapNotNull { runCatching { gson.fromJson(it, TmdbTitle::class.java) }.getOrNull() }
            .sortedBy { it.displayTitle.lowercase() }
    }

    fun add(context: Context, title: TmdbTitle) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val values = prefs.getStringSet(KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        values.removeAll { value ->
            runCatching { gson.fromJson(value, TmdbTitle::class.java) }.getOrNull()?.let {
                it.id == title.id && it.media_type == title.media_type
            } ?: false
        }
        values.add(gson.toJson(title))
        prefs.edit().putStringSet(KEY, values).apply()
    }

    fun remove(context: Context, title: TmdbTitle) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val values = prefs.getStringSet(KEY, emptySet())?.filterNot { value ->
            runCatching { gson.fromJson(value, TmdbTitle::class.java) }.getOrNull()?.let {
                it.id == title.id && it.media_type == title.media_type
            } ?: false
        }?.toSet() ?: emptySet()
        prefs.edit().putStringSet(KEY, values).apply()
    }
}
