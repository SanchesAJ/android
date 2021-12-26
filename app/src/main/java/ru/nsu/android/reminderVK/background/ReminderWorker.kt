package ru.nsu.android.reminderVK.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.nsu.android.reminderVK.data.repository.ToDoItemRepository
import ru.nsu.android.reminderVK.util.NotificationMaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReminderWorker(private val context: Context, private val workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    private lateinit var reminderWorkerJob: Job

    override fun doWork(): Result {

        reminderWorkerJob = Job()
        val reminderWorkerScope = CoroutineScope(Dispatchers.IO + reminderWorkerJob)
        val toDoItemRepository = ToDoItemRepository(context)
        val itemId = workerParameters.inputData.getLong("toDoItemId", 0L)

        reminderWorkerScope.launch {
            val toDoItem = toDoItemRepository.getItem(itemId)
            if (!toDoItem.completed && toDoItem.alert) {
                toDoItem.alert = false
                toDoItem.backgroundWorkUUID = null
                toDoItemRepository.update(toDoItem)
                NotificationMaker.showVKNotification(context, toDoItem)
                NotificationMaker.showSingleNotification(context, toDoItem.description)
            }
        }

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()

        reminderWorkerJob.cancel()
    }
}