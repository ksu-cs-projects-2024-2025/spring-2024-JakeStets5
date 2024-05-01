package jakestets5.ksu.heatstressapp.helpers.ui

import android.content.Context
import android.graphics.Color
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import jakestets5.ksu.heatstressapp.activities.MainActivity
import jakestets5.ksu.heatstressapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * A helper class to manage UI updates for the MainActivity based on weather data and time of day conditions.
 *
 * @param mainActivity An instance of MainActivity to which UI updates are applied.
 */
class MainUIHelper (private val mainActivity: MainActivity){

    /**
     * Updates the UI elements of the MainActivity to reflect either day or night mode.
     * This includes changing backgrounds, button styles, and text colors.
     *
     * @param isDay A boolean value indicating whether it is currently day (true) or night (false).
     */
    fun nightAndDay(isDay: Boolean){
        // Assign backgrounds and border styles depending on the time of day
        val dayBackground = ContextCompat.getDrawable(mainActivity, R.drawable.background_day)
        val nightBackground = ContextCompat.getDrawable(mainActivity, R.drawable.background_night)
        val blackBorder = ContextCompat.getDrawable(mainActivity, R.drawable.info_border_black)
        val whiteBorder = ContextCompat.getDrawable(mainActivity, R.drawable.info_border_white)
        val borderColor = if(isDay) blackBorder else whiteBorder
        val textColor = if (isDay) Color.parseColor("#000000") else Color.parseColor("#FFFFFF")
        // Set background and text according to the time of day
        if(isDay) {
            mainActivity.findViewById<ConstraintLayout>(R.id.main_view).background = dayBackground
            mainActivity.findViewById<Button>(R.id.forecast_btn).background = ContextCompat.getDrawable(mainActivity,
                R.drawable.info_border_black
            )
        }
        else{
            mainActivity.findViewById<ConstraintLayout>(R.id.main_view).background = nightBackground
            mainActivity.findViewById<Button>(R.id.forecast_btn).background = ContextCompat.getDrawable(mainActivity,
                R.drawable.info_border_white
            )
        }

        // Applying the text color and filter adjustments to buttons and icons.
        mainActivity.findViewById<ImageButton>(R.id.saved_locations_btn).setColorFilter(textColor)
        mainActivity.findViewById<ImageButton>(R.id.settings_btn).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.address_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.updated_at).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.temp_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.low_temp_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.high_temp_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.threat_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.threat_level_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.today_textView).setTextColor(textColor)
        mainActivity.findViewById<Button>(R.id.forecast_btn).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.sunrise_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.sunrise_image).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.sunrise_title_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.sunset_image).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.sunset_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.sunset_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.humidity_image).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.humidity_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.humidity_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.wind_image).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.wind_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.wind_speed_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.precipitation_imageView).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.precipitation_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.precipitation_textView).setTextColor(textColor)
        mainActivity.findViewById<ImageView>(R.id.cloud_coverage_imageView).setColorFilter(textColor)
        mainActivity.findViewById<TextView>(R.id.cloud_coverage_title_textView).setTextColor(textColor)
        mainActivity.findViewById<TextView>(R.id.cloud_coverage_textView).setTextColor(textColor)
        mainActivity.findViewById<LinearLayout>(R.id.main_sunrise_layout).background = borderColor
        mainActivity.findViewById<LinearLayout>(R.id.main_sunset_layout).background = borderColor
        mainActivity.findViewById<LinearLayout>(R.id.main_humidity_layout).background = borderColor
        mainActivity.findViewById<LinearLayout>(R.id.main_wind_layout).background = borderColor
        mainActivity.findViewById<LinearLayout>(R.id.main_precipitation_layout).background = borderColor
        mainActivity.findViewById<LinearLayout>(R.id.main_clouds_layout).background = borderColor
    }

    fun updateWeatherUI(mainActivity: MainActivity, rainVolume: Double, snowVolume: Double, rainInt: Int, cloudCoverageString: String, userCountryCode: String, userCity: String, userState: String,
                             temp: Double, tempMin: Double, tempMax: Double, windSpeed: Double, updatedAt: Long, humidity: Int, cloudCoverage: Int, formattedSunrise: String, formattedSunset: String){

        val precipitationView: ImageView =
            mainActivity.findViewById(R.id.precipitation_imageView)
        if (rainVolume > 0.0 && snowVolume == 0.0) {
            mainActivity.findViewById<TextView>(R.id.precipitation_textView).text =
                "Rain: $rainInt mm"
        } else if (snowVolume > 0.0) {
            mainActivity.findViewById<TextView>(R.id.precipitation_textView).text =
                "Snow: $snowVolume mm"
            precipitationView.setImageResource(R.drawable.snowing_icon)
        } else {
            mainActivity.findViewById<TextView>(R.id.precipitation_textView).text =
                "Rain: $rainInt mm"
        }

        val cloudImageView: ImageView = mainActivity.findViewById(R.id.cloud_coverage_imageView)
        when (cloudCoverageString) {
            "Clear skies" -> cloudImageView.setImageResource(R.drawable.sunny_icon)
            "Partly cloudy" -> cloudImageView.setImageResource(R.drawable.partly_cloudy_icon)
            "Mostly cloudy" -> cloudImageView.setImageResource(R.drawable.partly_cloudy_icon)
            "Cloudy" -> cloudImageView.setImageResource(R.drawable.partly_cloudy_icon)
            "Overcast" -> cloudImageView.setImageResource(R.drawable.cloudy_icon)
        }

        if(userCountryCode == "US"){
            mainActivity.findViewById<TextView>(R.id.address_textView).text = "$userCity, $userState, $userCountryCode"
        }
        else{
            mainActivity.findViewById<TextView>(R.id.address_textView).text = "$userCity, $userCountryCode"
        }

        mainActivity.findViewById<TextView>(R.id.updated_at).text = "Updated at: " + SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(
            Date(updatedAt * 1000)
        )
        mainActivity.findViewById<TextView>(R.id.temp_textView).text = "${temp.roundToInt()}°F"
        mainActivity.findViewById<TextView>(R.id.low_temp_textView).text = "Low Temp: ${tempMin.roundToInt()}°F"
        mainActivity.findViewById<TextView>(R.id.high_temp_textView).text = "High Temp: ${tempMax.roundToInt()}°F"
        mainActivity.findViewById<TextView>(R.id.wind_speed_textView).text = "${windSpeed.roundToInt()} mph"
        mainActivity.findViewById<TextView>(R.id.humidity_textView).text = "$humidity%"
        mainActivity.findViewById<TextView>(R.id.cloud_coverage_textView).text = cloudCoverage.toString() + "%"
        mainActivity.findViewById<TextView>(R.id.sunrise_textView).text = formattedSunrise
        mainActivity.findViewById<TextView>(R.id.sunset_textView).text = formattedSunset
    }
}