package com.herry.libs.util

import android.app.Activity
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class AppActivityManager {
    interface OnGetAppActivityManager {
        fun getAppActivityManager(): AppActivityManager?
    }

    private val activityStack: LinkedList<Activity> = LinkedList()

    fun addActivity(activity: Activity?) {
        activity ?: return
        activityStack.add(activity)
    }

    fun removeActivity(activity: Activity?) {
        activity ?: return
        activityStack.remove(activity)
    }

    fun isExistActivityOnRunningTask(activityClass: Class<*>?): Boolean {
        return getActivity(activityClass) != null
    }

    fun getActivity(activityClass: Class<*>?): Activity? {
        activityClass ?: return null

        for (activity in activityStack) {
            if (activity.javaClass == activityClass) {
                return activity
            }
        }
        return null
    }

    fun getActivities(): MutableList<Activity> = activityStack

    fun clearChildActivities(parentActivityClass: Class<*>, excludeActivityClass: Class<*>?, latest: Boolean) {
        if (isExistActivityOnRunningTask(parentActivityClass)) {
            var parentActivityPosition = -1

            // find parent activity position
            if (latest) {
                for (index in activityStack.indices.reversed()) {
                    val activity = activityStack[index]
                    if (activity.javaClass == parentActivityClass) {
                        parentActivityPosition = index
                        break
                    }
                }
            } else {
                for (index in activityStack.indices) {
                    val activity = activityStack[index]
                    if (activity.javaClass == parentActivityClass) {
                        parentActivityPosition = index
                        break
                    }
                }
            }

            if (0 > parentActivityPosition) {
                return
            }

            for (index in activityStack.indices.reversed()) {
                if (index == parentActivityPosition) {
                    break
                }
                val activity = activityStack[index]
                if (null != excludeActivityClass && activity.javaClass == excludeActivityClass) {
                    continue
                }
                activity.finish()
            }
        }
    }

    fun clearParentActivities(baseActivityClass: Class<*>, excludeActivityClass: Class<*>?) {
        if (isExistActivityOnRunningTask(baseActivityClass)) {
            // gets position of base class
            var baseActivityPosition = 0
            for (index in activityStack.indices.reversed()) {
                val activity = activityStack[index]
                if (activity.javaClass == baseActivityClass) {
                    baseActivityPosition = index
                    break
                }
            }

            for (index in baseActivityPosition - 1 downTo 0) {
                val activity = activityStack[index]
                if (null != excludeActivityClass && activity.javaClass == excludeActivityClass) {
                    continue
                }

                activity.finish()
            }
        }
    }
}