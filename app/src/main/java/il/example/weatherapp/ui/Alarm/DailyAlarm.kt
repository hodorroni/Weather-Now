package il.example.weatherapp.ui.Alarm

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import il.example.weatherapp.R
import il.example.weatherapp.databinding.AlarmLayoutBinding
import il.example.weatherapp.utils.WorkManager.WeatherWorker
import il.example.weatherapp.utils.autoCleared
import il.example.weatherapp.utils.receivers.AlarmManagerReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyAlarm : Fragment() {

    private var binding : AlarmLayoutBinding by autoCleared()
    private lateinit var sharedPreferences: SharedPreferences


    @RequiresApi(Build.VERSION_CODES.S)
    val postNotificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
        } else {
            // If user denies twice and checks "Don't ask again"
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationsDialog()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.notifications_permission_denied), Toast.LENGTH_SHORT).show()
                //showNotificationsDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    val backgroundLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){

        }
        else {
            // If user denies twice and checks "Don't ask again"
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                showPermissionDeniedDialog()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.allow_permision_denied), Toast.LENGTH_SHORT).show()
                //showPermissionDeniedDialog()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AlarmLayoutBinding.inflate(inflater,container,false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("time", Context.MODE_PRIVATE)

        //storing the time the user chosen by sharedPreferences
        val result = sharedPreferences.getString("time_picked", "")

        // Check if the time string is not null if so then the user didnt pick any time so i will put Nan
        val formattedTime = if (!result.isNullOrEmpty() && result.contains(":")) {
            val timeParts = result.split(":")
            // Check if timeParts has at least two elements before accessing them
            if (timeParts.size >= 2) {
                String.format("%02d:%02d", timeParts[0].toInt(), timeParts[1].toInt())
            } else {
                ""
            }
        } else {
            // when the shared preference doesnt contain ":" meaning the first time the user just went to this page and never scheduled before
            ""
        }

        if(formattedTime.equals("")){
            binding.alarmEachDay.text = getString(R.string.choose_time)
        }
        else {
            binding.disableBtn.visibility = View.VISIBLE
            binding.alarmEachDay.text = getString(R.string.chosen_time, formattedTime)
        }

        Glide.with(requireContext()).load("https://www.techyourchance.com/wp-content/uploads/2023/06/work-manager.jpg").into(binding.androidWorkImage)


        binding.disableBtn.setOnClickListener {
            val workManager = WorkManager.getInstance(requireContext())
            workManager.cancelUniqueWork("PeriodicUniqueJob1")
            binding.disableBtn.visibility = View.GONE
            binding.alarmEachDay.text = getString(R.string.choose_time)
            sharedPreferences.edit().putString("time_picked","" ).apply()
        }

        //schedule next job
        binding.alarmEachDay.setOnClickListener {
            if((ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED  ){
                //showTimePickerDialog()
                setExactPermission()
            }
             if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                 //backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                 showPermissionDeniedDialog()
            }
             else if(ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                 //showNotificationsDialog()
            }

        }

    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun setExactPermission() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            // Device does not support scheduling exact alarms, Show user rational dialog with the ability to go to the settings
            permissionScheduleExact()
        } else {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val intent = Intent(requireActivity(), AlarmManagerReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(requireActivity(), 1, intent, PendingIntent.FLAG_IMMUTABLE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    binding.disableBtn.visibility = View.VISIBLE
                    sharedPreferences.edit().putString("time_picked","$selectedHour"+":"+"$selectedMinute" ).apply()


                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)

                        // Get the specified time
                        val specifiedTime = timeInMillis

                        // Compare with the current time
                        val currentTime = Calendar.getInstance().timeInMillis

                        // If the specified time has already passed for the current day
                        if (specifiedTime <= currentTime) {
                            // Increment the day by one
                            add(Calendar.DAY_OF_MONTH, 1)
                        }

                        // Format the hour and minute into a readable string
                        val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                        // Update UI with chosen time
                        binding.alarmEachDay.text = getString(R.string.chosen_time, formattedTime)
                    }

                    // Show success message
                    Toast.makeText(requireContext(),
                        getString(R.string.alarm_scheduled_successfully), Toast.LENGTH_SHORT).show()

                    // Schedule alarm based on device's API level
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                },
                hour,
                minute,
                false
            )

            // Show time picker dialog
            timePickerDialog.show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun permissionScheduleExact(){
        AlertDialog.Builder(requireContext())
            .setTitle("Please Allow our app in Alarms & Remainders ")
            .setMessage("Inorder to be able to choose time you will be needed to allow in alarms & remainders our app.\n That feature allows our application to show you the weather forecast for tommorow.")
            .setPositiveButton( getString(R.string.go_to_settings)) { dialog, _ ->
                //Can send to the settings
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                requireActivity().startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                // Handle cancellation
                dialog.dismiss()
            }
            .show()
    }




    @RequiresApi(Build.VERSION_CODES.S)
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.location_allow_all_the_time_is_needed))
            .setMessage(getString(R.string.require_permisson_future_weather))
            .setPositiveButton( getString(R.string.go_to_settings)) { dialog, _ ->
                //Can send to the settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                // Handle cancellation
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showNotificationsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.notifications_permission_required))
            .setMessage(getString(R.string.feature_requires_permission))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                startActivity(intent)
                dialog.dismiss()

            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                // Handle cancellation
                dialog.dismiss()
            }
            .show()
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu,menu)
        val favouriteItem = menu.findItem(R.id.favourite_icon)
        val homeItem = menu.findItem(R.id.home_btn_icon)
        val historyItem = menu.findItem(R.id.history_icon)

        val colorControlNormal = ContextCompat.getColor(requireContext(), R.color.white)

        // Set tint for the icons
        favouriteItem.icon?.let { DrawableCompat.setTint(it, colorControlNormal) }
        homeItem.icon?.let { DrawableCompat.setTint(it, colorControlNormal) }
        historyItem.icon?.let { DrawableCompat.setTint(it, colorControlNormal) }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.favourite_icon){
            findNavController().navigate(R.id.action_dailyAlarm_to_favouritesFragment)
        }
        else if (item.itemId == R.id.home_btn_icon){
            findNavController().navigate(R.id.action_dailyAlarm_to_homeScreenFragment)
        }
        else if(item.itemId== R.id.history_icon){
            findNavController().navigate(R.id.action_dailyAlarm_to_historyFragment)
        }
        return super.onOptionsItemSelected(item)
    }
}


