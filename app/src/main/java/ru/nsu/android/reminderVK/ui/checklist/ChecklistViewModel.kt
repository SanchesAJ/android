package ru.nsu.android.reminderVK.ui.checklist

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.data.repository.ToDoItemRepository
import ru.nsu.android.reminderVK.background.ReminderWorker
import ru.nsu.android.reminderVK.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

const val TAG_NAME = "scheduledfor"

class ChecklistViewModel(application: Application, deletedDescription: String) :
    AndroidViewModel(application) {

    private val repository = ToDoItemRepository(application)
    val allToDoItems: LiveData<List<ToDoItem>>

    private var _navigateToNewToDoItem = MutableLiveData<Event<String>>()
    val navigateToNewToDoItem: LiveData<Event<String>>
        get() = _navigateToNewToDoItem

    private var _showDeletedSnackBar = MutableLiveData<Event<String>>()
    val showDeletedSnackBar: LiveData<Event<String>>
        get() = _showDeletedSnackBar

    private var _showScheduledSnackBar = MutableLiveData<Event<String>>()
    val showScheduledSnackBar: LiveData<Event<String>>
        get() = _showScheduledSnackBar

    init {
        allToDoItems = repository.allToDoItems
        if (deletedDescription != "default-nothing-deleted") {
            _showDeletedSnackBar.value = Event(deletedDescription)
        }
    }

    private fun update(toDoItem: ToDoItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(toDoItem)
    }

    fun onFabButtonClicked() = viewModelScope.launch {
        _navigateToNewToDoItem.value = Event("clicked")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun scheduleNewToDo(){
        val toDo = repository.allToDoItems.value
        for(it in toDo!!){
            if(it.alert && !it.completed && it.backgroundWorkUUID ==null)
                scheduleOrCancelRedoBackgroundTask(it)
        }
    }

    fun toggleCheckbox(toDoItem: ToDoItem) {
        toDoItem.completed = !toDoItem.completed
        update(toDoItem)

        if (toDoItem.alert || toDoItem.completed) {
            scheduleOrCancelRedoBackgroundTask(toDoItem)
        }
    }

    private fun scheduleOrCancelRedoBackgroundTask(toDoItem: ToDoItem) {
        if (!toDoItem.completed) {
            val itemId = requireNotNull(toDoItem.id)
            val data = Data.Builder()
                .putLong("toDoItemId", itemId)
                .build()

            val nowInMillis = GregorianCalendar.getInstance(Locale.getDefault()).timeInMillis
            val scheduleMillis = toDoItem.deadline!! - nowInMillis

            val notificationWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(scheduleMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("$TAG_NAME:$scheduleMillis")
                .build()

            WorkManager.getInstance(getApplication()).enqueue(notificationWork)

            toDoItem.backgroundWorkUUID = notificationWork.id.toString()
            update(toDoItem)

            _showScheduledSnackBar.value = Event("${toDoItem.deadline} ${toDoItem.timeUnit}")
        } else {
            if (!toDoItem.backgroundWorkUUID.isNullOrEmpty()) {
                WorkManager.getInstance(getApplication())
                    .cancelWorkById(UUID.fromString(toDoItem.backgroundWorkUUID))
                toDoItem.backgroundWorkUUID = null
                update(toDoItem)
            }
        }
    }
}