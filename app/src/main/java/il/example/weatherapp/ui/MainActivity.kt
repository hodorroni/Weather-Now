package il.example.weatherapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import il.example.weatherapp.R
import il.example.weatherapp.databinding.ActivityMainBinding
import il.example.weatherapp.databinding.WeatherScreenFragmentBinding
import il.example.weatherapp.ui.HomeScreen.HomeScreenFragment
import il.example.weatherapp.ui.WeatherScreen.WeatherScreenFragment
import il.example.weatherapp.utils.AppUtils

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppUtils.createNotificationChannel(this)
        AppUtils.cancelNotification(this)

//
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.homeScreenFragment -> {
//                    // Hide the home_btn_icon when HomeScreenFragment is displayed
//                    binding.homeBtnIcon.visibility = View.GONE
//                    // Show or hide other icons as needed
//                }
//                else -> {
//                    // Show the home_btn_icon for other fragments
//                    binding.homeBtnIcon.visibility = View.VISIBLE
//                    // Show or hide other icons as needed
//                }
//            }
//        }
//
//        val WeatherScreenFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull { it is WeatherScreenFragment } as WeatherScreenFragment?
//        binding.homeBtnIcon.setOnClickListener {
//            WeatherScreenFragment?.moveToHome()
//        }
    }



}