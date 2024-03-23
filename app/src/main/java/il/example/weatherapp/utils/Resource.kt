package il.example.weatherapp.utils

class Resource<out T> private constructor(val status: Status<T>){

    companion object {
        fun<T> success(data : T) = Resource(Success(data))
        fun<T> error(message: String, data : T?  = null) = Resource(Error(message,data))
        fun<T> loading(data : T? = null) = Resource(Loading(data))
    }
}


//in compile time we would know all of the subclasses of Status -> no need of else in the when() block
sealed class Status<out T>(val data : T?  = null)

class Success<T>(data: T) : Status<T>(data)
class Error<T>(val message : String, data: T? = null) : Status<T>(data)
class Loading<T>(data: T? = null) : Status<T>(data)