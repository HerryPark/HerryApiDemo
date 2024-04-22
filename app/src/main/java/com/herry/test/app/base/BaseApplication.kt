package com.herry.test.app.base

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import com.herry.libs.log.Trace
import com.herry.libs.util.AppActivityManager
import com.herry.libs.util.preferences.PreferenceHelper
import java.util.concurrent.atomic.AtomicBoolean

class BaseApplication: Application(), AppActivityManager.OnGetAppActivityManager {
    private var isBackground = true

    internal val appActivityManager = AppActivityManager()

    private var pausedAppTime = 0L

    @Suppress("unused")
    enum class RestartApp {
        NONE, SSO, SESSION_TIMEOUT
    }

    private var restartApp = RestartApp.NONE

    override fun onCreate() {
        super.onCreate()

        Trace.setDebug(true)

        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(
                object : ActivityLifecycleCallbacks {
                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityResumed(activity: Activity) {
                        notifyBackground(false)
                    }

                    override fun onActivityStarted(activity: Activity) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                        appActivityManager.removeActivity(activity)
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                    }

                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                        appActivityManager.addActivity(activity)
                    }
                })

        val screenOffFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                notifyBackground(true)
            }
        }, screenOffFilter)

        PreferenceHelper.init(
            context = { applicationContext }
        )
    }

    override fun onTerminate() {
        // reset aloha instance datas
        setLoggedIn(false)

        super.onTerminate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            notifyBackground(true)
        }
    }

    private fun notifyBackground(isBackground: Boolean) {
        val changedBackgroundStatus = this.isBackground != isBackground
        if (changedBackgroundStatus) {
            if (!isBackground) {
                if (0 < pausedAppTime && pausedAppTime + APP_SESSION_TIME_OUT < System.currentTimeMillis()) { // restart application
                    setRestartApplication(RestartApp.SESSION_TIMEOUT)
                } else {
                    pausedAppTime = 0L
                }
            } else {
                pausedAppTime = System.currentTimeMillis()
            }
            this.isBackground = isBackground
        }
    }

    private fun setRestartApplication(causedBy: RestartApp) {
        restartApp = causedBy
    }

    fun isNeedRestartApp(): Boolean = restartApp != RestartApp.NONE

    fun resetRestartApp() {
        setRestartApplication(RestartApp.NONE)
    }

    companion object {
        // app session time out duration
        private const val APP_SESSION_TIME_OUT = 30 * 60 * 1000L
        private val loggedIn = AtomicBoolean(false)

        fun isLoggedIn() : Boolean = loggedIn.get()

        fun setLoggedIn(loggedIn: Boolean) {
            this.loggedIn.set(loggedIn)
        }
    }

    override fun getAppActivityManager(): AppActivityManager = appActivityManager
}