package ru.nsu.android.reminderVK.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.nsu.android.reminderVK.data.repository.ToDoItemRepository
import ru.nsu.android.reminderVK.util.NotificationMaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnBootBroadcastReceiver : BroadcastReceiver() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            requireNotNull(context)
            val toDoItemRepository = ToDoItemRepository(context)

            applicationScope.launch {
            }
        }
    }
}