package il.example.weatherapp.ui.WeatherScreen

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import il.example.weatherapp.R
import il.example.weatherapp.data.models.LocalModels.Favourites
import il.example.weatherapp.databinding.WeatherScreenFragmentBinding
import il.example.weatherapp.ui.ViewModel.WeatherViewModel
import il.example.weatherapp.utils.Error
import il.example.weatherapp.utils.Loading
import il.example.weatherapp.utils.Success
import il.example.weatherapp.utils.autoCleared

@AndroidEntryPoint
class WeatherScreenFragment: Fragment() {

    private var binding : WeatherScreenFragmentBinding by autoCleared()

    //private val viewModel : WeatherViewModel by activityViewModels()
    private val viewModel : WeatherViewModel by viewModels()

    private var cityToDisplay=""
    //private var englishCity=""

    private lateinit var longitude:String
    private lateinit var latitude:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WeatherScreenFragmentBinding.inflate(inflater,container,false)
        setHasOptionsMenu(true)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //from home screen
        arguments?.getString("city")?.let {
            viewModel.setCity(it)
            cityToDisplay=it
        }


        //from history fragment
        arguments?.getString("city_history")?.let {
            viewModel.setCity(it)
        }

        //from the favorites screen
        arguments?.getString("fav_city")?.let {
            viewModel.setCity(it)
        }


        //if using transformation we will observe transformationWeather instead of weather
        //if not using transformation we will observe weather
        viewModel.transformationWeather.observe(viewLifecycleOwner){
            when(it.status){
                is Loading ->{
                    binding.progressBar.isVisible = true
                }
                is Success -> {

                    longitude=it.status.data!!.geoCoderResult.long.toString()
                    latitude= it.status.data!!.geoCoderResult.lat.toString()
                    binding.progressBar.isVisible = false
                    val dataCurrent = it.status.data!!.response.status.data!!.current
                    val forecast = it.status.data!!.response.status.data!!.forecast
                    val location = it.status.data!!.response.status.data!!.location

                    binding.hoursRv.adapter = WeatherHourAdapter(forecast.forecastday[0].hour,requireContext())
                    binding.hoursRv.layoutManager =LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)

                    binding.currentDegrees.text = dataCurrent.temp_c.toInt().toString()+"°"
                    binding.currentCityName.text = it.status.data!!.geoCoderResult.city.split(",")[0]
                    cityToDisplay = it.status.data!!.geoCoderResult.city.split(",")[0]
                    //in some occasions nearby shows in this answer and the api docs doesn't show it. we split it for localization means.
                    val currentConditionText = if(dataCurrent.condition.text.contains("nearby")) dataCurrent.condition.text.split("nearby")[0]
                    else dataCurrent.condition.text
                    when(currentConditionText){
                        "Sunny" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+getString(R.string.clear)
                        }
                        "Clear" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+getString(R.string.clear)
                        }

                        "Partly cloudy" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+ getString(R.string.partly_cloudy)
                        }

