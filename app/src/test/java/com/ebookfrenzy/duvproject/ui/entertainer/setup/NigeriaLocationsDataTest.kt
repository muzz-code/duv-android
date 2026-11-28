package com.ebookfrenzy.duvproject.ui.entertainer.setup

import com.google.gson.JsonParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the bundled states/LGAs list against accidental edits. */
class NigeriaLocationsDataTest {

    private val states = JsonParser.parseString(File("src/main/assets/nigeria_states_lgas.json").readText())
        .asJsonObject.getAsJsonArray("states").map { it.asJsonObject }

    @Test
    fun `has 37 states and 774 LGAs`() {
        assertEquals(37, states.size)
        assertEquals(774, states.sumOf { it.getAsJsonArray("lgas").size() })
    }

    @Test
    fun `no state lists the same LGA twice`() {
        states.forEach { state ->
            val lgas = state.getAsJsonArray("lgas").map { it.asString }
            assertEquals(state["name"].asString, lgas.size, lgas.toSet().size)
        }
    }

    @Test
    fun `known states have their official LGA counts`() {
        val counts = states.associate { it["name"].asString to it.getAsJsonArray("lgas").size() }
        assertEquals(20, counts["Lagos"])
        assertEquals(44, counts["Kano"])
        assertEquals(6, counts["FCT Abuja"])
        assertTrue("Ikeja" in states.first { it["name"].asString == "Lagos" }.getAsJsonArray("lgas").map { it.asString })
    }
}
