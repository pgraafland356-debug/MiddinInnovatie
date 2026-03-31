package com.middin.innovatie.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Clears the login session when the user removes the app from Recents (swipe away).
 * [android:stopWithTask]="false" is required so the system calls [onTaskRemoved] before stopping
 * the process; [MainActivity.onDestroy] is not always run with [isFinishing] in that scenario.
 */
class LogoutOnTaskRemovedService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onTaskRemoved(rootIntent: Intent?) {
        val app = applicationContext as MiddinApplication
        runBlocking(Dispatchers.IO) {
            app.container.userPreferences.setSession(loggedIn = false)
        }
        stopSelf()
    }
}
