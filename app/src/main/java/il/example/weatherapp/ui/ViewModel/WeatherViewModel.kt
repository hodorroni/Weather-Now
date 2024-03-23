package il.example.weatherapp.ui.ViewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.example.weatherapp.data.Repository.GeoCoderRepository
import il.example.weatherapp.data.Repository.WeatherRepository
import il.example.weatherapp.data.models.LocalModels.Favourites
import il.example.weatherapp.data.models.LocalModels.History
import il.example.weatherapp.data.models.coordinates.MyLatLong
import il.example.weatherapp.data.models.new_api.AllDataNew
import il.example.weatherapp.utils.Error
import il.example.weatherapp.utils.Resource
import il.example.weatherapp.utils.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val geoCoderRepository: GeoCoderRepository
) : ViewModel() {


    val cityHomeScreen = MutableLiveData<String>()


    //using transformation to fetch data only if the city variable is changed
    //in that way we obtain the chance to fetch data only of the data of the weather has changed and not in everytime we are gonna get to that screen
    val city2 = MutableLiveData<String>()
    val transformationWeather = city2.switchMap { cityName ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Resource.loading())
            val geoResult = geoCoderRepository.cityToLongLat(cityName)
            if (geoResult.status is Success) {
                val concated_lat_long = "${geoResult.status.data!!.lat},${geoResult.status.data!!.long}"
                val city = geoResult.status.data!!
                val toParse = EnglishCity(weatherRepository.fetchWeather(concated_lat_long),city)
                if(toParse.response.status is Success){
                    emit(Resource.success(toParse))
                }
                else if(toParse.response.status is Error){
                    emit(Resource.error(toParse.response.status.message))
                }

            }
            else if(geoResult.status is Error) {
                emit(Resource.error(geoResult.status.message))
            }
        }
    }

    //in our fragments that function will trigger the transformation above
    fun setCity(cityInput:String){
        if(city2.value!=cityInput){
            city2.value = cityInput
        }
    }


    fun insertCity(city:Favourites){
        viewModelScope.launch(Dispatchers.IO) {
            val cityCapitalize = city.city.capitalize().trim()
            val capitalizedCity = city.copy(city = cityCapitalize)
            weatherRepository.insertCity(capitalizedCity)
        }
    }

    //to convert google current location to real city
    //for the HomeScreen -> used for the fusedLocation google service
//    fun latLongToCity(lat:Double,long:Double){
//        viewModelScope.launch(Dispatchers.IO) {
//            val cityResult = geoCoderRepository.longLatToCity(long,lat)
//            if(cityResult.status is Success){
//                cityHomeScreen.postValue(cityResult.status.data!!)
//            }
//            else if(cityResult.status is Error) {
//                cityHomeScreen.postValue(cityResult.status.message)
//            }
//        }
//    }

    fun insertToHistory(city:History){
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.insertToHistory(city)
        }
    }
}
