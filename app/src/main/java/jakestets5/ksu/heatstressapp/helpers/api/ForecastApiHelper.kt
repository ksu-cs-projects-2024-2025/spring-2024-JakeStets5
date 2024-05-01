package jakestets5.ksu.heatstressapp.helpers.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jakestets5.ksu.heatstressapp.helpers.database.SettingsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.formula.RadiationFormulaHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import kotlin.math.exp
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Helper class to fetch weather forecast data from an API and compute various indices for assessing weather conditions.
 *
 * @param context The context from which this helper is called, used for making network requests and accessing resources.
 * @param bA Breed adjustment factor for CCI calculations.
 * @param cA Color adjustment factor for CCI calculations.
 * @param aA Acclimation adjustment factor for CCI calculations.
 * @param hA Health adjustment factor for CCI calculations.
 * @param sA Shade adjustment factor for CCI calculations.
 * @param fA Feed adjustment factor for CCI calculations.
 * @param mA Manure adjustment factor for CCI calculations.
 * @param wA Water adjustment factor for CCI calculations.
 */
class ForecastApiHelper(context: Context, bA: Int, cA: Int, aA: Int, hA: Int, sA: Int, fA: Int, mA: Int, wA: Int) {

    private val thisContext = context
    private val apiKey: String = "ea3b3c4926888b3de6571a6204c16818" // Change this to your OpenWeatherMap API key
    var isDay: Boolean = true
    private var userLat = 0.0
    private var userDate: Long = 0

    private var listener: ForecastListener? = null

    // Variables that will hold forecast information
    private var precipitationList = mutableListOf<String>()
    private var humidityList = mutableListOf<String>()
    private var windSpeedList = mutableListOf<String>()
    private var threatLevelList = mutableListOf<String>()
    private var lowTempList = mutableListOf<String>()
    private var highTempList = mutableListOf<String>()
    private var dayOfWeekList = mutableListOf<String>()
    private var radiationList = mutableListOf<Double>()

    // Variables to calculate CCI
    var ambientTemperature: Double = 0.0
    var relativeHumidity: Double = 0.0
    var windSpeed: Double = 0.0
    var solarRadiation: Double = 0.0

    // Variables to adjust the CCI
    private var breedAdjustment = bA
    private var colorAdjustment = cA
    private var acclimationAdjustment = aA
    private var healthAdjustment = hA
    private var shadeAdjustment = sA
    private var feedAdjustment = fA
    private var manureAdjustment = mA
    private var waterAdjustment = wA

    // Helper variables for CCI calculation
    private var radiationFormulaHelper = RadiationFormulaHelper()
    private lateinit var settingsdbHelper: SettingsDatabaseHelper

    /**
     * Adds a location to the internal list managed by this helper.
     *
     * @param location The location data to add.
     */
    private fun addToList(dayOfWeek: String, precipitation: String, humidity: String, windSpeed: String, threatLevel: String, lowTemp: String, highTemp: String){
        precipitationList.add(precipitation)
        humidityList.add(humidity)
        windSpeedList.add(windSpeed)
        threatLevelList.add(threatLevel)
        lowTempList.add(lowTemp)
        highTempList.add(highTemp)
        dayOfWeekList.add(dayOfWeek)
    }

    /**
     * Interface to provide callbacks for when data is fetched successfully from the API.
     */
    interface ForecastListener {
        fun onDataFetched(dayOfWeekFetched: MutableList<String>, precipitationListFetched: MutableList<String>, humidityListFetched: MutableList<String>, windSpeedListFetched: MutableList<String>, threatListFetched: MutableList<String>, lowTempListFetched:  MutableList<String>, highTempListFetched: MutableList<String>)
    }

    /**
     * Registers a listener that will be notified when the forecast data has been fetched and processed.
     *
     * @param listener The listener that will be notified of data fetching events.
     */
    fun setForecastUpdateListener(listener: ForecastListener) {
        this.listener = listener
    }

