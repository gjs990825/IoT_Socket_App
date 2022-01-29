package com.maverick.iotsocket.ui.controlcenter

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
import com.maverick.iotsocket.databinding.FragmentControlCenterBinding
import com.maverick.iotsocket.model.ControlCenterUIData
import com.maverick.iotsocket.model.Date
import com.maverick.iotsocket.model.Time
import com.maverick.iotsocket.ui.MainActivityViewModel


class ControlCenterFragment : Fragment() {
    private val TAG = "ControlCenterFragment"


    private val controlCenterViewDataKey = "control_center_data_reserved"

    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(
            controlCenterViewDataKey,
            Context.MODE_PRIVATE
        )
    }

    private val mainActivityViewModel by lazy { ViewModelProvider(requireActivity())[MainActivityViewModel::class.java] }
    private val controlCenterViewModel by lazy {
        val json = sharedPreferences.getString(controlCenterViewDataKey, "")
        val controlCenterUIData = Gson().fromJson(json, ControlCenterUIData::class.java) ?: ControlCenterUIData()
        ViewModelProvider(
            this,
            ControlCenterViewModelFactory(controlCenterUIData)
        )[ControlCenterViewModel::class.java]
    }
    private var _binding: FragmentControlCenterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlCenterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        with(binding) {

            textStartDate.setOnClickListener {
                timePickerStart.visibility = View.GONE
                switchViewVisibility(datePickerStart)
            }

            textStartTime.setOnClickListener {
                datePickerStart.visibility = View.GONE
                switchViewVisibility(timePickerStart)
            }

            textEndDate.setOnClickListener {
                timePickerEnd.visibility = View.GONE
                switchViewVisibility(datePickerEnd)
            }

            textEndTime.setOnClickListener {
                datePickerEnd.visibility = View.GONE
                switchViewVisibility(timePickerEnd)
            }

            radioGroupAlarmType.setOnCheckedChangeListener { _, checkedId ->
                controlCenterViewModel.updateAlarmTypeCheckedId(checkedId)

                datePickerStart.visibility = View.GONE
                timePickerStart.visibility = View.GONE
                datePickerEnd.visibility = View.GONE
                timePickerEnd.visibility = View.GONE

                val visibility = if (checkedId == radioButtonOnce.id) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                textStartDate.visibility = visibility
                textEndDate.visibility = visibility
            }

            timePickerStart.setOnTimeChangedListener { _, hourOfDay, minute ->
                controlCenterViewModel.updateAlarmStartTime(Time(hourOfDay, minute))
            }
            datePickerStart.setOnDateChangedListener { _, _, monthOfYear, dayOfMonth ->
                controlCenterViewModel.updateAlarmStartDate(Date(monthOfYear, dayOfMonth))
            }
            timePickerEnd.setOnTimeChangedListener { _, hourOfDay, minute ->
                controlCenterViewModel.updateAlarmEndTime(Time(hourOfDay, minute))
            }
            datePickerEnd.setOnDateChangedListener { _, _, monthOfYear, dayOfMonth ->
                controlCenterViewModel.updateAlarmEndDate(Date(monthOfYear, dayOfMonth))
            }

            buttonSetAlarm.setOnClickListener {
                val cmd = controlCenterViewModel.getCommandString()
                Log.i(TAG, "alarm cmd:$cmd")
                mainActivityViewModel.mqttSendCommand(cmd)
//                buttonResetAlarmInput.callOnClick()
            }
            buttonResetAlarmInput.setOnClickListener {
                radioGroupAlarmType.check(radioButtonPeriodic.id)
                controlCenterViewModel.resetAlarmInput()
            }
        }

        with(controlCenterViewModel) {
            alarmStartDate.observe(requireActivity()) {
                binding.textStartDate.text = it.getFormatted()
            }
            alarmStartTime.observe(requireActivity()) {
                binding.textStartTime.text = it.getFormatted()
            }
            alarmEndDate.observe(requireActivity()) {
                binding.textEndDate.text = it.getFormatted()
            }
            alarmEndTime.observe(requireActivity()) {
                binding.textEndTime.text = it.getFormatted()
            }
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()

        val controlCenterUIData: ControlCenterUIData
        with(controlCenterViewModel) {
            controlCenterUIData = if (alarmStartDate.value != null &&
                alarmStartTime.value != null &&
                alarmEndDate.value != null &&
                alarmEndTime.value != null) {
                ControlCenterUIData(
                    alarmStartDate.value!!,
                    alarmStartTime.value!!,
                    alarmEndDate.value!!,
                    alarmEndTime.value!!
                )
            } else {
                ControlCenterUIData()
            }
        }

        val json = Gson().toJson(controlCenterUIData)
        sharedPreferences.edit {
            putString(controlCenterViewDataKey, json)
        }
        Log.v(TAG, "preference stored $json")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun switchVisibility(visibility: Int) =
        if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

    private fun switchViewVisibility(view: View) {
        view.visibility = switchVisibility(view.visibility)
    }
}