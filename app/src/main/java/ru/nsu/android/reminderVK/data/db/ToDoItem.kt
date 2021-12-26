package ru.nsu.android.reminderVK.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vk.api.sdk.auth.VKAccessToken
import java.util.*

@Entity(tableName = "item")
data class ToDoItem(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        @ColumnInfo(name = "sort") var sort: Long? = null,
        @ColumnInfo(name = "description") var description: String,
        @ColumnInfo(name = "title") var title: String,
        @ColumnInfo(name = "completed") var completed: Boolean = false,
        @ColumnInfo(name = "recurring") var alert: Boolean = false,
        @ColumnInfo(name = "duration") var deadline: Long? = null,
        @ColumnInfo(name = "time_unit") var timeUnit: String? = null,
        @ColumnInfo(name = "background_work_uuid") var backgroundWorkUUID: String? = null
) {
    fun getDurationString() :String{
            val date = deadline?.let { Date(it) }
            return "Will be reminded in  " + date.toString()
    }
}