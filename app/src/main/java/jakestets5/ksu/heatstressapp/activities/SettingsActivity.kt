package jakestets5.ksu.heatstressapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.helpers.database.SettingsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.ui.SettingsUIHelper

/**
 * Activity for adjusting and managing user preferences and settings related to the comprehensive comfort index (CCI).
 */
class SettingsActivity : AppCompatActivity() {
    // Variables to store user settings and location details
    private var userCity: String = "New York City"
    private var userCountry: String = "United States"
    private var userCountryCode: String = "US"
    private var userLat = 40.7128
    private var userLong = -74.0060
    private var userState: String = "New York"
    private var isDay = true

    // Variables for adjusting the CCI based on various environmental factors
    private var breedAdjustment = 0
    private var colorAdjustment = 0
    private var acclimationAdjustment = 5
    private var healthAdjustment = 5
    private var shadeAdjustment = 0
    private var feedAdjustment = 2
    private var manureAdjustment = 8
    private var waterAdjustment = 2

    private lateinit var settingsdbHelper: SettingsDatabaseHelper

    /**
     * Initializes the activity, sets up UI components, and prepares data handlers.
     *
     * @param savedInstanceState Contains data supplied in onSaveInstanceState(Bundle) or null if no data was supplied.
     */
    @SuppressLint("MissingInflatedId") //for the back btn. Says missing inflation but works like any of the other activities
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Extract data passed from another activity
        val inBundle = intent.extras
        if (inBundle != null){
            userCity = "${inBundle.getString("city")}"
            userCountry = "${inBundle.getString("country")}"
            userLong = "${inBundle.getString("longitude")}".toDouble()
            userLat = "${inBundle.getString("latitude")}".toDouble()
            userCountryCode = "${inBundle.getString("countryCode")}"
            userState = "${inBundle.getString("state")}"
            isDay = inBundle.getBoolean("dayStatus")
        }

        // Configure spinners for user settings with appropriate day/night themes
        val sharedPrefs = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        val spinnerLayout = if(isDay) R.layout.day_spinner_item else R.layout.night_spinner_item
        val spinnerPopup = if(isDay) R.layout.day_spinner_dropdown else R.layout.night_spinner_dropdown

        // Setup UI helper to adjust the theme based on the time of day
        val settingsUIHelper = SettingsUIHelper(this)
        settingsUIHelper.nightAndDay(isDay)

        // Setup back button to return to MainActivity with the current settings
        setupBackButton()

