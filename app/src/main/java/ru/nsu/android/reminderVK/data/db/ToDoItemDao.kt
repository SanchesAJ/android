package ru.nsu.android.reminderVK.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoItemDao {
    @Query("SELECT * FROM item")
    fun getAll(): LiveData<List<ToDoItem>>

    @Query("SELECT * FROM item WHERE id = :id")
    fun getItem(id: Long): ToDoItem

    @Query("SELECT COUNT(*) FROM item WHERE completed = 0")
    fun getUncheckedCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ToDoItem): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: ToDoItem)

    @Delete
    suspend fun deleteItem(toDoItem: ToDoItem)

    @Query("DELETE FROM item")
    fun deleteAll()

}