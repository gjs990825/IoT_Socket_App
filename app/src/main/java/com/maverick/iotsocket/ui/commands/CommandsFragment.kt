package com.maverick.iotsocket.ui.commands

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.maverick.iotsocket.Command
import com.maverick.iotsocket.CommandAdapter
import com.maverick.iotsocket.R
import com.maverick.iotsocket.databinding.FragmentCommandsBinding
import com.maverick.iotsocket.ui.MainActivityViewModel
import com.maverick.iotsocket.util.showToast


class CommandsFragment : Fragment() {
    private val TAG = "NotificationsFragment"
    private val commandList = ArrayList<Command>()
    private val mainActivityViewModel by lazy { ViewModelProvider(requireActivity())[MainActivityViewModel::class.java] }
    private val commandsViewModel: CommandsViewModel by viewModels()
    private var _binding: FragmentCommandsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommandsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainActivityViewModel = mainActivityViewModel

        initCommands()
        val commandListView = binding.commandListView
        commandListView.adapter =
            CommandAdapter(requireActivity(), R.layout.command_item, commandList)
        commandListView.setOnItemClickListener { _, _, position, _ ->
            val command = commandList[position]
            "\"${command.commandName}\" ${getString(R.string.prompt_command_sent)}".showToast(
                requireContext()
            )
            mainActivityViewModel.mqttSendCommand(command.commandContent)
        }

        binding.outlinedTextField.editText?.setOnEditorActionListener(InputActionListener())

        binding.floatingActionButton2.setOnClickListener {
            Log.i(TAG, "float button pressed")
            checkInputCommandAndSend()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initCommands() {
        commandList.add(Command("开启电源", "relay true"))
        commandList.add(Command("关闭电源", "relay false"))
        commandList.add(Command("开启蜂鸣器", "beeper true"))
        commandList.add(Command("关闭蜂鸣器", "beeper false"))
        commandList.add(Command("开启LED", "led 255"))
        commandList.add(Command("半开LED", "led 127"))
        commandList.add(Command("关闭LED", "led 0"))
        commandList.add(Command("电机正转", "motor 100"))
        commandList.add(Command("电机反转", "motor -100"))
        commandList.add(Command("电机停止", "motor 0"))
        commandList.add(Command("红外发送预设0", "infrared send 0"))
        commandList.add(Command("红外发送预设1", "infrared send 1"))
        commandList.add(Command("红外发送预设2", "infrared send 2"))
        commandList.add(Command("红外发送预设3", "infrared send 3"))
        commandList.add(Command("红外捕获到预设0", "infrared capture start 0"))
        commandList.add(Command("红外捕获到预设1", "infrared capture start 1"))
        commandList.add(Command("红外捕获到预设2", "infrared capture start 2"))
        commandList.add(Command("红外捕获到预设3", "infrared capture start 3"))
        commandList.add(Command("红外结束捕获", "infrared capture end"))
        commandList.add(Command("任务：映射亮度到电机输出", "task add \"handler motor brightness linear 0 100 -100 100\""))
        commandList.add(Command("清除所有任务", "task clear"))
        commandList.add(Command("闹钟：每秒翻转一次开关", "alarm add \"* * * * * *\" \"flip relay\" false"))
        commandList.add(Command("清除所有闹钟", "alarm clear"))
    }

    private fun checkInputCommandAndSend(needConfirm: Boolean = true) {
        binding.outlinedTextField.editText?.text?.let { text ->
            val command = text.toString()
            if (command.isBlank()) {
                R.string.prompt_blank_command.showToast(requireContext())
                text.clear()
            } else {
                if (needConfirm) {
                    val builder1: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
                    builder1?.let {
                        it.setTitle(R.string.prompt_confirm_sending)
                        it.setMessage(command)
                        it.setCancelable(true)
                        it.setIcon(android.R.drawable.ic_dialog_info)
                        it.setPositiveButton(getString(R.string.dialog_option_yes)) { dialog, _ ->
                            mainActivityViewModel.mqttSendCommand(command)
                            text.clear()
                            R.string.prompt_command_sent.showToast(requireContext())
                            dialog.cancel()
                        }
                        it.setNegativeButton(getString(R.string.dialog_option_no)) { dialog, _ ->
                            R.string.prompt_canceled.showToast(requireContext())
                            dialog.cancel()
                        }
                        it.create().show()
                    }
                } else {
                    mainActivityViewModel.mqttSendCommand(command)
                    text.clear()
                }
            }
        }
    }

    inner class InputActionListener : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent): Boolean {
            return if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                checkInputCommandAndSend()
                true
            } else {
                false
            }
        }
    }
}