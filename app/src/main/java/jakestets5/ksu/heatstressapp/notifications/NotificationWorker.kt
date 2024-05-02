package jakestets5.ksu.heatstressapp.notifications

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jakestets5.ksu.heatstressapp.data.`object`.NotificationData
import jakestets5.ksu.heatstressapp.helpers.database.SavedLocationsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.database.SettingsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.formula.RadiationFormulaHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.util.concurrent.CountDownLatch
import kotlin.math.roundToInt

/**
 * A worker class that handles asynchronous fetch and notification of weather data for saved locations.
 * It extends the Worker class from the Android WorkManager API, which allows it to perform background tasks.
 *
 * @param context The application context used to access application-wide features and system services.
 * @param workerParams Parameters for this worker, containing runtime information like tags and arguments.
 */
class NotificationWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams)  {

    private val apiKey: String = "ea3b3c4926888b3de6571a6204c16818"

    // Setting dummy values for variables used in CCI calculations
    private var windSpeed = 0.0
    private var solarRadiation = 0.0
    private var ambientTemperature: Double = 0.0
    private var relativeHumidity: Double = 0.0
    private var currentCity = "New York"
    private var currentLat = 0.0
    private var currentDate: Long = 0
    private var locationListSize = 0
    private var notificationString = ""

    // Setting variables used to adjust the CCI
    private var breedAdjustment = 0
    private var colorAdjustment = 0
    private var acclimationAdjustment = 5
    private var healthAdjustment = 5
    private var shadeAdjustment = 0
    private var feedAdjustment = 2
    private var manureAdjustment = 8
    private var waterAdjustment = 2

    // Lists for storing forecasted weather data.
    private var precipitationList = mutableListOf<String>()
    private var humidityList = mutableListOf<String>()
    private var windSpeedList = mutableListOf<String>()
    private var threatLevelList = mutableListOf<String>()
    private var lowTempList = mutableListOf<String>()
    private var highTempList = mutableListOf<String>()
    private var dayOfWeekList = mutableListOf<String>()
    private var radiationList = mutableListOf<Double>()
    private var completeLocationList = mutableListOf<ArrayList<NotificationData>>()
    private var notificationThreatList: ArrayList<String> = ArrayList()
    private var notificationList: ArrayList<NotificationData> = ArrayList()

    private val radiationFormulaHelper = RadiationFormulaHelper()

    // Database helpers for accessing saved locations and settings.
    private lateinit var savedLocationsdbHelper: SavedLocationsDatabaseHelper
    private lateinit var settingsdbHelper: SettingsDatabaseHelper

    // A listener to handle callbacks when weather data fetching is complete.
    private var listener: NotificationListener? = null

    /**
     * Orchestrates the fetching of weather data for each saved location
     * and subsequently sends notifications based on the weather conditions.
     *
     * @return The result of the work, either SUCCESS or RETRY, depending on whether the work needs to be retried.
     */
    override fun doWork(): Result {
        savedLocationsdbHelper = SavedLocationsDatabaseHelper(this.applicationContext)
        val locationList = savedLocationsdbHelper.getAllLocations()
        locationListSize = locationList.size

        // Synchronizer to ensure all asynchronous operations complete before proceeding.
        val countDownLatch = CountDownLatch(locationList.size)
        for(location in locationList){
            currentCity = location.city
            val lat = location.latitude
            val long = location.longitude
            getForecastApiDetails(lat, long, countDownLatch)
        }

        try {
            countDownLatch.await()
            sendNotificationWithWeatherDetails()
            return Result.success()
        } catch (e: InterruptedException) {
            return Result.retry()
        }
    }

    /**
     * Fetches detailed weather forecasts from a remote API and processes this data
     * to determine the necessary notifications to send based on certain thresholds.
     */
    private fun fetchWeatherDetails(): Boolean{
        var success = false
        savedLocationsdbHelper = SavedLocationsDatabaseHelper(this.applicationContext)
        val locationList = savedLocationsdbHelper.getAllLocations()
        notificationThreatList.clear()

        for(location in locationList){
            setNotificationUpdateListener(object : NotificationListener {
                override fun onDataFetched(dayOfWeekFetched: MutableList<String>, precipitationListFetched: MutableList<String>, humidityListFetched: MutableList<String>, windSpeedListFetched: MutableList<String>, threatListFetched: MutableList<String>, lowTempListFetched: MutableList<String>, highTempListFetched: MutableList<String>) {
                    var dayCount = 0
                    for(threat in threatListFetched){
                        if(threat != "low"){
                            notificationList.add(NotificationData(currentCity, threat, dayOfWeekFetched[dayCount]))
                        }
                        dayCount += 1
                    }
                    completeLocationList.add(notificationList)
                    success = true
                }
            })
        }
        return success
    }

    /**
     * Sends out notifications compiled from the fetched weather data, alerting users to potential weather threats.
     */
    private fun sendNotificationWithWeatherDetails() {
        notificationString = ""
        if(completeLocationList.isNotEmpty()){
            var count = 0
            for(notificationList in completeLocationList){
                for(item in notificationList){
                    if(item.threat != "low"){
                        notificationString += "Threat in ${notificationList[count].city}: ${notificationList[count].threat}, on ${notificationList[count].dayOfWeek}, "
                    }
                    count += 1
                }
            }
        }
            NotificationHelper(applicationContext).sendNotification(
                "Heat Stress Alert",
                notificationString
            )
    }

