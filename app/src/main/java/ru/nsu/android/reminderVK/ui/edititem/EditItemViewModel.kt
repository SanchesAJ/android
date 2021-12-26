package ru.nsu.android.reminderVK.ui.edititem

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.data.repository.ToDoItemRepository
import ru.nsu.android.reminderVK.util.Event
import kotlinx.coroutines.*
import java.util.*

class EditItemViewModel(application: Application, itemId: Long) : AndroidViewModel(application) {

    private val repository = ToDoItemRepository(application)

    private var _scheduled = MutableLiveData<Boolean>()
    val scheduled: LiveData<Boolean> = _scheduled
    val inputEnabled: LiveData<Boolean> = _scheduled

    val description = MutableLiveData<String?>()

    val title = MutableLiveData<String?>()

    val duration = MutableLiveData<Long?>()

    private var _timeUnit = MutableLiveData<String?>()
    val timeUnit: LiveData<String?> = _timeUnit

    private var _saveItemEvent = MutableLiveData<Event<String>>()
    val saveItemEvent: LiveData<Event<String>> = _saveItemEvent

    private var _saveButtonEnabled = MutableLiveData<Boolean>()
    val saveButtonEnabled: LiveData<Boolean> = _saveButtonEnabled

    private val _descriptionError = MutableLiveData<Boolean>()
    val descriptionError: LiveData<Boolean> = _descriptionError

    private val _titleError = MutableLiveData<Boolean>()
    val titleError: LiveData<Boolean> = _titleError

    private val _showDeleteMenuOption = MutableLiveData<Boolean>()
    val showDeleteMenuOption: LiveData<Boolean> = _showDeleteMenuOption

    private val _setToolbarTitleAddItem = MutableLiveData<Event<String>>()
    val setToolbarTitleAddItem: LiveData<Event<String>> = _setToolbarTitleAddItem

    private lateinit var toDoItem: ToDoItem

    init {
        _scheduled.value = false
        duration.value = 1L
        _saveButtonEnabled.value = false
        _showDeleteMenuOption.value = false
        initializeById(itemId)
    }

    private fun initializeById(id: Long) {
        if (id != -1L) {
            viewModelScope.launch(Dispatchers.IO) {
                toDoItem = repository.getItem(id)
                withContext(Dispatchers.Main) {
                    description.value = toDoItem.description
                    _scheduled.value = toDoItem.alert
                    title.value = toDoItem.title
                    duration.value = toDoItem.deadline
                    _timeUnit.value = toDoItem.timeUnit
                }
            }
            _showDeleteMenuOption.value = true
        } else {
            _setToolbarTitleAddItem.value = Event("new")
        }
    }

    fun onFabButtonClicked() = viewModelScope.launch {
        _saveItemEvent.value = Event("clicked")
    }

    fun saveItem(itemId: Long, description: String, title: String, recurring: Boolean, duration: Long, timeUnit: String) {
        when (itemId) {
            -1L -> insert(ToDoItem(description = description, title = title, alert = recurring, deadline = duration, timeUnit = timeUnit))
            else -> {
                toDoItem.description = description
                toDoItem.title = title
                toDoItem.alert = recurring
                toDoItem.deadline = duration
                toDoItem.timeUnit = timeUnit
                update(toDoItem)
            }
        }
    }

    fun deleteItem() {
        delete(toDoItem)
    }

    fun toggleSchedule() {
        _scheduled.value?.let {
            _scheduled.value = !it
        }
    }

    fun validateInput() {
        val descriptionIsBlank = description.value.isNullOrBlank()
        _descriptionError.value = descriptionIsBlank

        _saveButtonEnabled.value = !(descriptionIsBlank)
    }

    private fun insert(toDoItem: ToDoItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(toDoItem)
    }

    private fun update(toDoItem: ToDoItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(toDoItem)
    }

    private fun delete(toDoItem: ToDoItem) = viewModelScope.launch(Dispatchers.IO) {
        if (!toDoItem.backgroundWorkUUID.isNullOrEmpty()) {
            WorkManager.getInstance(getApplication()).cancelWorkById(UUID.fromString(toDoItem.backgroundWorkUUID))
        }
        repository.deleteItem(toDoItem)
    }
}