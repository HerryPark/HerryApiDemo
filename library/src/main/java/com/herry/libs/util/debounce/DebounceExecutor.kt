package com.herry.libs.util.debounce

import androidx.annotation.IntRange
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * // creates
 * val debounceExecutor = DebounceExecutor()
 * // debounce
 * debounceExecutor.debounce(
 *     runnable = {
 *         // result is on the working thread
 *     },
 *     delay = 33, // debounce time
 *     unit = TimeUnit.MILLISECONDS
 * )
 * // cancel debounce
 * debounceExecutor.cancel()
 */
class DebounceExecutor {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val delayedMap: ConcurrentHashMap<String, Future<*>> = ConcurrentHashMap<String, Future<*>>()

    private val key = "debounce"
    /**
     * Debounces `callable` by `debounceTime`, i.e., schedules it to be executed after `debounceTime`,
     * or cancels its execution if the method is called with the same key within the `debounceTime` again.
     */
    fun debounce(runnable: Runnable, @IntRange(from = 1L) debounceTime: Long, timeUnit: TimeUnit) {
        try {
            val pendingFeature = delayedMap.put(
                /* key = */ key,
                /* value = */ scheduler.schedule(
                    /* command = */ {
                        try {
                            runnable.run()
                        } finally {
                            delayedMap.remove(key)
                        }
                    },
                    /* delay = */ debounceTime,
                    /* unit = */ timeUnit
                )
            )
            // cancel the previous schedule
            pendingFeature?.cancel(true)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun cancel() {
        try {
            // cancel pending schedule
            delayedMap[key]?.cancel(true)
            delayedMap.clear()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}