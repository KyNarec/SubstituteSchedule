package org.substitute.schedule.utils

import kotlinx.coroutines.flow.Flow

interface SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean

    fun observeBoolean(key: String): Flow<Boolean>

    fun observeString(key: String): Flow<String?>
    fun remove(key: String)
}