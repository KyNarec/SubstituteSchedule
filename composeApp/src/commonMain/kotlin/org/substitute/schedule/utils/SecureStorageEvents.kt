package org.substitute.schedule.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SecureStorageEvents {
    private val _booleanUpdates = MutableSharedFlow<Pair<String, Boolean>>(replay = 1)
    private val _stringUpdates = MutableSharedFlow<Pair<String, String>>(replay = 1)
    private val _enumUpdates = MutableSharedFlow<Pair<String, Enum<*>>>(replay = 1)
    val booleanUpdates: SharedFlow<Pair<String, Boolean>> = _booleanUpdates.asSharedFlow()
    val stringUpdates: SharedFlow<Pair<String, String>> = _stringUpdates.asSharedFlow()

    val enumUpdates: SharedFlow<Pair<String, Enum<*>>> = _enumUpdates.asSharedFlow()

    fun emitBooleanUpdate(key: String, value: Boolean) {
        _booleanUpdates.tryEmit(key to value)
    }

    fun emitStringUpdate(key: String, value: String) {
        _stringUpdates.tryEmit(key to value)
    }

    fun emitEnumUpdate(key: String, value: Enum<*>) {
        _enumUpdates.tryEmit(key to value)
    }
}