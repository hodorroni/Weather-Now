package il.example.weatherapp.utils.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.AndroidEntryPoint
import il.example.weatherapp.MyApplication
import il.example.weatherapp.data.Repository.WeatherRepository
import il.example.weatherapp.ui.MainActivity
import il.example.weatherapp.utils.AppUtils
import il.example.weatherapp.utils.Success
import il.example.weatherapp.utils.WorkManager.WeatherWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AlarmManagerReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatInterval = 3L // 23 hours

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(
            repeatInterval,
            TimeUnit.HOURS
        )
            .addTag("PeriodicUniqueJob1") // Add a tag if needed
            .setBackoffCriteria(BackoffPolicy.LINEAR,10L,TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context!!).enqueueUniquePeriodicWork(
            "PeriodicUniqueJob1",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicWorkRequest
        )
    }
}