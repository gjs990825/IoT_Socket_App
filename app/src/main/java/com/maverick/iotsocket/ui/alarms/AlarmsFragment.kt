package com.maverick.iotsocket.ui.alarms

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.maverick.iotsocket.databinding.FragmentAlarmsBinding
import com.maverick.iotsocket.model.AlarmsUIData
import com.maverick.iotsocket.ui.MainActivityViewModel


class AlarmsFragment : Fragment() {
    private val TAG = "ControlCenterFragment"

    private val alarmsViewDataKey = "alarms_data_reserved"

    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(
            alarmsViewDataKey,
            Context.MODE_PRIVATE
        )
    }

    private val mainActivityViewModel by lazy { ViewModelProvider(requireActivity())[MainActivityViewModel::class.java] }
    private val alarmsViewModel by lazy {
        val json = sharedPreferences.getString(alarmsViewDataKey, "")
        val alarmsUIData =
            Gson().fromJson(json, AlarmsUIData::class.java) ?: AlarmsUIData()
        ViewModelProvider(
            this,
            AlarmsViewModelFactory(alarmsUIData)
        )[AlarmsViewModel::class.java]
    }
    private var _binding: FragmentAlarmsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.alarmsViewModel = alarmsViewModel
        binding.mainActivityViewModel = mainActivityViewModel

        with(binding) {

            textStartDate.setOnClickListener {
                timePickerStart.visibility = View.GONE
                switchVisibility(datePickerStart)
            }

            textStartTime.setOnClickListener {
                datePickerStart.visibility = View.GONE
                switchVisibility(timePickerStart)
            }

            textEndDate.setOnClickListener {
                timePickerEnd.visibility = View.GONE
                switchVisibility(datePickerEnd)
            }

            textEndTime.setOnClickListener {
                datePickerEnd.visibility = View.GONE
                switchVisibility(timePickerEnd)
            }

            buttonResetIntervalAlarmInput.setOnClickListener {
                if (radioGroupAlarmType.checkedRadioButtonId == radioButtonPeriodic.id) {
                    radioGroupAlarmType.callOnClick()
                } else {
                    radioGroupAlarmType.check(radioButtonPeriodic.id)
                }
                this@AlarmsFragment.alarmsViewModel.resetAlarmInput()
            }
        }

        binding.radioGroupAlarmType.setOnCheckedChangeListener { _, checkedId ->
            Log.i(TAG, "radio onCheckedChange")
            alarmsViewModel.alarmTypeCheckedId.postValue(checkedId)

            binding.datePickerStart.visibility = View.GONE
            binding.timePickerStart.visibility = View.GONE
            binding.datePickerEnd.visibility = View.GONE
            binding.timePickerEnd.visibility = View.GONE

            val visibility = if (checkedId == binding.radioButtonOnce.id) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.textStartDate.visibility = visibility
            binding.textEndDate.visibility = visibility
        }
        return binding.root
    }

    override fun onStop() {
        super.onStop()

        val alarmsUIData: AlarmsUIData
        with(alarmsViewModel) {
            alarmsUIData = if (alarmStartDate.value != null &&
                alarmStartTime.value != null &&
                alarmEndDate.value != null &&
                alarmEndTime.value != null
            ) {
                AlarmsUIData(
                    alarmStartDate.value!!,
                    alarmStartTime.value!!,
                    alarmEndDate.value!!,
                    alarmEndTime.value!!,
                    alarmTypeCheckedId.value!!
                )
            } else {
                AlarmsUIData()
            }
        }

        val json = Gson().toJson(alarmsUIData)
        sharedPreferences.edit {
            putString(alarmsViewDataKey, json)
        }
        Log.v(TAG, "preference stored $json")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun switchVisibility(visibility: Int) =
        if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

    private fun switchVisibility(view: View) {
        view.visibility = switchVisibility(view.visibility)
    }
}