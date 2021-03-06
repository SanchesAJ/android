package ru.nsu.android.reminderVK.util

import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

@BindingAdapter("inputEnabled")
fun View.setInputEnabled(inputEnabled: Boolean?) {
    isEnabled = inputEnabled ?: false
}

@BindingAdapter("saveEnabled")
fun View.setSaveEnabled(saveEnabled: Boolean?) {
    visibility = when (saveEnabled) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

@BindingAdapter("error")
fun setError(editText: EditText, isError: Boolean?) {
    if (isError == true) {
        editText.error = "Description required"
    }
}

@BindingAdapter("duration", requireAll = false)
fun setDuration(view: EditText, duration: Long?) {
    view.setText(duration.toString())
}

@InverseBindingAdapter(attribute = "duration")
fun getDuration(view: EditText): Long {
    return when (view.editableText.isNullOrBlank()) {
        true -> 1
        else -> view.editableText.toString().toLong()
    }
}

@BindingAdapter("durationAttrChanged")
fun setDurationChangedListener(view: EditText, listener: InverseBindingListener) {
    view.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        listener.onChange()
    }
}