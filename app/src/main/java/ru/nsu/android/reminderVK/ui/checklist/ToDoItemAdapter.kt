package ru.nsu.android.reminderVK.ui.checklist

import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.databinding.ChecklistItemCheckedBinding
import ru.nsu.android.reminderVK.databinding.ChecklistItemUncheckedBinding
import java.util.*

const val ITEM_CHECKED = 0
const val ITEM_UNCHECKED = 1

class ToDoItemAdapter(private val onCheckboxClick: (ToDoItem, Int) -> Unit,
                      private val onCardClick: (ToDoItem) -> Unit,
                      private val workManager: WorkManager,
                      private val lifecycleOwner: LifecycleOwner): ListAdapter<ToDoItem,
        RecyclerView.ViewHolder>(ToDoItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_CHECKED -> ViewHolderChecked.from(parent)
            else -> ViewHolderUnchecked.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val toDoItem = getItem(position)
        when (holder) {
            is ViewHolderChecked -> {
                holder.bind(toDoItem, onCheckboxClick, onCardClick, workManager, lifecycleOwner)
            }
            is ViewHolderUnchecked -> {
                holder.bind(toDoItem, onCheckboxClick, onCardClick)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val toDoItem = getItem(position)
        return when (toDoItem.completed) {
            true -> ITEM_CHECKED
            else -> ITEM_UNCHECKED
        }
    }

    class ViewHolderChecked private constructor(private val binding: ChecklistItemCheckedBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItem, onCheckboxClick: (ToDoItem, Int) -> Unit,
                 onCardClick: (ToDoItem) -> Unit,
                 workManager: WorkManager,
                 lifecycleOwner: LifecycleOwner) {
            binding.todoItem = toDoItem
            binding.checkboxCompleted.setOnClickListener {
                onCheckboxClick(toDoItem, layoutPosition)
            }
            binding.cardTodo.setOnClickListener {
                onCardClick(toDoItem)
            }
            toDoItem.backgroundWorkUUID?.let {
                val workUUID = UUID.fromString(toDoItem.backgroundWorkUUID)
                workManager.getWorkInfoByIdLiveData(workUUID).observe(lifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                    workInfo?.let {
                        val iterator = workInfo.tags.iterator()
                        var endDateString = "0"
                        iterator.forEach {
                            if (it.startsWith(TAG_NAME)) {
                                val tokens = it.split(":")
                                endDateString = tokens[1]
                            }
                        }
                        val endDateMillis = endDateString.toLong()
                        val timeSpan = DateUtils.getRelativeTimeSpanString(endDateMillis, System.currentTimeMillis(), DAY_IN_MILLIS)
                        binding.tvRecurring.text = timeSpan
                    }
                })
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderChecked {
                val layoutInflater = LayoutInflater.from(parent.context)
                return ViewHolderChecked(ChecklistItemCheckedBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    class ViewHolderUnchecked private constructor(private val binding: ChecklistItemUncheckedBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(toDoItem: ToDoItem, onCheckboxClick: (ToDoItem, Int) -> Unit, onCardClick: (ToDoItem) -> Unit) {
            binding.todoItem = toDoItem
            binding.checkboxCompleted.setOnClickListener {
                onCheckboxClick(toDoItem, layoutPosition)
            }
            binding.cardTodo.setOnClickListener {
                onCardClick(toDoItem)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderUnchecked {
                val layoutInflater = LayoutInflater.from(parent.context)
                return ViewHolderUnchecked(ChecklistItemUncheckedBinding.inflate(layoutInflater, parent, false))
            }
        }
    }
}

class ToDoItemDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
    override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
        return oldItem == newItem
    }
}
