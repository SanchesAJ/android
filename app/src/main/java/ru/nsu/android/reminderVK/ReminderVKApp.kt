package ru.nsu.android.reminderVK

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.nsu.android.reminderVK.util.NotificationMaker

class ReminderVKApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            // token expired
        }
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
        VK.addTokenExpiredHandler(tokenTracker)
    }


    private fun delayedInit() {
        applicationScope.launch {
            setupNotificationChannels()
        }
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalName = getString(R.string.general_channel_name)
            val generalDescriptionText = getString(R.string.general_channel_description)
            val generalImportance = NotificationManager.IMPORTANCE_LOW
            val generalNotificationChannel = NotificationChannel(NotificationMaker.GENERAL_CHANNEL, generalName, generalImportance).apply {
                description = generalDescriptionText
            }

            val singleName = getString(R.string.single_channel_name)
            val singleDescriptionText = getString(R.string.single_channel_description)
            val singleImportance = NotificationManager.IMPORTANCE_DEFAULT
            val singleNotificationChannel = NotificationChannel(NotificationMaker.SINGLE_CHANNEL, singleName, singleImportance).apply {
                description = singleDescriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(generalNotificationChannel)
            notificationManager.createNotificationChannel(singleNotificationChannel)
        }
    }
}