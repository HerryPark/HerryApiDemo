package com.herry.libs.util

import android.os.Bundle
import androidx.core.os.BundleCompat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@Suppress("MemberVisibilityCanBePrivate", "unused")
object BundleUtil {
    /**
     * Retrieves a Serializable object from the bundle with the given key and casts it to the specified class.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @param clazz The class to cast the data to.
     * @return The Serializable object if found and cast successfully, null otherwise.
     */
    fun <T : Serializable> getSerializableData(bundle: Bundle?, key: String, clazz: Class<T>): T? {
        bundle ?: return null
        return BundleCompat.getSerializable(bundle, key, clazz)
    }

    /**
     * Retrieves a Serializable object from the bundle with the given key and casts it to the specified KClass.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @param kClazz The KClass to cast the data to.
     * @return The Serializable object if found and cast successfully, null otherwise.
     */
    fun <T : Serializable> getSerializableData(bundle: Bundle?, key: String, kClazz: KClass<T>): T? {
        return getSerializableData(bundle, key, kClazz.java)
    }

    /**
     * Casts a Serializable object to the specified class.
     *
     * @param data The Serializable object to cast.
     * @param clazz The class to cast the data to.
     * @return The casted object if successful, null otherwise.
     */
    fun <T : Serializable> getSerializableData(data: Serializable?, clazz: Class<T>): T? {
        return try {
            clazz.cast(data)
        } catch (_: ClassCastException) {
            null
        }
    }

    /**
     * Casts a Serializable object to the specified KClass.
     *
     * @param data The Serializable object to cast.
     * @param kClazz The KClass to cast the data to.
     * @return The casted object if successful, null otherwise.
     */
    fun <T : Serializable> getSerializableData(data: Serializable?, kClazz: KClass<T>): T? {
        return try {
            kClazz.cast(data)
        } catch (_: ClassCastException) {
            null
        }
    }

    /**
     * Stores data in the bundle with the given key.
     * If the data is Serializable, it is stored directly.
     * Otherwise, it is serialized using Kotlin serialization and stored as a String.
     *
     * @param bundle The bundle to store the data in.
     * @param key The key associated with the data.
     * @param data The data to store.
     */
    inline fun <reified T : Any> putSerializableDataToBundle(bundle: Bundle, key: String, data: T?) {
        bundle.apply {
            if (key.isEmpty() || data == null) {
                return@apply
            }

            if (data is Serializable) {
                putSerializable(key, data)
            } else {
                putString(key, Json.encodeToString(data))
            }
        }
    }

    /**
     * Retrieves data from the bundle with the given key.
     * If the data is Serializable, it is retrieved and cast to the specified type.
     * Otherwise, it is retrieved as a String, deserialized using Kotlin serialization, and cast to the specified type.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @return The retrieved data if found and cast successfully, null otherwise.
     */
    inline fun <reified T : Any> getSerializableDataFromBundle(bundle: Bundle, key: String): T? {
        if (key.isEmpty()) {
            return null
        }

        val serializableData = BundleUtil.get(bundle, key, T::class) as? Serializable
        if (serializableData != null) {
            return T::class.cast(serializableData)
        }

        val kotlinSerializationDataString = BundleUtil[bundle, key, ""]
        if (kotlinSerializationDataString.isNotBlank()) {
            val kotlinSerializationData = Json.decodeFromString<T>(kotlinSerializationDataString)
            return try {
                T::class.cast(kotlinSerializationData)
            } catch (_: ClassCastException) {
                null
            }
        }

        return null
    }

    /**
     * Retrieves a value from the bundle with the given key and casts it to the specified class.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @param clazz The class to cast the data to.
     * @return The value if found and cast successfully, null otherwise.
     */
    @Suppress("DEPRECATION")
    operator fun <T : Any> get(bundle: Bundle, key: String, clazz: Class<T>): T? {
        if (!bundle.containsKey(key)) return null
        return try {
            clazz.cast(bundle.get(key))
        } catch (_: ClassCastException) {
            null
        }
    }

    /**
     * Retrieves a value from the bundle with the given key and casts it to the specified KClass.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @param kClass The KClass to cast the data to.
     * @return The value if found and cast successfully, null otherwise.
     */
    @Suppress("DEPRECATION")
    operator fun <T : Any> get(bundle: Bundle?, key: String, kClass: KClass<T>): T? {
        bundle ?: return null
        if (!bundle.containsKey(key)) return null
        return try {
            kClass.cast(bundle.get(key))
        } catch (_: ClassCastException) {
            null
        }
    }

    /**
     * Retrieves a value from the bundle with the given key and casts it to the type of the default value.
     * If the key is not found or the cast fails, the default value is returned.
     *
     * @param bundle The bundle containing the data.
     * @param key The key associated with the data.
     * @param default The default value to return if the key is not found or the cast fails.
     * @return The value if found and cast successfully, otherwise the default value.
     */
    @Suppress("DEPRECATION")
    operator fun <T : Any> get(bundle: Bundle?, key: String, default: T): T {
        bundle ?: return default
        if (!bundle.containsKey(key)) return default
        return try {
            default::class.cast(bundle.get(key))
        } catch (_: ClassCastException) {
            null
        } ?: default
    }
}