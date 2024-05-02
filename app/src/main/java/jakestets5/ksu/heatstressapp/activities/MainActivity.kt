package jakestets5.ksu.heatstressapp.activities

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.adapters.recycler.CitiesRecyclerAdapter
import jakestets5.ksu.heatstressapp.data.`object`.ApiData
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import jakestets5.ksu.heatstressapp.helpers.api.CurrentWeatherApiHelper
import jakestets5.ksu.heatstressapp.helpers.database.CitiesDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.database.SavedLocationsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.database.SettingsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.ui.MainUIHelper
import jakestets5.ksu.heatstressapp.notifications.NotificationWorker
import java.util.concurrent.TimeUnit

/**
 * Main activity class for handling location-based weather updates, user interface interactions,
 * and navigation to other activities such as forecast and settings.
 */
class MainActivity : ComponentActivity(), View.OnClickListener, CurrentWeatherApiHelper.WeatherUpdateListener {

    // Variables for storing user location and weather data
    var userCity: String = "New York City"
    var userState: String = "New York"
    var userCountryCode: String = "US"
    var userCountry: String = "United States"
    var userLat = 40.7128
    var userLong = -74.0060
    var cloudCoverageGlobal: String = "clear_skies"
    var ambientTemperature: Double = 0.0
    var relativeHumidity: Double = 0.0
    var windSpeed: Double = 0.0
    var solarRadiation: Double = 0.0
    var initialCity: Boolean = true
    var dayStatus: Boolean = true
    var firstSubmission = true

    // Variables for adjustments based on user or animal specifics, affecting how weather data is interpreted
    private var breedAdjustment = 0
    private var colorAdjustment = 0
    private var acclimationAdjustment = 5
    private var healthAdjustment = 5
    private var shadeAdjustment = 0
    private var feedAdjustment = 2
    private var manureAdjustment = 8
    private var waterAdjustment = 2

    // List of locations for managing multiple city data
    private var locationList = ArrayList<LocationData>()

    // Database helpers for managing local data storage
    private lateinit var citiesdbHelper: CitiesDatabaseHelper
    private lateinit var savedLocationsdbHelper: SavedLocationsDatabaseHelper
    private lateinit var settingsdbHelper: SettingsDatabaseHelper

    // UI and API helpers for managing weather data and user interface updates
    private lateinit var mainUIHelper: MainUIHelper
    private lateinit var currentWeatherApiHelper: CurrentWeatherApiHelper

