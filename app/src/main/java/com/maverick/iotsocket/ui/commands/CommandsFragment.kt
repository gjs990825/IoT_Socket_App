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
import androidx.recyclerview.widget.LinearLayoutManager
import com.maverick.iotsocket.Command
import com.maverick.iotsocket.CommandAdapter
import com.maverick.iotsocket.CommandOnClickListener
import com.maverick.iotsocket.R
import com.maverick.iotsocket.databinding.FragmentCommandsBinding
import com.maverick.iotsocket.ui.MainActivityViewModel
import com.maverick.iotsocket.util.showToast

class CommandsFragment : Fragment(), CommandOnClickListener {
    private val TAG = "CommandsFragment"
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

        binding.mainActivityViewModel = mainActivityViewModel
        binding.commandsViewModel = commandsViewModel

        with(binding) {
            lifecycleOwner = viewLifecycleOwner

            commandListView.layoutManager = LinearLayoutManager(requireContext())
            commandListView.adapter = CommandAdapter(commandList, this@CommandsFragment)

            textUserInputCommand.editText?.setOnEditorActionListener(InputActionListener())

            buttonSendUserInputCommand.setOnClickListener { checkInputCommandAndSend() }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    init {
        with(commandList) {
            add(Command("开启蜂鸣器", "beeper true"))
            add(Command("关闭蜂鸣器", "beeper false"))
            add(Command("开启LED", "led 255"))
            add(Command("半开LED", "led 127"))
            add(Command("关闭LED", "led 0"))
            add(Command("电机正转", "motor 100"))
            add(Command("电机反转", "motor -100"))
            add(Command("电机停止", "motor 0"))
            add(Command("红外发送预设0", "infrared send 0"))
            add(Command("红外发送预设1", "infrared send 1"))
            add(Command("红外发送预设2", "infrared send 2"))
            add(Command("红外发送预设3", "infrared send 3"))
            add(Command("红外捕获到预设0", "infrared capture start 0"))
            add(Command("红外捕获到预设1", "infrared capture start 1"))
            add(Command("红外捕获到预设2", "infrared capture start 2"))
            add(Command("红外捕获到预设3", "infrared capture start 3"))
            add(Command("红外结束捕获", "infrared capture end"))
            add(Command("任务：映射亮度到电机输出", "task add motor brightness linear 0 100 -100 100"))
            add(Command("闹钟：每秒翻转一次开关", "alarm add \"* * * * * *\" \"flip relay\" false"))
        }
    }

    private fun checkInputCommandAndSend(needConfirm: Boolean = true) {
        if (commandsViewModel.isCommandValid.value != true) {
            return
        }

        val command = commandsViewModel.getUserInputCommand()

        if (!needConfirm) {
            mainActivityViewModel.sendCommand(command)
            return
        }
        context?.let { AlertDialog.Builder(it) }?.let {
            it.setTitle(R.string.prompt_confirm_sending)
            it.setMessage(command)
            it.setCancelable(true)
            it.setIcon(android.R.drawable.ic_dialog_info)
            it.setPositiveButton(getString(R.string.dialog_option_yes)) { dialog, _ ->
                mainActivityViewModel.sendCommand(command)
                R.string.prompt_command_sent.showToast(requireContext())
                dialog.dismiss()
            }
            it.setNegativeButton(getString(R.string.dialog_option_no)) { dialog, _ ->
                R.string.prompt_canceled.showToast(requireContext())
                dialog.cancel()
            }
            it.create().show()
        }
    }

    inner class InputActionListener : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent): Boolean {
            return if (event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                checkInputCommandAndSend()
                true
            } else {
                false
            }
        }
    }

    override fun onClick(command: Command) {
        mainActivityViewModel.sendCommand(command.commandContent)
        "\"${command.commandName}\" ${getString(R.string.prompt_command_sent)}"
            .showToast(requireContext())
    }
}