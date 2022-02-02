package com.maverick.iotsocket.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.maverick.iotsocket.databinding.FragmentTasksBinding
import com.maverick.iotsocket.ui.MainActivityViewModel

class TasksFragment: Fragment() {
    private val TAG = "TasksFragment"
    private val mainActivityViewModel by lazy { ViewModelProvider(requireActivity())[MainActivityViewModel::class.java] }
    private val tasksViewModel by lazy { ViewModelProvider(requireActivity())[TasksViewModel::class.java] }
    private var _binding: FragmentTasksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainActivityViewModel = mainActivityViewModel
        binding.tasksViewModel = tasksViewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}