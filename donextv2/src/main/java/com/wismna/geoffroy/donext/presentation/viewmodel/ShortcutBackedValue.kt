package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A form field that a quick-add shortcut in the title can fill in live.
 * A new shortcut value replaces the field; removing the shortcut restores what the field held before;
 * a change made by hand wins until the title brings a different shortcut value.
 */
internal class ShortcutBackedValue<T>(initial: T) {
    var value by mutableStateOf(initial)
        private set

    private var valueBeforeShortcut: T = initial
    private var lastShortcut: T? = null
    private var isSetByShortcut = false

    fun setManually(newValue: T) {
        value = newValue
        isSetByShortcut = false
    }

    fun applyShortcut(shortcut: T?) {
        if (shortcut == lastShortcut) return
        lastShortcut = shortcut
        if (shortcut != null) {
            if (!isSetByShortcut) valueBeforeShortcut = value
            value = shortcut
            isSetByShortcut = true
        } else if (isSetByShortcut) {
            value = valueBeforeShortcut
            isSetByShortcut = false
        }
    }

    fun reset(newValue: T) {
        value = newValue
        valueBeforeShortcut = newValue
        lastShortcut = null
        isSetByShortcut = false
    }
}