    /**
     * Fetches the forecast data from the OpenWeatherMap API for the given latitude and longitude.
     *
     * @param lat The latitude of the location for which weather data is requested.
     * @param long The longitude of the location for which weather data is requested.
     */
    fun getForecastApiDetails(lat: Double, long: Double) {

        val weatherMapURL =
            "https://api.openweathermap.org/data/2.5/forecast/daily?lat=$lat&lon=$long&cnt=7&appid=$apiKey"

        userLat = lat
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
                        val tempMinInt = ((9/5) * (temp.optDouble("min", 20.0) - 273) + 32).roundToInt()
                        val tempMaxInt = ((9/5) * (temp.optDouble("max", 20.0) - 273) + 42).roundToInt()
                        val tempMin = "$tempMinInt" + "°F"
                        val tempMax = "$tempMaxInt" + "°F"
                        ambientTemperature = tempMaxInt.toDouble()

                        //getting all humidity related variables, and setting the global variable for use in the comprehensiveClimateIndex
                        val humidity = forecast.getDouble("humidity")
                        val humidityForView = humidity.roundToInt()
                        val humidityString = humidityForView.toString() + "%"
                        relativeHumidity = humidity

                        //getting all wind related variables, and setting the global variable for use in the comprehensiveClimateIndex
                        val windSpeedDouble = forecast.getDouble("speed")
                        val windForView = windSpeedDouble.roundToInt()
                        val windSpeedString = windForView.toString() + " mph"
                        windSpeed = windSpeedDouble

                        //getting the day of the week
                        val timestamp = forecast.getLong("dt") * 1000 // Convert to milliseconds
                        userDate = timestamp /1000
                        val date = Date(timestamp)
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                        val readableDayOfWeek = daysOfWeek[dayOfWeek - 1]

                        //get the radiation for the day
                        solarRadiation = radiationFormulaHelper.getSolarRadiation(lat, timestamp)
                        radiationList.add(solarRadiation)

                        //calling the comprehensiveClimateIndex to asses the threat level
                        settingsdbHelper = SettingsDatabaseHelper(thisContext)
                        val settings = settingsdbHelper.getSettings()
                        breedAdjustment = settings.breed
                        colorAdjustment = settings.color
                        acclimationAdjustment = settings.acclimation
                        healthAdjustment = settings.health
                        shadeAdjustment = settings.shade
                        feedAdjustment = settings.feed
                        manureAdjustment = settings.manure
                        waterAdjustment = settings.water
                        val threatLevel = comprehensiveClimateIndex(breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)

                        //getting all precipitation related variables
                        val precipitation = forecast.getDouble("pop")
                        val precipitationPercentage = (precipitation * 100).toInt()
                        val precipitationForList = precipitationPercentage.toString() + "%"

                        //adding all the variables to the list for the recyclerview
                        addToList(readableDayOfWeek, precipitationForList, humidityString, windSpeedString, threatLevel, tempMin, tempMax)
                    }

                    listener?.onDataFetched(dayOfWeekList, precipitationList, humidityList, windSpeedList, threatLevelList, lowTempList, highTempList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(thisContext, error.toString().trim(), Toast.LENGTH_LONG).show()
                Log.e("WeatherAppError", error.toString())
            }
        ) {
        }
        //adding the api request to the request queue
        val requestQueue = Volley.newRequestQueue(thisContext.applicationContext)
        requestQueue.add(weatherMapRequest)
    }

    /**
     * Computes the comprehensive climate index (CCI) using the formula helpers based on weather data and adjustments.
     *
     * @return The computed threat level as a String based on the CCI.
     */
    fun comprehensiveClimateIndex(bA: Int, cA: Int, aA: Int, hA: Int, sA: Int, fA: Int, mA: Int, wA: Int): String{
        //uses the formula helpers to be implemented when hourly data can be retrieved
        val cci = radiationFormulaHelper.calculateCCI(ambientTemperature, relativeHumidity, windSpeed, radiationFormulaHelper.getSolarRadiation(userLat, userDate))
        val adjustedCci = cci + bA + cA + aA + hA + sA + fA + mA + wA

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