    /**
     * Fetches the forecast data from the OpenWeatherMap API for the given latitude and longitude.
     *
     * @param lat The latitude of the location for which weather data is requested.
     * @param long The longitude of the location for which weather data is requested.
     * @param latch The CountDownLatch that assists in executing asynchronous tasks
     */
    private fun getForecastApiDetails(lat: Double, long: Double, latch: CountDownLatch) {

        val weatherMapURL =
            "https://api.openweathermap.org/data/2.5/forecast/daily?lat=$lat&lon=$long&cnt=7&appid=$apiKey"
        currentLat = lat

        //start of the api request
        val weatherMapRequest = object : StringRequest(
            Method.GET, weatherMapURL,
            Response.Listener<String> { response ->
                var output = ""
                try {
                    //clearing any previous data
                    precipitationList.clear()
                    humidityList.clear()
                    windSpeedList.clear()
                    threatLevelList.clear()
                    lowTempList.clear()
                    highTempList.clear()
                    dayOfWeekList.clear()

                    val jsonObj = JSONObject(response)
                    val daily = jsonObj.getJSONArray("list")
                    for(i in 0 until daily.length()){
                        val forecast = daily.getJSONObject(i)

                        //getting all temperature related variables, and setting the global variable for use in the comprehensiveClimateIndex
                        val temp = forecast.getJSONObject("temp")
                        val tempMaxInt = ((9/5) * (temp.optDouble("min", 20.0) - 273) + 42).roundToInt()
                        ambientTemperature = tempMaxInt.toDouble()

                        //getting all humidity related variables, and setting the global variable for use in the comprehensiveClimateIndex
                        val humidity = forecast.getDouble("humidity")
                        relativeHumidity = humidity

                        //getting all wind related variables, and setting the global variable for use in the comprehensiveClimateIndex
                        val windSpeedDouble = forecast.getDouble("speed")
                        windSpeed = windSpeedDouble

                        //getting the day of the week
                        val timestamp = forecast.getLong("dt") * 1000 // Convert to milliseconds
                        val date = Date(timestamp)
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                        val readableDayOfWeek = daysOfWeek[dayOfWeek - 1]
                        currentDate = timestamp

                        //get the radiation for the day
                        solarRadiation = radiationFormulaHelper.getSolarRadiation(lat, timestamp)
                        radiationList.add(solarRadiation)

                        //calling the comprehensiveClimateIndex to asses the threat level
                        settingsdbHelper = SettingsDatabaseHelper(this.applicationContext)
                        val settings = settingsdbHelper.getSettings()
                        breedAdjustment = settings.breed
                        colorAdjustment = settings.color
                        acclimationAdjustment = settings.acclimation
                        healthAdjustment = settings.health
                        shadeAdjustment = settings.shade
                        feedAdjustment = settings.feed
                        manureAdjustment = settings.manure
                        waterAdjustment = settings.water
                        val threatLevel = comprehensiveClimateIndex()

                        val city = savedLocationsdbHelper.getCity(lat, long)
                        notificationList.add(NotificationData(city, threatLevel, readableDayOfWeek))
                    }
                    fetchWeatherDetails()
                    listener?.onDataFetched(dayOfWeekList, precipitationList, humidityList, windSpeedList, threatLevelList, lowTempList, highTempList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                finally {
                    latch.countDown()
                }
            },
            Response.ErrorListener { error ->
                Log.e("WeatherAppError", error.toString())
                latch.countDown()
            }
        ) {
        }
        //adding the api request to the request queue
        val requestQueue = Volley.newRequestQueue(this.applicationContext)
        requestQueue.add(weatherMapRequest)
    }

    /**
     * Defines the interface for notification data fetching, allowing the implementation of callback methods
     * to handle the fetched data.
     */
    interface NotificationListener {
        fun onDataFetched(dayOfWeekFetched: MutableList<String>, precipitationListFetched: MutableList<String>, humidityListFetched: MutableList<String>, windSpeedListFetched: MutableList<String>, threatListFetched: MutableList<String>, lowTempListFetched:  MutableList<String>, highTempListFetched: MutableList<String>)
    }

    /**
     * Sets the notification update listener to handle callbacks when weather data is fetched.
     */
    private fun setNotificationUpdateListener(listener: NotificationListener) {
        this.listener = listener
    }

    /**
     * Computes the comprehensive climate index (CCI) using the formula helpers based on weather data and adjustments.
     *
     * @return The computed threat level as a String based on the CCI.
     */
    fun comprehensiveClimateIndex(): String{
        val cci = radiationFormulaHelper.calculateCCI(ambientTemperature, relativeHumidity, windSpeed, radiationFormulaHelper.getSolarRadiation(currentLat, currentDate))
        val adjustedCci = cci + breedAdjustment + acclimationAdjustment + colorAdjustment + feedAdjustment + healthAdjustment + manureAdjustment + shadeAdjustment + waterAdjustment

        if(adjustedCci <= 65.0){
            return "low"
        }
        else if(adjustedCci in 65.001..86.0){
            return "medium"
        }
        else{
            return "high"
        }
    }
}
