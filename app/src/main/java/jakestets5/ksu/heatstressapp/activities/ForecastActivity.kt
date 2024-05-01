package jakestets5.ksu.heatstressapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.adapters.recycler.ForecastRecyclerAdapter
import jakestets5.ksu.heatstressapp.helpers.api.ForecastApiHelper
import jakestets5.ksu.heatstressapp.helpers.ui.ForecastUIHelper

/**
 * Activity for displaying weather forecasts. Implements the ForecastApiHelper.ForecastListener interface
 * to handle updates to forecast data.
 */
class ForecastActivity : AppCompatActivity(), ForecastApiHelper.ForecastListener {

    // Variables to store user's location and environmental settings
    private var userCity: String = "New York"
    private var userState: String = "New York"
    private var userCountry: String = "United States"
    private var userCountryCode: String = "US"
    private var userLat = 40.7128
    private var userLong = -74.0060
    var isDay: Boolean = true

    // Lists to store various weather parameters
    private var precipitationList = mutableListOf<String>()
    private var humidityList = mutableListOf<String>()
    private var windSpeedList = mutableListOf<String>()
    private var threatLevelList = mutableListOf<String>()
    private var lowTempList = mutableListOf<String>()
    private var highTempList = mutableListOf<String>()
    private var dayOfWeekList = mutableListOf<String>()

    // API helper for fetching forecast data
    private lateinit var forecastApiHelper: ForecastApiHelper

    // UI helper for updating UI elements
    private lateinit var forecastUIHelper: ForecastUIHelper

    // Adapter for RecyclerView to display forecast information
    private lateinit var forecastAdapter: ForecastRecyclerAdapter

    // Variables to adjust the Comprehensive Climate Index (CCI) based on specific factors
    private var breedAdjustment = 0
    private var colorAdjustment = 0
    private var acclimationAdjustment = 5
    private var healthAdjustment = 5
    private var shadeAdjustment = 0
    private var feedAdjustment = 2
    private var manureAdjustment = 8
    private var waterAdjustment = 2

    /**
     * Called when the activity is starting. Sets up the user interface and initializes components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        // Extract data passed from previous activity
        val inBundle = intent.extras
        if (inBundle != null) {
            userCity = "${inBundle.getString("city")}"
            userCountry = "${inBundle.getString("country")}"
            userLong = "${inBundle.getString("longitude")}".toDouble()
            userLat = "${inBundle.getString("latitude")}".toDouble()
            userCountryCode = "${inBundle.getString("countryCode")}"
            userState = "${inBundle.getString("state")}"
            isDay = inBundle.getBoolean("dayStatus")
        }

        // Setup the RecyclerView for displaying forecast data
        val forecastRecyclerView: RecyclerView = findViewById(R.id.forecast_recyclerView)
        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        forecastAdapter = ForecastRecyclerAdapter(
            this,
            isDay,
            dayOfWeekList,
            precipitationList,
            threatLevelList,
            humidityList,
            windSpeedList,
            highTempList,
            lowTempList
        )
        forecastRecyclerView.adapter = forecastAdapter

        // Initialize UI helper and update UI based on time of day
        forecastUIHelper = ForecastUIHelper(this)
        forecastUIHelper.nightAndDay(isDay)

        // Generate initial placeholder data for the list
        postToList()

        // Setup and initiate fetching forecast data via API
        forecastApiHelper = ForecastApiHelper(this, breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
        forecastApiHelper.setForecastUpdateListener(this@ForecastActivity)
        forecastApiHelper.getForecastApiDetails(userLat, userLong)

        //listens for updates in forecasted values
        forecastApiHelper.setForecastUpdateListener(object : ForecastApiHelper.ForecastListener {
            override fun onDataFetched(
                dayOfWeekFetched: MutableList<String>,
                precipitationListFetched: MutableList<String>,
                humidityListFetched: MutableList<String>,
                windSpeedListFetched: MutableList<String>,
                threatListFetched: MutableList<String>,
                lowTempListFetched: MutableList<String>,
                highTempListFetched: MutableList<String>
            ) {
                // Updates the lists
                dayOfWeekList.clear()
                dayOfWeekList.addAll(dayOfWeekFetched)

                precipitationList.clear()
                precipitationList.addAll(precipitationListFetched)

                humidityList.clear()
                humidityList.addAll(humidityListFetched)

                windSpeedList.clear()
                windSpeedList.addAll(windSpeedListFetched)

                threatLevelList.clear()
                threatLevelList.addAll(threatListFetched)

                lowTempList.clear()
                lowTempList.addAll(lowTempListFetched)

                highTempList.clear()
                highTempList.addAll(highTempListFetched)

                // Notify adapter of data change on the UI thread
                runOnUiThread {
                    forecastAdapter.notifyDataSetChanged()
                }
            }
        })

        // Set up the back button
        val outBundle = Bundle()
        outBundle.putString("latitude", userLat.toString())
        outBundle.putString("longitude", userLong.toString())
        outBundle.putString("city", userCity)
        outBundle.putString("countryCode", userCountryCode)
        outBundle.putString("state", userState)
        outBundle.putString("initialCity", "2")
        val backBtn = findViewById<ImageButton>(R.id.forecast_back_btn)
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtras(outBundle)
            startActivity(intent)
        }
    }

    /**
     * Adds initial placeholder data to forecast lists for demonstration purposes.
     */
    private fun postToList(){
        for(i in 1..7){
            addToList("i","$i%", "$i%", "$i"+"mph", "$i", "$i°C", "$i°C")
        }
    }

    /**
     * Adds a single day's forecast data to the lists.
     *
     * @param dayOfWeek Day of the week.
     * @param precipitation Precipitation percentage.
     * @param humidity Humidity percentage.
     * @param windSpeed Wind speed in mph.
     * @param threatLevel Threat level description.
     * @param lowTemp Low temperature.
     * @param highTemp High temperature.
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
     * Refreshes UI state when the activity resumes from paused state.
     */
    override fun onResume() {
        super.onResume()
        forecastUIHelper.nightAndDay(isDay)
    }

    /**
     * Callback for when forecast data is fetched successfully.
     *
     * @param dayOfWeekFetched List of day names fetched.
     * @param precipitationListFetched List of precipitation data fetched.
     * @param humidityListFetched List of humidity data fetched.
     * @param windSpeedListFetched List of wind speed data fetched.
     * @param threatListFetched List of threat level data fetched.
     * @param lowTempListFetched List of low temperature data fetched.
     * @param highTempListFetched List of high temperature data fetched.
     */
    override fun onDataFetched(
        dayOfWeekFetched: MutableList<String>,
        precipitationListFetched: MutableList<String>,
        humidityListFetched: MutableList<String>,
        windSpeedListFetched: MutableList<String>,
        threatListFetched: MutableList<String>,
        lowTempListFetched: MutableList<String>,
        highTempListFetched: MutableList<String>
    ) {
        // Update your lists
        dayOfWeekList.clear()
        dayOfWeekList.addAll(dayOfWeekFetched)

        precipitationList.clear()
        precipitationList.addAll(precipitationListFetched)

        humidityList.clear()
        humidityList.addAll(humidityListFetched)

        windSpeedList.clear()
        windSpeedList.addAll(windSpeedListFetched)

        threatLevelList.clear()
        threatLevelList.addAll(threatListFetched)

        lowTempList.clear()
        lowTempList.addAll(lowTempListFetched)

        highTempList.clear()
        highTempList.addAll(highTempListFetched)

        // Notify adapter of data change on the UI thread
        runOnUiThread {
            forecastAdapter.notifyDataSetChanged()
        }
    }
}
