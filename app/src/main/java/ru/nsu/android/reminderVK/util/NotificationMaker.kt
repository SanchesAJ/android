package ru.nsu.android.reminderVK.util

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import com.vk.sdk.api.friends.dto.FriendsGetFieldsResponse
import com.vk.sdk.api.groups.dto.GroupsGroupFull
import com.vk.sdk.api.messages.MessagesService
import com.vk.sdk.api.wall.WallService
import com.vk.sdk.api.wall.dto.WallAppPost
import com.vk.sdk.api.wall.dto.WallPostResponse

import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.nsu.android.reminderVK.R
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.ui.MainActivity
import java.util.*






class NotificationMaker {

    companion object {
        const val GENERAL_CHANNEL = "general_channel_id"
        const val SINGLE_CHANNEL = "single_channel_id"
        private const val GENERAL_TYPE = 1
        private const val SINGLE_TYPE = 2


        @ExperimentalCoroutinesApi
        fun showVKNotification(context: Context, count: ToDoItem) {
            val title = count.title
            val text = count.description
            val deadline = count.deadline?.let { Date(it) }.toString()
            val userId = VK.getUserId()
            val token = VKAccessToken.KEYS[0]

            val mess = MessagesService()


            VK.execute(WallService().wallPost(VK.getUserId(),true,message = "Hello world"),
                object: VKApiCallback<WallPostResponse>{
                    override fun fail(error: Exception) {
                        showError(context,"Error: cant post remind on wall")
                    }
                    override fun success(result: WallPostResponse) {
                        TODO("Not yet implemented")
                    }
                }
            )



        }

        fun showSingleNotification(context: Context, text: String) {
            val title = context.getString(R.string.single_notification_title)
            showNotification(context, title, text, SINGLE_TYPE)
        }

        private fun showNotification(context: Context, title: String, text: String, type: Int) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, type, intent, 0)

            val channel = when (type) {
                GENERAL_TYPE -> GENERAL_CHANNEL
                SINGLE_TYPE -> SINGLE_CHANNEL
                else -> GENERAL_CHANNEL
            }

            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(31449600000L)

            if (type == GENERAL_TYPE) {
                notification.setOnlyAlertOnce(true)
            }

            if (type == SINGLE_TYPE) {
                notification.setAutoCancel(true)
            }

            with(NotificationManagerCompat.from(context)) {
                notify(type, notification.build())
            }
        }

        private fun showError(context: Context, mes: String){
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(context, mes, duration)
            toast.show()
        }
    }
}