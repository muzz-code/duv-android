package com.ebookfrenzy.duvproject.ui.entertainer.setup

import android.app.Activity
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar

/** Snackbar on the activity's content, so it stays visible after a screen closes or a card is dismissed. */
fun Activity.showSnackbar(message: CharSequence) =
    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()

fun Activity.showSnackbar(messageRes: Int) = showSnackbar(getString(messageRes))

/**
 * Shows a set-up result. [fields] maps backend request field names (e.g. "stageName", "nextOfKin.phone") to the
 * boxes that hold them, so field errors from the server appear on the right box. Returns true when it was a save.
 */
fun Activity.handleSetupEvent(event: SetupEvent, fields: Map<String, EditText> = emptyMap()): Boolean = when (event) {
    is SetupEvent.Saved -> {
        showSnackbar(event.message)
        true
    }
    is SetupEvent.Invalid -> {
        showSnackbar(event.message)
        false
    }
    is SetupEvent.ServerError -> {
        event.fieldErrors.forEach { (name, message) -> fields[name]?.error = message }
        showSnackbar(event.message)
        false
    }
}
