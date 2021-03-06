package ru.nsu.android.reminderVK.data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.data.db.ToDoItemDatabase

class ToDoItemRepository(context: Context) {

    private val toDoItemDao = ToDoItemDatabase.getInstance(context).toDoItemDao
    val allToDoItems: LiveData<List<ToDoItem>>

    init {
        allToDoItems = toDoItemDao.getAll()
    }

    @WorkerThread
    fun getItem(id: Long): ToDoItem {
        return toDoItemDao.getItem(id)
    }

    @WorkerThread
    fun getUncheckedItemCount(): Int {
        return toDoItemDao.getUncheckedCount()
    }

    @WorkerThread
    suspend fun insert(toDoItem: ToDoItem) {
        toDoItemDao.insert(toDoItem)
    }

    @WorkerThread
    suspend fun update(toDoItem: ToDoItem) {
        toDoItemDao.update(toDoItem)
    }

    @WorkerThread
    suspend fun deleteItem(toDoItem: ToDoItem) {
        toDoItemDao.deleteItem(toDoItem)
    }


}