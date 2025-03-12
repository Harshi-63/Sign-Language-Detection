package com.example.signlanguagedetection_app.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.*

private val LocalOnBackPressedDispatcherOwner =
    staticCompositionLocalOf<OnBackPressedDispatcherOwner?> { null }

// Directly access the dispatcher without derivedStateOf
private val LocalBackPressedDispatcher
    @Composable get() = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

private class ComposableBackNavigationHandler(enabled: Boolean) : OnBackPressedCallback(enabled) {
    lateinit var onBackPressed: () -> Unit

    override fun handleOnBackPressed() {
        onBackPressed()
    }
}

@Composable
internal fun ComposableHandler(
    enabled: Boolean = true,
    onBackPressed: () -> Unit
) {
    // Retrieve the dispatcher directly from the CompositionLocal
    val dispatcher = LocalBackPressedDispatcher ?: return

    // Create and remember the back navigation handler
    val handler = remember { ComposableBackNavigationHandler(enabled) }

    // Update the handler with the provided callback
    handler.onBackPressed = onBackPressed
    handler.isEnabled = enabled

    DisposableEffect(dispatcher) {
        // Add the callback to the dispatcher
        dispatcher.addCallback(handler)

        // Remove the callback when the composable is disposed
        onDispose { handler.remove() }
    }
}