                        "Cloudy" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+ getString(R.string.cloudy)
                        }
                        "Overcast" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+ getString(R.string.overcast)
                        }
                        "Mist" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+ getString(R.string.mist)
                        }
                        "Patchy rain possible" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) +": "+ getString(R.string.patchy_rain_possible)
                        }
                        "Patchy snow possible" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_snow_possible)

                        }
                        "Patchy sleet" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_sleet)
                        }
                        "Patchy freezing drizzle possible" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_freezing_drizzle)

                        }
                        "Thundery outbreaks possible" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.thundery_outbreaks_possible)

                        }
                        "Blowing snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.blowing_snow)

                        }
                        "Blizzard" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.blizzard)

                        }
                        "Fog" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.fog)

                        }
                        "Freezing fog" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.freezing_fog)

                        }
                        "Patchy light drizzle" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_drizzle)

                        }
                        "Light drizzle" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_drizzle)

                        }
                        "Freezing drizzle" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.freezing_drizzle)

                        }
                        "Heavy freezing drizzle" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.heavy_freezing_drizzle)

                        }
                        "Patchy light rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_rain)

                        }
                        "Light rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_rain)

                        }
                        "Moderate rain at times" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_rain_at_times)

                        }
                        "Moderate rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_rain)

                        }
                        "Heavy rain at times" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.heavy_rain_at_times)

                        }
                        "Heavy rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.heavy_rain)

                        }
                        "Light freezing rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_freezing_rain)

                        }
                        "Moderate or heavy freezing rain" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_freezing_rain)

                        }
                        "Light sleet" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_sleet)

                        }
                        "Moderate or heavy sleet" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_sleet)

                        }
                        "Patchy light snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_snow)
                        }

                        "Light snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_snow)
                        }

                        "Patchy moderate snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_moderate_snow)
                        }

                        "Moderate snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_snow)
                        }

                        "Patchy heavy snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_heavy_snow)
                        }

                        "Heavy snow" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.heavy_snow)
                        }

                        "Ice pellets" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.ice_pellets)
                        }

                        "Light rain shower" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_rain_shower)
                        }

                        "Moderate or heavy rain shower" -> {
                            binding.myLocationCondition.text = getString(R.string.moderate_or_heavy_rain_shower)
                        }
                        "Torrential rain shower" -> {
                            binding.myLocationCondition.text = getString(R.string.torrential_rain_shower)

                        }
                        "Light sleet showers" -> {
                            binding.myLocationCondition.text = getString(R.string.light_sleet_showers)

                        }
                        "Moderate or heavy sleet showers" -> {
                            binding.myLocationCondition.text = getString(R.string.moderate_or_heavy_sleet_showers)

                        }
                        "Light snow showers" -> {
                            binding.myLocationCondition.text = getString(R.string.light_snow_showers)

                        }
                        "Moderate or heavy snow showers" -> {
                            binding.myLocationCondition.text = getString(R.string.moderate_or_heavy_snow_showers)

                        }
                        "Light showers of ice pellets" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.light_showers_of_ice_pellets)
                        }

                        "Moderate or heavy showers of ice pelltes" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_showers_of_ice_pelltes)
                        }

                        "Patchy light rain with thunder" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_rain_with_thunder)
                        }

                        "Moderate or heavy rain with thunder" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_rain_with_thunder)
                        }

                        "Patchy light snow with thunder" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_snow_with_thunder)
                        }

                        "Moderate or heavy snow with thunder" -> {
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_snow_with_thunder)
                        }

                        "Patchy rain nearby"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_rain_nearby)
                        }
                        "Patchy snow nearby"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_snow_nearby)
                        }
                        "Patchy sleet nearby"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_sleet_nearby)
                        }
                        "Patchy freezing drizzle nearby"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_freezing_drizzle_nearby)
                        }
                        "Thundery outbreaks in nearby"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.thundery_outbreaks_in_nearby)
                        }
                        "Moderate or heavy rain in area with thunder"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_rain_in_area_with_thunder)
                        }
                        "Patchy light snow in area with thunder"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.patchy_light_snow_in_area_with_thunder)
                        }
                        "Moderate or heavy snow in area with thunder"->{
                            binding.myLocationCondition.text = getString(R.string.current_weather) + ": " + getString(R.string.moderate_or_heavy_snow_in_area_with_thunder)
                        }
                    }
                    val max_deg = forecast.forecastday[0].day.maxtemp_c.toInt().toString()
                    val min_deg = forecast.forecastday[0].day.mintemp_c.toInt().toString()
                    binding.maxTempMinTempFeelsLike.text = max_deg+"°"+" / " + min_deg+"° " + getString(R.string.feels_like)+" " + dataCurrent.feelslike_c.toInt().toString()+"°"

                    //Progress bar and sunrise + sunset connected to the bar
                    val sunrise = setToInt(forecast.forecastday[0].astro.sunrise)
                    val sunset =  setToInt(forecast.forecastday[0].astro.sunset)
                    val currentTime = setCurrentToInt(location.localtime.split(" ")[1])
                    val maxTime = sunset - sunrise
                    val progressBar = currentTime - sunrise
                    binding.progressBarrr.max=maxTime
                    binding.progressBarrr.progress = progressBar
                    binding.sunriseTime.text=forecast.forecastday[0].astro.sunrise.split(" ")[0] +" " +  getString(R.string.PM)
                    binding.sunsetTime.text = forecast.forecastday[0].astro.sunset.split(" ")[0] +" " +  getString(R.string.AM)


                    //weekly adapter forecast
                    binding.weeklyRv.adapter = WeatherWeeklyAdapter(forecast.forecastday,requireContext())
                    binding.weeklyRv.layoutManager = LinearLayoutManager(requireContext())


                    //Wind + Humidity
                    binding.currentWind.text = dataCurrent.wind_kph.toInt().toString()+" " + getString(R.string.km_h)
                    binding.currentHumidity.text = dataCurrent.humidity.toInt().toString() + "%"
                    Glide.with(requireContext()).load(R.drawable.baseline_air_24).into(binding.windCurrentIcon)
                    Glide.with(requireContext()).load(R.drawable.baseline_water_drop_24).into(binding.humidityCurrentIcon)


                    //Moon card
                    when(forecast.forecastday[0].astro.moonrise.split(" ")[1]){
                        "AM" -> {
                            binding.moonriseWeather.text = forecast.forecastday[0].astro.moonrise.split(" ")[0] + " " + getString(R.string.AM)
                        }
                        "PM" -> {
                            binding.moonriseWeather.text = forecast.forecastday[0].astro.moonrise.split(" ")[0] + " " + getString(R.string.PM)
                        }
                    }

                    when(forecast.forecastday[0].astro.moonset.split(" ")[1]){
                        "AM" -> {
                            binding.moonsetWeather.text = forecast.forecastday[0].astro.moonset.split(" ")[0] + " " + getString(R.string.AM)
                        }
                        "PM" -> {
                            binding.moonsetWeather.text = forecast.forecastday[0].astro.moonset.split(" ")[0] + " " + getString(R.string.PM)
                        }
                    }
                    binding.moonPhaseWeather.text = forecast.forecastday[0].astro.moon_phase



                    //Background changes based on current time
                        val hoursResponse = location.localtime.split(" ")[1].split(":")[0].toInt()

                        if((hoursResponse > 6 && hoursResponse<16) && (dataCurrent.condition.text.contains("cloudy",true)
                                    ||dataCurrent.condition.text.contains("fog",false))){
                            binding.root.setBackgroundResource(R.drawable.cloudy_day)
                        }
                        else if (hoursResponse > 6 && hoursResponse<16){
                            binding.root.setBackgroundResource(R.drawable.morning_colors)
                        }
                        else if(hoursResponse>=16 && hoursResponse<18){
                            binding.root.setBackgroundResource(R.drawable.afternoon_colors)
                        }
                        else {
                            binding.root.setBackgroundResource(R.drawable.night_colors)
                        }
                }

                is Error-> {
                    binding.progressBar.isVisible = false
                    when(it.status.message){
                        "No such city!" -> {
                            Toast.makeText(requireContext(),getString(R.string.no_such_city),Toast.LENGTH_SHORT).show()
                        }
                        "Network call has failed" -> {
                            Toast.makeText(requireContext(),getString(R.string.network_call_has_failed),Toast.LENGTH_SHORT).show()
                        }
                        "Some exception caught" -> {
                            Toast.makeText(requireContext(),getString(R.string.exception_cagught),Toast.LENGTH_SHORT).show()
                        }
                    }
                    findNavController().navigate(R.id.action_weatherScreenFragment_to_homeScreenFragment)
                }
            }
        }

        binding.btnShowMap.setOnClickListener {
            val location = "$latitude,$longitude"
            openMapsWithLocation(location)
        }
    }

    private fun setToInt(time : String): Int{
        var tmp = time.take(5)
        var hours = time.take(2)
        var minutes = tmp.takeLast(2)

        if( hours.take(1).toInt() == 0 ){
            hours = hours.takeLast(1)
        }
        if( minutes.take(1).toInt() == 0 ){
            minutes = minutes.takeLast(1)
        }

        return if( "pm" in time || "PM" in time){
            60*(hours.toInt() + 12 ) + minutes.toInt()
        }else{
            60*(hours.toInt()  ) + minutes.toInt()
        }
    }

    private fun setCurrentToInt(time : String): Int{
        if(time.length==4){
            val tmp = time.take(4)
            val hours = time.take(1)
            var minutes = tmp.takeLast(2)

            if( minutes.take(1).toInt() == 0 ){
                minutes = minutes.takeLast(1)
            }
            return 60*(hours.toInt()) + minutes.toInt()
        }
        else {
            val tmp = time.take(5)
            val hours = time.take(2)
            var minutes = tmp.takeLast(2)

            if( minutes.take(1).toInt() == 0 ){
                minutes = minutes.takeLast(1)
            }
            return 60*(hours.toInt()) + minutes.toInt()
        }
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.favourite_icon){
            val options = arrayOf(getString(R.string.add_city_to_favorites) ,
                getString(R.string.go_to_favorites_screen))

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.choose_an_option))
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.insertCity(Favourites(cityToDisplay))
                    }
                    1 -> {
                        findNavController().navigate(R.id.action_weatherScreenFragment_to_favouritesFragment)
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
        else if (item.itemId == R.id.home_btn_icon){
            findNavController().navigate(R.id.action_weatherScreenFragment_to_homeScreenFragment)
        }
        else if(item.itemId== R.id.history_icon){
            findNavController().navigate(R.id.action_weatherScreenFragment_to_historyFragment)
        }
        return super.onOptionsItemSelected(item)
    }


    private fun openMapsWithLocation(location: String) {
        val mapIntentUri = Uri.parse("geo:0,0?q=$location")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Specify the package name of Google Maps app to ensure it's used
        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Google Maps app is not installed, handle accordingly (open web browser, show error message, etc.)
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps")
            )
            startActivity(intent)
        }
    }
}