    // Client for managing location updates
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Placeholder method to meet interface obligations; currently does nothing.
     */
    @SuppressLint("MissingPermission")
    override fun onClick(v: View?) {
        // Intentionally left blank
    }

//REGION: location and notification permissions
    //sets the variable for location permissions
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            //If permissions are granted
            getLocation()
            requestNotificationPermission()
        } else {
            //If permissions are denied
            Toast.makeText(this, "Denying location permission prevents access to current location", Toast.LENGTH_LONG).show()
        }
    }

    //Sets the variable for notification permissions
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    //requests location permission from the user
    private fun requestLocationPermissions() {
        val requiredLocationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val shouldRequestLocationPermission = requiredLocationPermissions.any {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (shouldRequestLocationPermission) {
            locationPermissionLauncher.launch(requiredLocationPermissions)
        } else {
            getLocation()
            requestNotificationPermission()
        }
    }

    //requests notification permission from the user
    private fun requestNotificationPermission() {
        // Check and request notification permissions (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    //checks if location permission is granted
    private fun areLocationPermissionsGranted(): Boolean {
        // Check both fine and coarse location permissions
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
//END REGION

    /**
     * Sets up initial configurations and retrieves user settings from the database.
     */
    @SuppressLint("MissingPermission") // Suppressed because permission request is handled in this method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //updating the adjustments for the cci formula
        settingsdbHelper = SettingsDatabaseHelper(this)
        updateSettings()

        // Initializing the WeatherApiHelper
        currentWeatherApiHelper = CurrentWeatherApiHelper(this, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
        currentWeatherApiHelper.setWeatherUpdateListener(this)

        // Check if location permissions are already granted and if so, get location
        if (areLocationPermissionsGranted()) {
            getLocation()
        }

        //gets the user's location
        if(savedInstanceState == null){
            requestLocationPermissions()
            if(initialCity){

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                })
                    .addOnSuccessListener { location: Location? ->
                        if (location == null)
                            Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Retrieve any initial settings from intent
        val inBundle = intent.extras
        if (inBundle != null){
            userCity = "${inBundle.getString("city")}"
            userCountry = "${inBundle.getString("country")}"
            userLong = "${inBundle.getString("longitude")}".toDouble()
            userLat = "${inBundle.getString("latitude")}".toDouble()
            userCountryCode = "${inBundle.getString("countryCode")}"
            userState = "${inBundle.getString("state")}"
            dayStatus = inBundle.getBoolean("dayStatus")
            initialCity = false
        }

//REGION: click listeners
        // Variables to set up recycler views
        val locationRecyclerView: RecyclerView = findViewById(R.id.location_recyclerView)
        locationRecyclerView.layoutManager = LinearLayoutManager(this)
        val citiesRecyclerAdapter = CitiesRecyclerAdapter(this, dayStatus, locationList)
        locationRecyclerView.adapter = citiesRecyclerAdapter

        //sets up the forecast button
        val forecastBtn = findViewById<Button>(R.id.forecast_btn)
        forecastBtn.setOnClickListener {
            navigateToActivity(ForecastActivity::class.java)
        }

        //sets up the saved locations button
        val locationsBtn = findViewById<ImageButton>(R.id.saved_locations_btn)
        locationsBtn.setOnClickListener{
            navigateToActivity(SavedLocationsActivity::class.java)
        }

        //sets up the settings button
        val settingsBtn = findViewById<ImageButton>(R.id.settings_btn)
        settingsBtn.setOnClickListener{
            navigateToActivity(SettingsActivity::class.java)
        }

        //set the save button click listener
        citiesRecyclerAdapter.setOnSaveClickListener(object: CitiesRecyclerAdapter.OnSaveClickListener{
            override fun onButtonClick(locationData: LocationData){
                //sets the values of the clicked city
                val cityInfo = citiesdbHelper.getExactCity(locationData.latitude, locationData.longitude, this@MainActivity)
                userCity = cityInfo.city
                userCountry = cityInfo.country
                userLat = cityInfo.latitude
                userLong = cityInfo.longitude
                userCountryCode = cityInfo.countryCode
                //updates the weather details based on the clicked city
                currentWeatherApiHelper = CurrentWeatherApiHelper(this@MainActivity, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
                currentWeatherApiHelper.setWeatherUpdateListener(this@MainActivity)
                val saveData: ApiData = currentWeatherApiHelper.saveRecyclerClick(initialCity, savedLocationsdbHelper, cityInfo.city, cityInfo.country, cityInfo.latitude, cityInfo.longitude, cityInfo.countryCode)
                userState = saveData.state
                dayStatus = saveData.isDay
                initialCity = false
                //UI maintenance
                hideKeyboard(currentFocus ?: View(this@MainActivity))
                locationRecyclerView.visibility = View.GONE
            }
        })

        // Set the recycler item click listener
        citiesRecyclerAdapter.setOnItemClickListener(object : CitiesRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(locationData: LocationData) {
                //set up in a similar fashion to the save click
                val query = citiesdbHelper.getExactCity(locationData.latitude, locationData.longitude, this@MainActivity)
                userCity = query.city
                userLat = query.latitude
                userLong = query.longitude
                userCountry = query.country
                userCountryCode = query.countryCode

                currentWeatherApiHelper = CurrentWeatherApiHelper(this@MainActivity, query.latitude, query.longitude, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
                currentWeatherApiHelper.setWeatherUpdateListener(this@MainActivity)
                userState = currentWeatherApiHelper.cityRecyclerClick(query.city, initialCity, query.latitude, query.longitude).state
                dayStatus = currentWeatherApiHelper.cityRecyclerClick(query.city, initialCity, query.latitude, query.longitude).isDay
                initialCity = false
                firstSubmission = true

                locationRecyclerView.visibility = View.GONE
                hideKeyboard(currentFocus ?: View(this@MainActivity))
            }
        })

        //setting up the location search view submission event
        val locationSearchView: SearchView = findViewById(R.id.location_searchView)
        locationSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard(currentFocus ?: View(this@MainActivity))
                query?.let {
                    if(query.length <= 1){
                        Toast.makeText(this@MainActivity, "Please enter a valid query", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        if(firstSubmission){
                            locationRecyclerView.visibility = View.VISIBLE
                            val recyclerCity = it
                            val searchResults = citiesdbHelper.searchCities(recyclerCity, this@MainActivity)
                            locationList.clear()
                            currentWeatherApiHelper = CurrentWeatherApiHelper(this@MainActivity, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
                            currentWeatherApiHelper.setWeatherUpdateListener(this@MainActivity)
                            currentWeatherApiHelper.updateCityRecycler(citiesRecyclerAdapter, searchResults)
                            initialCity = false
                        }
                    }
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
//END REGION
        //schedules the 24 hour notification
        if(initialCity){
            schedulePeriodicWork()
        }

        if(!initialCity){
            updateWeatherDetails(userLat, userLong)
        }
    }
//END ON CREATE

    /**
     * Navigates to a specified activity, passing current location and weather data.
     */
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            putExtras(Bundle().apply {
                putString("city", userCity)
                putString("country", userCountry)
                putString("latitude", userLat.toString())
                putString("longitude", userLong.toString())
                putString("countryCode", userCountryCode)
                putString("state", userState)
                putBoolean("dayStatus", dayStatus)
            })
        }
        startActivity(intent)
    }

    /**
     * Schedules a notification to be sent to the user every 24 hours.
     */
    private fun schedulePeriodicWork() {
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(notificationWorkRequest)
    }

    /**
     * Refreshes UI state when the activity resumes from paused state.
     */
    override fun onResume() {
        super.onResume()
        //setting up the initial home screen
        hideKeyboard(currentFocus ?: View(this@MainActivity))
        currentWeatherApiHelper = CurrentWeatherApiHelper(this@MainActivity, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
        currentWeatherApiHelper.setWeatherUpdateListener(this@MainActivity)
        if(!initialCity){
            userState = currentWeatherApiHelper.cityRecyclerClick(userCity, initialCity, userLat, userLong).state
            dayStatus = currentWeatherApiHelper.cityRecyclerClick(userCity, initialCity, userLat, userLong).isDay
        }
        citiesDatabaseSetup()
        savedLocationsDatabaseSetup()
        settingsDatabaseSetup()
        if(initialCity){
            schedulePeriodicWork()
        }
    }

    /**
     * Saves the current state of activity variables before the activity is potentially destroyed.
     * Used to save user settings, location data, and weather conditions.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("city", userCity)
        outState.putString("state", userState)
        outState.putString("countryCode", userCountryCode)
        outState.putString("country", userCountry)
        outState.putDouble("long", userLong)
        outState.putDouble("lat", userLat)
        outState.putString("cloud", cloudCoverageGlobal)
        outState.putDouble("temp", ambientTemperature)
        outState.putDouble("humidity", relativeHumidity)
        outState.putDouble("wind", windSpeed)
        outState.putDouble("solarRad", solarRadiation)
        outState.putBoolean("initialCity", false)
    }

    /**
     * Restores the saved state of the activity. This is typically called if the activity was previously destroyed
     * and is now being re-created, restoring all the crucial variables needed to maintain the user's session.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        userCity = savedInstanceState.getString("city").toString()
        userState = savedInstanceState.getString("state").toString()
        userCountry = savedInstanceState.getString("country").toString()
        userCountryCode = savedInstanceState.getString("countryCode").toString()
        userLong = savedInstanceState.getDouble("long")
        userLat = savedInstanceState.getDouble("lat")
        cloudCoverageGlobal = savedInstanceState.getString("cloud").toString()
        ambientTemperature = savedInstanceState.getDouble("temp")
        relativeHumidity = savedInstanceState.getDouble("humidity")
        windSpeed = savedInstanceState.getDouble("wind")
        solarRadiation = savedInstanceState.getDouble("solarRad")
        initialCity = savedInstanceState.getBoolean("initialCity")
        // Restore saved state
    }

    /**
     * Retrieves the current location of the device using the Fused Location Provider.
     * Requires location permissions to be granted. Used for updating user's location and fetching weather data.
     */
    @SuppressLint("MissingPermission") //error suppressed due to permission check being in onCreate
    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Constructing a new location request using the Builder
        val locationRequest = LocationRequest.Builder(10000L)
            .apply {
            }
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Use the location, update UI
                    if (initialCity) {
                        userLat = location.latitude
                        userLong = location.longitude
                        updateWeatherDetails(location.latitude, location.longitude)
                        break
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        hideKeyboard(currentFocus ?: View(this@MainActivity))
    }

    /**
     * Dismisses the keyboard when touching outside of the search view or other specified views.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val searchView = findViewById<SearchView>(R.id.location_searchView)
            val recyclerView = findViewById<RecyclerView>(R.id.location_recyclerView)
            if (!isTouchInsideView(ev, searchView) && !isTouchInsideView(ev, recyclerView)) {
                recyclerView.visibility = View.GONE
                hideKeyboard(currentFocus)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Checks if a touch event occurred inside the boundaries of a specified view.
     *
     * @param ev The MotionEvent object containing details about the touch event.
     * @param view The view to check if the touch is inside.
     * @return Returns true if the touch coordinates are within the view's boundaries, false otherwise.
     */
    private fun isTouchInsideView(ev: MotionEvent, view: View): Boolean {
        val outRect = Rect()
        view.getGlobalVisibleRect(outRect)
        return outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    /**
     * Sets up and initializes the database for managing city data.
     * This includes creating and opening the database.
     */
    private fun citiesDatabaseSetup(){
        citiesdbHelper = CitiesDatabaseHelper(this, userState)

        try {
            citiesdbHelper.createDataBase()
            if (citiesdbHelper.openDataBase()) {
                Log.d("CitiesDatabaseDebug", "Database opened successfully")
            } else {
                Log.d("CitiesDatabaseDebug", "Failed to open database")
            }
        } catch (e: Exception) {
            Log.e("CitiesDatabaseDebug", "Error creating database", e)
        }
    }

    /**
     * Sets up and initializes the settings database.
     * This process involves creating and opening the database, which stores various user settings.
     */
    private fun settingsDatabaseSetup(){
        settingsdbHelper = SettingsDatabaseHelper(this)

        try {
            settingsdbHelper.createDataBase()
            if (settingsdbHelper.openDataBase()) {
                Log.d("SettingsDatabaseDebug", "Database opened successfully")
            } else {
                Log.d("SettingsDatabaseDebug", "Failed to open database")
            }
        } catch (e: Exception) {
            Log.e("SettingsDatabaseDebug", "Error creating database", e)
        }
    }

    /**
     * Sets up and initializes the database for managing saved locations.
     * This includes creating and opening the database, allowing the app to store and retrieve user-specified locations.
     */
    private fun savedLocationsDatabaseSetup(){
        savedLocationsdbHelper = SavedLocationsDatabaseHelper(this)

        try {
            savedLocationsdbHelper.createDataBase()
            if (savedLocationsdbHelper.openDataBase()) {
                Log.d("SavedLocationsDatabaseDebug", "Database opened successfully")
            } else {
                Log.d("SavedLocationsDatabaseDebug", "Failed to open database")
            }
        } catch (e: Exception) {
            Log.e("SavedLocationsDatabaseDebug", "Error creating database", e)
        }
    }

    /**
     * Hides the soft keyboard from the screen. This function is commonly used to improve user experience
     * by removing the keyboard when it is no longer necessary.
     *
     * @param view The current focus view from which to hide the keyboard, or null if there is no focus.
     */
    fun hideKeyboard(view: View?) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    /**
     * Updates all relevant weather details based on the user's current latitude and longitude.
     *
     * @param lat The latitude for which to fetch and update the weather details.
     * @param long The longitude for which to fetch and update the weather details.
     */
    private fun updateWeatherDetails(lat: Double, long: Double){
        userLat = lat
        userLong = long
        firstSubmission = true
        citiesdbHelper = CitiesDatabaseHelper(this, userState)
        userCity = citiesdbHelper.getExactCity(lat, long, this@MainActivity).city
        userState = currentWeatherApiHelper.cityRecyclerClick(userCity, false, lat, long).state
        dayStatus = currentWeatherApiHelper.cityRecyclerClick(userCity, false, lat, long).isDay

        currentWeatherApiHelper = CurrentWeatherApiHelper(this, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
        currentWeatherApiHelper = CurrentWeatherApiHelper(this@MainActivity, userLat, userLong, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
        currentWeatherApiHelper.setWeatherUpdateListener(this@MainActivity)
        currentWeatherApiHelper.getWeatherDetails(userCity, false, lat, long)
    }

    /**
     * Retrieves and applies updated settings from the database.
     * These settings are used to adjust the CCI.
     */
    private fun updateSettings(){
        val settings = settingsdbHelper.getSettings()
        breedAdjustment = settings.breed
        colorAdjustment = settings.color
        acclimationAdjustment = settings.acclimation
        healthAdjustment = settings.health
        shadeAdjustment = settings.shade
        feedAdjustment = settings.feed
        manureAdjustment = settings.manure
        waterAdjustment = settings.water
    }

    /**
     * Responds to updates in weather data, updating the UI to reflect the new day status and city information.
     *
     * @param isDay A boolean indicating whether it is currently day or night.
     * @param city The city for which the weather update is applicable.
     */
    override fun onWeatherDataUpdated(isDay: Boolean, city: String) {
        mainUIHelper = MainUIHelper(this)
        dayStatus = isDay
        userCity = city
        mainUIHelper.nightAndDay(isDay)
    }
}