package com.maverick.iotsocket.ui.commands

import android.os.Bundle
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
import com.maverick.iotsocket.model.Command
import com.maverick.iotsocket.model.CommandAdapter
import com.maverick.iotsocket.model.CommandAdapter.CommandOnClickListener
import com.maverick.iotsocket.R
import com.maverick.iotsocket.databinding.FragmentCommandsBinding
import com.maverick.iotsocket.ui.MainActivityViewModel
import com.maverick.iotsocket.util.showKeyboard
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
            add(Command("???????????????", "beeper true"))
            add(Command("???????????????", "beeper false"))
            add(Command("??????LED", "led 255"))
            add(Command("??????LED", "led 127"))
            add(Command("??????LED", "led 0"))
            add(Command("PWM??????100", "pwm 100"))
            add(Command("PWM??????-100", "pwm -100"))
            add(Command("PWM??????0", "pwm 0"))
            add(Command("??????????????????0", "infrared send 0"))
            add(Command("??????????????????1", "infrared send 1"))
            add(Command("??????????????????2", "infrared send 2"))
            add(Command("??????????????????3", "infrared send 3"))
            add(Command("?????????????????????0", "infrared capture 0"))
            add(Command("?????????????????????1", "infrared capture 1"))
            add(Command("?????????????????????2", "infrared capture 2"))
            add(Command("?????????????????????3", "infrared capture 3"))
            add(Command("????????????????????????PWM??????", "task add pwm brightness linear 0 100 -100 100"))
            add(Command("?????????????????????????????????", "alarm add \"* * * * * *\" \"flip relay\" false"))
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
        mainActivityViewModel.sendCommand(command.content)
        "\"${command.name}\" ${getString(R.string.prompt_command_sent)}"
            .showToast(requireContext())
    }

    override fun onLongClick(command: Command) {
        commandsViewModel.updateUserInputCommand(command.content)
        binding.textUserInputCommand.editText?.showKeyboard()
    }
}