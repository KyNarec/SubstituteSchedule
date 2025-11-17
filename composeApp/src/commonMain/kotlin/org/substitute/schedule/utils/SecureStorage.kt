package org.substitute.schedule.utils

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun observeString(key: String): Flow<String?>


    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean
    fun observeBoolean(key: String): Flow<Boolean>


    fun <T : Enum<T>> putEnum(key: String, value: T)
    fun <T : Enum<T>> getEnum(key: String, enumClass: KClass<T>): T?
    fun <T : Enum<T>> observeEnum(key: String, enumClass: KClass<T>): Flow<T?>

    fun observeContains(key: String): Flow<Boolean>
    fun remove(key: String)
}