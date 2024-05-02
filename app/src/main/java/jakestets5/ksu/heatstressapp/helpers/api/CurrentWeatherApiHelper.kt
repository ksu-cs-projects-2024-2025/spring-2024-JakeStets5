package jakestets5.ksu.heatstressapp.helpers.api

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.MainActivity
import jakestets5.ksu.heatstressapp.adapters.recycler.CitiesRecyclerAdapter
import jakestets5.ksu.heatstressapp.data.`object`.ApiData
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import jakestets5.ksu.heatstressapp.helpers.formula.RadiationFormulaHelper
import jakestets5.ksu.heatstressapp.helpers.database.SavedLocationsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.ui.MainUIHelper
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Helper class for interfacing with weather APIs, processing weather data, and managing related updates in the application.
 *
 * @param ma The MainActivity instance where this helper is utilized.
 * @param lat Initial latitude for weather data.
 * @param long Initial longitude for weather data.
 * @param bA Adjustment factor for breed in the climate comfort index.
 * @param cA Adjustment factor for color.
 * @param aA Adjustment factor for acclimation.
 * @param hA Adjustment factor for health.
 * @param sA Adjustment factor for shade.
 * @param fA Adjustment factor for feed.
 * @param mA Adjustment factor for manure.
 * @param wA Adjustment factor for water.
 */
class CurrentWeatherApiHelper(ma: MainActivity, lat: Double, long: Double, bA: Int, cA: Int, aA: Int, hA: Int, sA: Int, fA: Int, mA: Int, wA: Int) {

    var userCity: String = "New York City"
    var userState: String = "New York"
    var userCountryCode: String = "US"
    private var userCountry: String = "United States"
    private val weatherApiKey: String = "ea3b3c4926888b3de6571a6204c16818" // Change this to your OpenWeatherMap API key
    private val geocodeApiKey: String = "AIzaSyBLHqkOaDoS3QK1Ynaw2xxKD0OvioBczVY" // Change this to your geocode API key
    var userLat = lat
    var userLong = long
    var cloudCoverageGlobal: String = "clear_skies"
    var userDate: Long = 0
    var isDay: Boolean = true
    val mainActivity = ma

    // Variables to calculate CCI
    var ambientTemperature: Double = 0.0
    var relativeHumidity: Double = 0.0
    var windSpeed: Double = 0.0

    // Variables to adjust the CCI
    private var breedAdjustment = bA
    private var colorAdjustment = cA
    private var acclimationAdjustment = aA
    private var healthAdjustment = hA
    private var shadeAdjustment = sA
    private var feedAdjustment = fA
    private var manureAdjustment = mA
    private var waterAdjustment = wA

    private val radiationFormulaHelper = RadiationFormulaHelper()

    //setting up variables for the location search recycler view
    private var locationList = ArrayList<LocationData>()

    //listener for the event of weather details retrieved
    private var listener: WeatherUpdateListener? = null

    //Initialize api helpers
    private lateinit var mainUIHelper: MainUIHelper

    /**
     * Updates weather details based on the given city and coordinates. It also triggers comprehensive climate index calculations.
     *
     * @param city The city for which weather details are being updated.
     * @param initial Flag to check if it's the initial setup.
     * @param lat Latitude of the location.
     * @param long Longitude of the location.
     * @return An ApiData object containing state and day/night information.
     */
    fun updateHelperWeatherDetails(city: String, initial: Boolean, lat: Double, long: Double): ApiData{
        return ApiData(getState(lat, long, object: GeocodeListener {
            override fun onDataFetched(state: String) {
                getWeatherDetails(city, initial, lat, long)
            }
        }), isDay)
    }

    /**
     * Adds a location to the internal list managed by this helper.
     *
     * @param location The location data to add.
     */
    //adds the location to the list of saved locations
    private fun addToList(location: LocationData){
        locationList.add(location)
    }

    /**
     * Interface to listen for geocode data fetching events.
     */
    private interface GeocodeListener{
        fun onDataFetched(state: String)
    }

    /**
     * Interface for callbacks when weather data is updated.
     */
    interface WeatherUpdateListener {
        fun onWeatherDataUpdated(isDay: Boolean, city: String)
    }

    /**
     * Registers a listener to receive updates when weather data is updated.
     *
     * @param listener The listener to notify of weather data updates.
     */
    fun setWeatherUpdateListener(listener: WeatherUpdateListener) {
        this.listener = listener
    }

