package com.ebookfrenzy.duvproject.ui.entertainer.setup

import android.content.Context
import com.google.gson.Gson

/**
 * Nigeria's 37 states (including FCT Abuja) and their 774 local government areas, read once from
 * assets/nigeria_states_lgas.json (see the file's "source" field for where the list comes from).
 */
object NigeriaLocations {

    private data class StateEntry(val name: String, val lgas: List<String>)
    private data class LocationsFile(val states: List<StateEntry>)

    @Volatile
    private var byState: Map<String, List<String>>? = null

    private fun load(context: Context): Map<String, List<String>> = byState ?: synchronized(this) {
        byState ?: context.applicationContext.assets.open("nigeria_states_lgas.json").bufferedReader().use { reader ->
            Gson().fromJson(reader, LocationsFile::class.java).states.associate { it.name to it.lgas }
        }.also { byState = it }
    }

    /** State names in alphabetical order. */
    fun states(context: Context): List<String> = load(context).keys.toList()

    /** LGAs for [state], or an empty list if the state isn't known. */
    fun lgas(context: Context, state: String): List<String> = load(context)[state].orEmpty()
}
