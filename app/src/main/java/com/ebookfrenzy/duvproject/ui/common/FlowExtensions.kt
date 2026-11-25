package com.ebookfrenzy.duvproject.ui.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Collects [flow] only while [owner] is at least STARTED, so the UI never updates in the background. */
fun <T> LifecycleOwner.collectWhenStarted(flow: Flow<T>, action: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

/** Fragment version: tied to the view's lifecycle, not the fragment's. */
fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: suspend (T) -> Unit) =
    viewLifecycleOwner.collectWhenStarted(flow, action)