    /**
     * Notifies registered listeners that weather data has been updated.
     *
     * @param isDay Boolean indicating whether it is currently day or night.
     * @param city The city for which weather data has been updated.
     */
    fun notifyWeatherUpdated(isDay: Boolean, city: String) {
        listener?.onWeatherDataUpdated(isDay, city)
    }

    /**
     * Updates the city recycler view with a list of cities based on search results.
     *
     * @param citiesRecyclerAdapter The adapter for the cities recycler view.
     * @param searchResults List of location data that will be used to update the recycler view.
     */
    fun updateCityRecycler(citiesRecyclerAdapter: CitiesRecyclerAdapter, searchResults: List<LocationData>){
        for(i in searchResults) {
            val city = i.city
            val country = i.country
            val latitude = i.latitude
            userLat = latitude
            val longitude = i.longitude
            userLong = longitude
            val countryCode = i.countryCode
            LocationData(userCity, userCountry, userState, userLat, userLong, userCountryCode)
            getState(latitude, longitude, object : GeocodeListener {
                override fun onDataFetched(state: String) {
                    addToList(LocationData(city, country, state, latitude, longitude, countryCode))
                    citiesRecyclerAdapter.updateBackground(isDay)
                    citiesRecyclerAdapter.setFilteredList(locationList)
                    citiesRecyclerAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    /**
     * Handles actions when the save button is clicked in the recycler view.
     *
     * @param initial Indicates if this is the initial setup.
     * @param sldbHelper Database helper for saved locations.
     * @param city City name.
     * @param country Country name.
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @param countryCode Country code of the location.
     * @return ApiData object containing the updated state and day/night information.
     */
    fun saveRecyclerClick(initial: Boolean, sldbHelper: SavedLocationsDatabaseHelper, city: String, country: String, latitude: Double, longitude: Double, countryCode: String): ApiData{
        if(countryCode == "US"){
            return ApiData(getState(latitude, longitude, object: GeocodeListener {
                override fun onDataFetched(state: String) {
                    updateHelperWeatherDetails(city, initial, latitude, longitude)
                    if(sldbHelper.addLocation(mainActivity, city, country, state, latitude.toString(), longitude.toString(), countryCode )){
                        Toast.makeText(mainActivity, "City added to saved locations", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(mainActivity, "Failed to add to saved locations", Toast.LENGTH_SHORT).show()
                    }
                }
            }), isDay)
        }
        else{
            updateHelperWeatherDetails(city, initial, latitude, longitude)
            if(sldbHelper.addLocation(mainActivity, city, country, "", latitude.toString(), longitude.toString(), countryCode )){
                Toast.makeText(mainActivity, "City added to saved locations", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(mainActivity, "Failed to add to saved locations", Toast.LENGTH_SHORT).show()
            }
            return ApiData("", isDay)
        }
    }

    /**
     * Handles the recycler item click event to update weather details.
     *
     * @param city The city name.
     * @param initial Indicates if this is the initial setup.
     * @param lat Latitude of the location.
     * @param long Longitude of the location.
     * @return ApiData object containing the updated state and day/night information.
     */
    fun cityRecyclerClick(city: String, initial: Boolean, lat: Double, long: Double): ApiData{
        return ApiData(getState(lat, long, object: GeocodeListener {
            override fun onDataFetched(state: String) {
                updateHelperWeatherDetails(city, initial, lat, long)
            }
        }), isDay)
    }

    /**
     * Retrieves the state based on latitude and longitude using a geocoding API.
     *
     * @param lat Latitude of the location.
     * @param long Longitude of the location.
     * @param listener GeocodeListener to notify when state information is fetched.
     * @return The state as a string.
     */
    private fun getState(lat: Double, long: Double, listener: GeocodeListener): String{
        val weatherMapURL =
            "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$long&key=$geocodeApiKey"

        //start of the api call
        val geocodeRequest = object : StringRequest(Method.GET, weatherMapURL,
            Response.Listener<String> { response ->
                var output = ""
                try {
                    val jsonObject = JSONObject(response)
                    val resultsArray = jsonObject.getJSONArray("results")
                    if (resultsArray.length() > 0) {
                        val addressComponents = resultsArray.getJSONObject(0).getJSONArray("address_components")
                        for (i in 0 until addressComponents.length()) {
                            val component = addressComponents.getJSONObject(i)
                            val types = component.getJSONArray("types")
                            for (j in 0 until types.length()) {
                                if (types.getString(j) == "administrative_area_level_1") {
                                    userState = component.getString("long_name")
                                }
                            }
                        }
                    }
                    //notifying listener via callback
                    listener.onDataFetched(userState)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(mainActivity.applicationContext, error.toString().trim(), Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
        }
        val requestQueue = Volley.newRequestQueue(mainActivity.applicationContext)
        requestQueue.add(geocodeRequest)
        return userState
    }

    /**
     * Fetches current weather data from the OpenWeatherMap API.
     *
     * @param city City name for which weather is fetched.
     * @param initial Indicates if this is the initial setup.
     * @param lat Latitude of the location.
     * @param long Longitude of the location.
     * @return The state as a string, usually used in updating UI elements.
     */
    fun getWeatherDetails(city: String, initial: Boolean, lat: Double, long: Double): String {
        val weatherMapURL =
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$long&appid=$weatherApiKey"
        val radiationFormulaHelper = RadiationFormulaHelper()

        //start of the api call
        val weatherMapRequest = object : StringRequest(Method.GET, weatherMapURL,
            Response.Listener<String> { response ->
                var output = ""
                try {
                    val jsonObj = JSONObject(response)

                    // Wind
                    val wind = jsonObj.getJSONObject("wind")
                    val speed = wind.getDouble("speed")
                    windSpeed = speed

                    // Rain and Snow Volume
                    val rainVolume = jsonObj.optJSONObject("rain")?.getDouble("1h") ?: 0.0
                    val rainInt = rainVolume.roundToInt()
                    val snowVolume = jsonObj.optJSONObject("snow")?.getDouble("1h") ?: 0.0

                    // Cloud Coverage
                    val cloudCoverage = jsonObj.getJSONObject("clouds").getInt("all")
                    var cloudCoverageString = "Clear skies"
                    if (cloudCoverage in 0..20) {
                        cloudCoverageString = "Clear skies"
                        cloudCoverageGlobal = "clear_sky"
                    } else if (cloudCoverage in 21..40) {
                        cloudCoverageString = "Partly cloudy"
                        cloudCoverageGlobal = "clear_sky"
                    } else if (cloudCoverage in 41..60) {
                        cloudCoverageString = "Mostly cloudy"
                        cloudCoverageGlobal = "cloudy_sky"
                    } else if (cloudCoverage in 61..80) {
                        cloudCoverageString = "Cloudy"
                        cloudCoverageGlobal = "cloudy_sky"
                    } else if (80 < cloudCoverage) {
                        cloudCoverageString = "Overcast"
                        cloudCoverageGlobal = "cloudy_sky"
                    }

                    // Updated Time
                    val updatedAt = jsonObj.getLong("dt")
                    userDate = updatedAt

                    // Temperature and Humidity
                    val main = jsonObj.getJSONObject("main")
                    val temp = (9/5) * (main.getDouble("temp") - 273.15) + 32
                    val tempMin = (9/5) * (main.getDouble("temp_min") - 273.15) + 32
                    val tempMax = (9/5) * (main.getDouble("temp_max") - 273.15) + 42
                    val humidity = main.getInt("humidity")
                    relativeHumidity = humidity.toDouble()
                    ambientTemperature = tempMax


                    //Sunrise and Sunset
                    val sys = jsonObj.getJSONObject("sys")
                    val timezoneOffsetSeconds = jsonObj.getLong("timezone")
                    val timezoneOffsetMillis = timezoneOffsetSeconds * 1000L
                    val sunriseTimestamp = sys.getLong("sunrise") * 1000 // Unix timestamp in milliseconds
                    val sunsetTimestamp = sys.getLong("sunset") * 1000 // Unix timestamp in milliseconds
                    val localSunriseTime = sunriseTimestamp + timezoneOffsetMillis
                    val localSunsetTime = sunsetTimestamp + timezoneOffsetMillis
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val formattedSunrise = dateFormat.format(Date(localSunriseTime))
                    val formattedSunset = dateFormat.format(Date(localSunsetTime))

                    //for changing the background
                    val twentyFourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    twentyFourFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val twentyFourSunset = twentyFourFormat.format(Date(localSunsetTime))
                    val twentyFourSunrise = twentyFourFormat.format(Date(localSunriseTime))
                    val sunsetHour = twentyFourSunset.substring(0, 2).toInt()
                    val sunriseHour = twentyFourSunrise.substring(0, 2).toInt()

                    val calendar = Calendar.getInstance()
                    val localCurrentTime = calendar.timeInMillis + timezoneOffsetMillis
                    val twentyFourCurrent = twentyFourFormat.format(Date(localCurrentTime))
                    val currentHour = twentyFourCurrent.substring(0, 2).toInt()

                    //updating shared preferences for ThresholdWorker
                    val sharedPreferences = mainActivity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putFloat("temp", temp.toFloat()).apply()
                    sharedPreferences.edit().putFloat("humidity", humidity.toFloat()).apply()
                    sharedPreferences.edit().putFloat("wind", windSpeed.toFloat()).apply()
                    sharedPreferences.edit().putFloat("radiation", radiationFormulaHelper.getSolarRadiation(userLat, userDate).toFloat()).apply()
                    sharedPreferences

                    // City and Country Information
                    val country = sys.getString("country")
                    userCountryCode = country

                    if(currentHour in sunriseHour..sunsetHour){
                        mainUIHelper = MainUIHelper(mainActivity)
                        isDay = true
                        mainUIHelper.nightAndDay(isDay)
                    }
                    else{
                        mainUIHelper = MainUIHelper(mainActivity)
                        isDay = false
                        mainUIHelper.nightAndDay(isDay)
                    }

                    if(initial){
                        if(jsonObj.has("name")){
                            userCity = jsonObj.getString("name")
                            mainUIHelper = MainUIHelper(mainActivity)
                            mainUIHelper.updateWeatherUI(mainActivity, rainVolume, snowVolume, rainInt, cloudCoverageString, userCountryCode, userCity, userState, temp, tempMin, tempMax, windSpeed, updatedAt, humidity, cloudCoverage, formattedSunrise, formattedSunset)
                        }
                        else{
                            userCity = city
                            mainUIHelper = MainUIHelper(mainActivity)
                            mainUIHelper.updateWeatherUI(mainActivity,rainVolume, snowVolume, rainInt, cloudCoverageString, userCountryCode, city, userState, temp, tempMin, tempMax, windSpeed, updatedAt, humidity, cloudCoverage, formattedSunrise, formattedSunset)
                        }
                    }
                    else{
                        userCity = city
                        mainUIHelper = MainUIHelper(mainActivity)
                        mainUIHelper.updateWeatherUI(mainActivity, rainVolume, snowVolume, rainInt, cloudCoverageString, userCountryCode, city, userState, temp, tempMin, tempMax, windSpeed, updatedAt, humidity, cloudCoverage, formattedSunrise, formattedSunset)
                    }
                    notifyWeatherUpdated(isDay, userCity)
                    userLat = lat
                    userLong = long
                    comprehensiveClimateIndex(breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(mainActivity.applicationContext, error.toString().trim(), Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
            // Override methods if necessary
        }
        //queues the api response in the response queue
        val requestQueue = Volley.newRequestQueue(mainActivity.applicationContext)
        requestQueue.add(weatherMapRequest)
        return userState
    }

    /**
     * Calculates the comprehensive climate index (CCI) based on various factors including temperature, humidity, and radiation.
     *
     * @param bA Breed adjustment factor.
     * @param cA Color adjustment factor.
     * @param aA Acclimation adjustment factor.
     * @param hA Health adjustment factor.
     * @param sA Shade adjustment factor.
     * @param fA Feed adjustment factor.
     * @param mA Manure adjustment factor.
     * @param wA Water adjustment factor.
     */
    private fun comprehensiveClimateIndex(bA: Int, cA: Int, aA: Int, hA: Int, sA: Int, fA: Int, mA: Int, wA: Int){
        //calculates the cci
        val solarRadiation = radiationFormulaHelper.getSolarRadiation(userLat, userDate)
        var cci = 0.0

        if(isDay){
            cci = radiationFormulaHelper.calculateCCI(ambientTemperature, relativeHumidity, windSpeed, solarRadiation)
        }
        else{
            cci = 0.0
        }
        val adjustedCci = cci  + bA + cA + aA + hA + sA + fA + mA + wA
        Log.d("CCI", "$ambientTemperature, $relativeHumidity, $windSpeed, ${radiationFormulaHelper.getSolarRadiation(userLat, userDate)}, $cci")
        //calculates the level of threat based on the value returned by the cci
        if(adjustedCci <= 65.0){
            mainActivity.findViewById<TextView>(R.id.threat_level_textView).text = "low"
        }
        else if(adjustedCci in 65.001..86.0){
            mainActivity.findViewById<TextView>(R.id.threat_level_textView).text = "medium"
        }
        else{
            mainActivity.findViewById<TextView>(R.id.threat_level_textView).text = "high"
        }
    }
}