        // Initialize spinners with values and set listeners
        setupSpinners(sharedPrefs, spinnerLayout, spinnerPopup)
    }

    /**
     * Initializes and sets up the spinners for various settings like breed, water, manure, etc.,
     * with custom layouts and listeners to handle selection events.
     */
    private fun setupSpinners(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        setupBreedSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupWaterSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupManureSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupFeedSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupShadeSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupAcclimationSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupHealthSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
        setupColorSpinner(sharedPrefs, spinnerLayout, spinnerPopup)
    }

    /**
     * Configures the breed spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupBreedSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val breedSpinner: Spinner = findViewById(R.id.breed_spinner)
        val breedAdapter = ArrayAdapter(this, spinnerLayout, listOf("Bos taurus (British)", "Bos taurus (European)", "Bos indicus (25%)", "Bos indicus (50%)", "Bos indicus (75%)", "Bos indicus (100%)", "Waygu"))
        breedAdapter.setDropDownViewResource(spinnerPopup)
        breedSpinner.adapter = breedAdapter

        val selectedBreed = sharedPrefs.getString("SelectedBreed", "Bos taurus (British)")
        breedSpinner.setSelection(breedAdapter.getPosition(selectedBreed))
        breedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedBreed", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("breed", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the water spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupWaterSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val waterSpinner: Spinner = findViewById(R.id.water_spinner)
        val waterAdapter = ArrayAdapter(this, spinnerLayout, listOf(">35°C", "15 to 20°C", "21 to 30°C", "31 to 35°C"))
        waterAdapter.setDropDownViewResource(spinnerPopup)
        waterSpinner.adapter = waterAdapter

        val selectedWater = sharedPrefs.getString("SelectedWater", ">35°C")
        waterSpinner.setSelection(waterAdapter.getPosition(selectedWater))
        waterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedWater", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("water", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the manure spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupManureSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val manureSpinner: Spinner = findViewById(R.id.manure_spinner)
        val manureAdapter = ArrayAdapter(this, spinnerLayout, listOf("Depth of manure pack: 200mm", "Depth of manure pack: 50mm", "Depth of manure pack: 100mm"))
        manureAdapter.setDropDownViewResource(spinnerPopup)
        manureSpinner.adapter = manureAdapter

        val selectedManure = sharedPrefs.getString("SelectedManure", "Depth of manure pack: 200mm")
        manureSpinner.setSelection(manureAdapter.getPosition(selectedManure))
        manureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedManure", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("manure", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the feed spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupFeedSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val feedSpinner: Spinner = findViewById(R.id.feed_spinner)
        val feedAdapter = ArrayAdapter(this, spinnerLayout, listOf(">130", "0 to 80", "80 to 130"))
        feedAdapter.setDropDownViewResource(spinnerPopup)
        feedSpinner.adapter = feedAdapter

        val selectedFeed = sharedPrefs.getString("SelectedFeed", ">130")
        feedSpinner.setSelection(feedAdapter.getPosition(selectedFeed))
        feedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedFeed", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("feed", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the shade spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupShadeSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val shadeSpinner: Spinner = findViewById(R.id.shade_spinner)
        val shadeAdapter = ArrayAdapter(this, spinnerLayout, listOf("No shade", "1.5 to 2m\u00B2", "2 to 3m\u00B2", ">3m\u00B2"))
        shadeAdapter.setDropDownViewResource(spinnerPopup)
        shadeSpinner.adapter = shadeAdapter

        val selectedShade = sharedPrefs.getString("SelectedShade", "No shade")
        shadeSpinner.setSelection(shadeAdapter.getPosition(selectedShade))
        shadeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedShade", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("shade", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the acclimation spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupAcclimationSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val acclimationSpinner: Spinner = findViewById(R.id.acclimation_spinner)
        val acclimationAdapter = ArrayAdapter(this, spinnerLayout, listOf("Not acclimated", "Acclimated"))
        acclimationAdapter.setDropDownViewResource(spinnerPopup)
        acclimationSpinner.adapter = acclimationAdapter

        val selectedAcclimation = sharedPrefs.getString("SelectedAcclimation", "Not acclimated")
        acclimationSpinner.setSelection(acclimationAdapter.getPosition(selectedAcclimation))
        acclimationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedAcclimation", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("acclimation", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the health spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupHealthSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val healthSpinner: Spinner = findViewById(R.id.health_spinner)
        val healthAdapter = ArrayAdapter(this, spinnerLayout, listOf("Showing signs of, or recovering from disease", "Healthy"))
        healthAdapter.setDropDownViewResource(spinnerPopup)
        healthSpinner.adapter = healthAdapter

        val selectedHealth = sharedPrefs.getString("SelectedHealth", "Showing signs of, or recovering from disease")
        healthSpinner.setSelection(healthAdapter.getPosition(selectedHealth))
        healthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedHealth", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("health", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Configures the color spinner with available options, default selections, and handling for user selection events.
     */
    private fun setupColorSpinner(sharedPrefs: SharedPreferences, spinnerLayout: Int, spinnerPopup: Int) {
        val colorSpinner: Spinner = findViewById(R.id.color_spinner)
        val colorAdapter = ArrayAdapter(this, spinnerLayout, listOf("Black", "White", "Red"))
        colorAdapter.setDropDownViewResource(spinnerPopup)
        colorSpinner.adapter = colorAdapter

        val selectedColor = sharedPrefs.getString("SelectedColor", "Black")
        colorSpinner.setSelection(colorAdapter.getPosition(selectedColor))
        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPrefs.edit().putString("SelectedColor", parent.getItemAtPosition(position).toString()).apply()
                updateAdjustment("color", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Updates the respective adjustment setting based on the user's selection from a spinner.
     */
    private fun updateAdjustment(settingType: String, selectedValue: String) {
        when (settingType) {
            "breed" -> breedAdjustment = when (selectedValue) {
                "Bos taurus (British)" -> 0
                "Bos taurus (European)" -> -3
                "Bos indicus (25%)" -> -4
                "Bos indicus (50%)" -> -7
                "Bos indicus (75%)" -> -8
                "Bos indicus (100%)" -> -10
                "Waygu" -> -4
                else -> breedAdjustment
            }
            "water" -> waterAdjustment = when (selectedValue) {
                ">35°C" -> 2
                "31 to 35°C" -> 1
                "21 to 30°C" -> 0
                "15 to 20°C" -> -1
                else -> waterAdjustment
            }
            "manure" -> manureAdjustment = when (selectedValue) {
                "Depth of manure pack: 200mm" -> 8
                "Depth of manure pack: 100mm" -> 4
                "Depth of manure pack: 50mm" -> 0
                else -> manureAdjustment
            }
            "feed" -> feedAdjustment = when (selectedValue) {
                ">130" -> 3
                "80 to 130" -> 0
                "0 to 80" -> -2
                else -> feedAdjustment
            }
            "shade" -> shadeAdjustment = when (selectedValue) {
                ">3m\u00B2" -> -7
                "2 to 3m\u00B2" -> -5
                "1.5 to 2m\u00B2" -> -3
                "No shade" -> 0
                else -> shadeAdjustment
            }
            "acclimation" -> acclimationAdjustment = when (selectedValue) {
                "Acclimated" -> 0
                "Not acclimated" -> 5
                else -> acclimationAdjustment
            }
            "health" -> healthAdjustment = when (selectedValue) {
                "Healthy" -> 0
                "Showing signs of, or recovering from disease" -> 5
                else -> healthAdjustment
            }
            "color" -> colorAdjustment = when (selectedValue) {
                "Black" -> 0
                "White" -> -3
                "Red" -> -1
                else -> colorAdjustment
            }
        }
        updateSettings()  // Persist changes to the database
    }

    /**
     * Sets up the back button with a listener that saves the current settings and navigates back to the MainActivity.
     */
    private fun setupBackButton() {
        val outBundle = Bundle().apply {
            putString("latitude", userLat.toString())
            putString("longitude", userLong.toString())
            putString("city", userCity)
            putString("countryCode", userCountryCode)
            putString("state", userState)
            putBoolean("dayStatus", isDay)
        }
        findViewById<ImageButton>(R.id.settings_back_btn).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtras(outBundle)
            startActivity(intent)
        }
    }

    /**
     * Updates settings in the database based on current adjustments.
     */
    private fun updateSettings() {
        settingsdbHelper = SettingsDatabaseHelper(this)
        settingsdbHelper.clearSettings()
        settingsdbHelper.addSettings(breedAdjustment, colorAdjustment, acclimationAdjustment, healthAdjustment, shadeAdjustment, feedAdjustment, manureAdjustment, waterAdjustment)
    }
}