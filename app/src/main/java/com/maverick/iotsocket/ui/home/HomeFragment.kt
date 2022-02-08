package com.maverick.iotsocket.ui.home

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
import com.google.gson.JsonSyntaxException
import com.maverick.iotsocket.R
import com.maverick.iotsocket.databinding.FragmentHomeBinding
import com.maverick.iotsocket.model.IoTSocket
import com.maverick.iotsocket.ui.MainActivityViewModel
import kotlin.math.abs

class HomeFragment : Fragment() {
    private var TAG = "HomeFragment"
    private val homeViewDataKey = "home_data_reserved"

    private val mainActivityViewModel by lazy { ViewModelProvider(requireActivity())[MainActivityViewModel::class.java] }
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(
            homeViewDataKey,
            Context.MODE_PRIVATE
        )
    }
    private val homeViewModel by lazy {
        val json = sharedPreferences.getString(homeViewDataKey, "")
        val ioTSocket: IoTSocket = try {
            Gson().fromJson(json, IoTSocket::class.java) ?: IoTSocket()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            IoTSocket()
        }
        Log.v(TAG, "preference restored $json")
        ViewModelProvider(this, HomeViewModelFactory(ioTSocket))[HomeViewModel::class.java]
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeViewModel = homeViewModel
        binding.mainActivityViewModel = mainActivityViewModel

        homeViewModel.peripheral.observe(requireActivity()) {
            with(binding) {
                textPeripheralRelay.text = getDisplayString(it.relay)
                textPeripheralLed.text = getDisplayString(it.led)
                textPeripheralBeeper.text = getDisplayString(it.beeper)
                textPeripheralMotor.text = getDisplayString(it.motor)
            }
        }

        homeViewModel.sensor.observe(requireActivity()) {
            with(binding) {
                textSensorTemperature.text = getDisplayString(it.temperature)
                textSensorPressure.text = getDisplayString(it.pressure)
                textSensorBrightness.text = getDisplayString(it.brightness)
            }
        }

        homeViewModel.systemInfo.observe(requireActivity()) {
            with(binding) {
                val diff = it.time - System.currentTimeMillis() / 1000

                textSystemTime.text = if (diff >= 0) {
                    "${getString(R.string.placeholder_time_diff_ahead_1)}${abs(diff)}${
                        getString(R.string.placeholder_time_diff_ahead_2)
                    }"
                } else {
                    "${getString(R.string.placeholder_time_diff_behind_1)}${abs(diff)}${
                        getString(R.string.placeholder_time_diff_behind_2)
                    }"
                }
                textSystemTemperature.text = getDisplayString(it.temperature)
            }
        }

        mainActivityViewModel.isUsingBluetoothConnection.observe(requireActivity()) {
            homeViewModel.updateConnection()
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        Log.w(TAG, "onStart: ")
        homeViewModel.subscribeTopicState()
        homeViewModel.updateLastSyncTime()
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.unsubscribeTopicState()
        var ioTSocket: IoTSocket? = null
        with(homeViewModel) {
            if (sensor.value != null && peripheral.value != null && systemInfo.value != null) {
                ioTSocket = IoTSocket(sensor.value!!, peripheral.value!!, systemInfo.value!!)
            }
        }

        val json = Gson().toJson(ioTSocket)
        sharedPreferences.edit {
            putString(homeViewDataKey, json)
        }
        Log.v(TAG, "preference stored $json")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDisplayString(value: Any): String {
        return when (value) {
            is Float -> String.format("%.2f", value)
            is Int, is Long -> (value as Number).toLong().toString()
            is Boolean -> getString(
                if (value) {
                    R.string.text_on
                } else {
                    R.string.text_off
                }
            )
            else -> {
                Log.w(TAG, "unexpected type")
                getString(R.string.text_not_a_number)
            }
        }
    }
}