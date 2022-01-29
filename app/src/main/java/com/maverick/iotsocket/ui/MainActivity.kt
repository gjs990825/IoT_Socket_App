package com.maverick.iotsocket.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.maverick.iotsocket.R
import com.maverick.iotsocket.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.mainActivityViewModel = mainActivityViewModel
        binding.lifecycleOwner = this

        mainActivityViewModel.message.observe(this) { errorMessage ->
            binding.cardViewErrorMessageText.text = errorMessage
            binding.cardViewErrorMessage.visibility = View.VISIBLE

            Looper.myLooper()?.let {
                Handler(it).postDelayed({
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    binding.cardViewErrorMessage.startAnimation(animation)
                    Handler(it).postDelayed({
                        binding.cardViewErrorMessage.visibility = View.GONE
                    }, 1000)
                }, 2000)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainActivityViewModel.mqttConnect()
    }

    override fun onStop() {
        super.onStop()
        mainActivityViewModel.mqttDisconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityViewModel.message.removeObservers(this)
        mainActivityViewModel.clearErrorMessage()
    }
}