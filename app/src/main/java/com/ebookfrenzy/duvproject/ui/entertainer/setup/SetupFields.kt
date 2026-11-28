package com.ebookfrenzy.duvproject.ui.entertainer.setup

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.ebookfrenzy.duvproject.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Tapping the (non-typeable) field opens a single-choice list; the choice is written into the field. */
fun EditText.pickOneOf(options: List<String>) = pickOneOf { options }

/**
 * Same, but the list is worked out when the field is tapped (e.g. LGAs for the state chosen so far). Return null to
 * cancel; [onPicked] runs after a choice is made.
 */
fun EditText.pickOneOf(onPicked: (String) -> Unit = {}, options: () -> List<String>?) = setOnClickListener {
    val list = options() ?: return@setOnClickListener
    pickFrom(list, onPicked)
}

private fun EditText.pickFrom(options: List<String>, onPicked: (String) -> Unit) {
    val current = options.indexOf(text.toString())
    MaterialAlertDialogBuilder(context)
        .setTitle(hint)
        .setSingleChoiceItems(options.toTypedArray(), current) { dialog, which ->
            setText(options[which])
            error = null
            dialog.dismiss()
            onPicked(options[which])
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

/**
 * Single choice from a long list (e.g. ~250 Nigerian banks), with a search box that filters as you type.
 * [options] is read when the field is tapped; return null to cancel.
 */
fun EditText.pickSearchable(onPicked: (String) -> Unit = {}, options: () -> List<String>?) = setOnClickListener {
    val all = options() ?: return@setOnClickListener
    val padding = resources.getDimensionPixelSize(R.dimen.setup_search_padding)
    val search = EditText(context).apply {
        hint = context.getString(R.string.setup_search_banks)
        isSingleLine = true
        inputType = android.text.InputType.TYPE_CLASS_TEXT
    }
    val empty = TextView(context).apply { visibility = View.GONE; setPadding(0, padding, 0, padding) }
    val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, all.toMutableList())
    val list = ListView(context).apply { this.adapter = adapter }
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding / 2, padding, 0)
        addView(search)
        addView(empty)
        // Fixed height so the dialog doesn't jump as the list shrinks while searching.
        val listHeight = (resources.displayMetrics.heightPixels * LIST_SCREEN_SHARE).toInt()
        addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listHeight))
    }
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(hint)
        .setView(content)
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    search.doAfterTextChanged { typed ->
        val query = typed.toString().trim()
        val matches = all.filter { it.contains(query, ignoreCase = true) }
        adapter.clear()
        adapter.addAll(matches)
        empty.text = context.getString(R.string.setup_no_banks_match, query)
        empty.visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE
    }
    list.setOnItemClickListener { _, _, position, _ ->
        val choice = adapter.getItem(position) ?: return@setOnItemClickListener
        setText(choice)
        error = null
        dialog.dismiss()
        onPicked(choice)
    }
}

/** Share of the screen height the searchable list takes. */
private const val LIST_SCREEN_SHARE = 0.5f

/** Multi-choice list; the field shows the picks joined by commas. */
fun EditText.pickManyOf(options: List<String>) = setOnClickListener {
    val chosen = text.toString().split(", ").filter { it.isNotBlank() }.toMutableSet()
    val checked = options.map { it in chosen }.toBooleanArray()
    MaterialAlertDialogBuilder(context)
        .setTitle(hint)
        .setMultiChoiceItems(options.toTypedArray(), checked) { _, which, isChecked ->
            if (isChecked) chosen += options[which] else chosen -= options[which]
        }
        .setPositiveButton(android.R.string.ok) { _, _ ->
            setText(options.filter { it in chosen }.joinToString(", "))
            error = null
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

private val dateFormat: DateTimeFormatter = SetupOptions.uiDate

/** Date field: opens the system date picker and writes dd/MM/yyyy. */
fun EditText.pickDate() = setOnClickListener {
    val start = runCatching { LocalDate.parse(text.toString(), dateFormat) }.getOrDefault(LocalDate.now())
    DatePickerDialog(context, { _, y, m, d ->
        setText(LocalDate.of(y, m + 1, d).format(dateFormat))
        error = null
    }, start.year, start.monthValue - 1, start.dayOfMonth).show()
}

/** Marks every empty field with an error and returns true only when all are filled. */
fun requireFilled(vararg fields: EditText): Boolean {
    var ok = true
    for (field in fields) {
        if (field.text.isNullOrBlank()) {
            field.error = field.context.getString(R.string.setup_required, field.hint)
            if (ok) field.requestFocus()
            ok = false
        }
    }
    return ok
}
