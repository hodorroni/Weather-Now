package il.example.weatherapp.utils.WorkManager

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import il.example.weatherapp.R
import il.example.weatherapp.data.Repository.WeatherRepository
import il.example.weatherapp.data.models.new_api.forecastday
import il.example.weatherapp.utils.AppUtils
import il.example.weatherapp.utils.Success
import il.example.weatherapp.utils.receivers.AlarmManagerReceiver
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar

//Used this guide https://medium.com/@jeremy.leyvraz/the-art-of-integrating-hilt-dependency-injection-with-workers-for-harmonious-android-development-28bdc21be047


@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: WeatherRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        return try {
            // Check for permission explicitly before accessing location
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                var nextDayWeather :Any = Any()

                //Permission is granted, proceed to fetch location
                val locationResult = fusedLocationClient.lastLocation
                val taskResult = Tasks.await(locationResult)
                val lat = taskResult.latitude
                val long = taskResult.longitude
                val result = repository.fetchWeather("$lat,$long")
                if (result.status is Success) {
                    val currentDate = Calendar.getInstance()

                    // Add 1 day to the current date
                    currentDate.add(Calendar.DAY_OF_YEAR, 1)
                    val nextDate = currentDate.time

                    // Format and print the next date
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    var nextDateStr = dateFormat.format(nextDate)
                    val forecastDayResult = result.status.data!!.forecast.forecastday
                    for (dayWeek in forecastDayResult){
                        if(dayWeek.date.contains(nextDateStr)){
                            nextDayWeather = dayWeek
                            break
                        }
                    }

                    if(nextDayWeather is forecastday){
                        val nextDayWeatherString = when (nextDayWeather.day.condition.text) {
                            "Sunny" -> applicationContext.getString(R.string.sunny)
                            "Clear" -> applicationContext.getString(R.string.clear)
                            "Partly cloudy" -> applicationContext.getString(R.string.partly_cloudy)
                            "Cloudy" -> applicationContext.getString(R.string.cloudy)
                            "Overcast" -> applicationContext.getString(R.string.overcast)
                            "Mist" -> applicationContext.getString(R.string.mist)
                            "Patchy rain possible" -> applicationContext.getString(R.string.patchy_rain_possible)
                            "Patchy snow possible" -> applicationContext.getString(R.string.patchy_snow_possible)
                            "Patchy sleet" -> applicationContext.getString(R.string.patchy_sleet)
                            "Patchy freezing drizzle possible" -> applicationContext.getString(R.string.patchy_freezing_drizzle)
                            "Thundery outbreaks possible" -> applicationContext.getString(R.string.thundery_outbreaks_possible)
                            "Blowing snow" -> applicationContext.getString(R.string.blowing_snow)
                            "Blizzard" -> applicationContext.getString(R.string.blizzard)
                            "Fog" -> applicationContext.getString(R.string.fog)
                            "Freezing fog" -> applicationContext.getString(R.string.freezing_fog)
                            "Patchy light drizzle" -> applicationContext.getString(R.string.patchy_light_drizzle)
                            "Light drizzle" -> applicationContext.getString(R.string.light_drizzle)
                            "Freezing drizzle" -> applicationContext.getString(R.string.freezing_drizzle)
                            "Heavy freezing drizzle" -> applicationContext.getString(R.string.heavy_freezing_drizzle)
                            "Patchy light rain" -> applicationContext.getString(R.string.patchy_light_rain)
                            "Light rain" -> applicationContext.getString(R.string.light_rain)
                            "Moderate rain at times" -> applicationContext.getString(R.string.moderate_rain_at_times)
                            "Moderate rain" -> applicationContext.getString(R.string.moderate_rain)
                            "Heavy rain at times" -> applicationContext.getString(R.string.heavy_rain_at_times)
                            "Heavy rain" -> applicationContext.getString(R.string.heavy_rain)
                            "Light freezing rain" -> applicationContext.getString(R.string.light_freezing_rain)
                            "Moderate or heavy freezing rain" -> applicationContext.getString(R.string.moderate_or_heavy_freezing_rain)
                            "Light sleet" -> applicationContext.getString(R.string.light_sleet)
                            "Moderate or heavy sleet" -> applicationContext.getString(R.string.moderate_or_heavy_sleet)
                            "Patchy light snow" -> applicationContext.getString(R.string.patchy_light_snow)
                            "Light snow" -> applicationContext.getString(R.string.light_snow)
                            "Patchy moderate snow" -> applicationContext.getString(R.string.patchy_moderate_snow)
                            "Moderate snow" -> applicationContext.getString(R.string.moderate_snow)
                            "Patchy heavy snow" -> applicationContext.getString(R.string.patchy_heavy_snow)
                            "Heavy snow" -> applicationContext.getString(R.string.heavy_snow)
                            "Ice pellets" -> applicationContext.getString(R.string.ice_pellets)
                            "Light rain shower" -> applicationContext.getString(R.string.light_rain_shower)
                            "Moderate or heavy rain shower" -> applicationContext.getString(R.string.moderate_or_heavy_rain_shower)
                            "Torrential rain shower" -> applicationContext.getString(R.string.torrential_rain_shower)
                            "Light sleet showers" -> applicationContext.getString(R.string.light_sleet_showers)
                            "Moderate or heavy sleet showers" -> applicationContext.getString(R.string.moderate_or_heavy_sleet_showers)
                            "Light snow showers" -> applicationContext.getString(R.string.light_snow_showers)
                            "Moderate or heavy snow showers" -> applicationContext.getString(R.string.moderate_or_heavy_snow_showers)
                            "Light showers of ice pellets" -> applicationContext.getString(R.string.light_showers_of_ice_pellets)
                            "Moderate or heavy showers of ice pelltes" -> applicationContext.getString(R.string.moderate_or_heavy_showers_of_ice_pelltes)
                            "Patchy light rain with thunder" -> applicationContext.getString(R.string.patchy_light_rain_with_thunder)
                            "Moderate or heavy rain with thunder" -> applicationContext.getString(R.string.moderate_or_heavy_rain_with_thunder)
                            "Patchy light snow with thunder" -> applicationContext.getString(R.string.patchy_light_snow_with_thunder)
                            "Moderate or heavy snow with thunder" -> applicationContext.getString(R.string.moderate_or_heavy_snow_with_thunder)
                            "Patchy rain nearby"-> applicationContext.getString(R.string.patchy_rain_nearby)
                            "Patchy snow nearby"-> applicationContext.getString(R.string.patchy_snow_nearby)
                            "Patchy sleet nearby"-> applicationContext.getString(R.string.patchy_sleet_nearby)
                            "Patchy freezing drizzle nearby"-> applicationContext.getString(R.string.patchy_freezing_drizzle_nearby)
                            "Thundery outbreaks in nearby"-> applicationContext.getString(R.string.thundery_outbreaks_in_nearby)
                            "Moderate or heavy rain in area with thunder"-> applicationContext.getString(R.string.moderate_or_heavy_rain_in_area_with_thunder)
                            "Patchy light snow in area with thunder"-> applicationContext.getString(R.string.patchy_light_snow_in_area_with_thunder)
                            "Moderate or heavy snow in area with thunder"-> applicationContext.getString(R.string.moderate_or_heavy_rain_in_area_with_thunder)
                            else -> nextDayWeather.day.condition.text
                        }
                        AppUtils.notify(applicationContext,applicationContext.getString(R.string.tomorrow_s_weather),
                            applicationContext.getString(R.string.weather_status)+ nextDayWeatherString +"\n"+
                                    applicationContext.getString(R.string.average_temperature)  +"${nextDayWeather.day.avgtemp_c.toInt()}",
                            R.mipmap.ic_launcher_weather_round)
                    }

                    Result.success()
                } else {
                    Result.retry() // for cases of lost internet connection etc
                }
            } else {
                // Permission is not granted
                AppUtils.notify(applicationContext,applicationContext.getString(R.string.error),applicationContext.getString(R.string.allow_all_the_time))
                Result.failure()
            }
        }
        catch (e: Exception) {
            Result.failure()
        }
    }
}


