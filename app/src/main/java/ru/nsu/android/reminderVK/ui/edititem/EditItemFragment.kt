package ru.nsu.android.reminderVK.ui.edititem

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.nsu.android.reminderVK.R
import ru.nsu.android.reminderVK.databinding.FragmentEditItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.nsu.android.reminderVK.data.db.ToDoItem
import ru.nsu.android.reminderVK.ui.checklist.ChecklistViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log


@RequiresApi(Build.VERSION_CODES.O)
class EditItemFragment : Fragment() {

    private lateinit var binding: FragmentEditItemBinding
    private lateinit var editItemViewModel: EditItemViewModel
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>
    private lateinit var checkListViewModel: ChecklistViewModel

    private var isDeleteOptionEnabled = false
    private val args: EditItemFragmentArgs by navArgs()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_item, container, false
        )

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EditItemViewModelFactory(application, args.itemId)

        editItemViewModel = ViewModelProvider(this, viewModelFactory)
            .get(EditItemViewModel::class.java)

        binding.viewmodel = editItemViewModel

        var calendar = Calendar.getInstance(Locale.getDefault())

        editItemViewModel.timeUnit.observe(viewLifecycleOwner, Observer {
            it?.let {
                val millis = editItemViewModel.duration.value
                var date = millis?.let { it1 -> Date(it1) }

                if (!date?.after(Date(1L))!!) {
                    binding.datePicker.init(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.WEEK_OF_MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        null
                    )
                } else {
                    val local = convertDateFromString(date)
                    binding.datePicker.init(
                        local.year,
                        local.month.value - 1,
                        local.dayOfMonth,
                        null
                    )

                    binding.timePicker.hour = local.hour
                    binding.timePicker.minute = local.minute
                }
            }
        })

        editItemViewModel.saveItemEvent.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                saveAndReturn()
            }
        })

        editItemViewModel.description.observe(viewLifecycleOwner, Observer {
            editItemViewModel.validateInput()
        })

        editItemViewModel.showDeleteMenuOption.observe(viewLifecycleOwner, Observer {
            isDeleteOptionEnabled = it
            requireActivity().invalidateOptionsMenu()
        })

        editItemViewModel.setToolbarTitleAddItem.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                (requireActivity() as AppCompatActivity).supportActionBar?.title = "Add Item"
            }
        })

        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        spinnerAdapter = ArrayAdapter.createFromResource(
            requireNotNull(context),
            R.array.time_units_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_item_menu, menu)
        menu.findItem(R.id.delete).isVisible = isDeleteOptionEnabled
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.delete -> {
            deleteAndReturn()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun deleteAndReturn() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete this item?")
            .setPositiveButton("Delete") { _, _ ->
                editItemViewModel.deleteItem()

                this.findNavController()
                    .navigate(EditItemFragmentDirections.actionEditItemToChecklist(binding.descriptionInput.text.toString()))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAndReturn() {
        val description = binding.descriptionInput.text.toString()
        val title = binding.titleInput.text.toString()
        val alert = binding.switchAlert.isChecked
        val duration = getDateFromDateAndTimePicker()

        val timeUnit = ""

        editItemViewModel.saveItem(
            args.itemId,
            description,
            title,
            alert,
            duration,
            timeUnit
        )

        this.findNavController().navigate(EditItemFragmentDirections.actionEditItemToChecklist())
    }


    private fun getDateFromDateAndTimePicker(): Long {
        val day: Int = binding.datePicker.dayOfMonth
        val month: Int = binding.datePicker.month
        val year: Int = binding.datePicker.year

        val hour: Int = binding.timePicker.hour
        val minute: Int = binding.timePicker.minute

        val calendar = GregorianCalendar(year, month, day, hour, minute)

        return calendar.timeInMillis
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDateFromString(date: Date): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        val localDate: LocalDateTime = LocalDateTime.parse(date.toString(), formatter)
        return localDate
    }
}
