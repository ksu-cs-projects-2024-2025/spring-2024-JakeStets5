package jakestets5.ksu.heatstressapp.helpers.ui

import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.SettingsActivity
/**
 * A utility class to assist in updating the user interface of the ForecastActivity based on the time of day.
 *
 * @param settings An instance of SettingsActivity, the context in which the UI updates are applied.
 */
class SettingsUIHelper(private val settings: SettingsActivity) {
    /**
     * Adjusts the user interface of the SettingsActivity based on the time of day. This includes changing background
     * images and text color themes to reflect day or night time visual themes.
     *
     * @param isDay A boolean value indicating whether it is currently day (true) or night (false).
     */
    fun nightAndDay(isDay: Boolean){
        val dayBackground = ContextCompat.getDrawable(settings, R.drawable.background_day)
        val nightBackground = ContextCompat.getDrawable(settings, R.drawable.background_night)
        val blackBorder = ContextCompat.getDrawable(settings, R.drawable.info_border_black)
        val whiteBorder = ContextCompat.getDrawable(settings, R.drawable.info_border_white)
        val blackSpinner = ContextCompat.getDrawable(settings, R.drawable.spinner_design_day)
        val whiteSpinner = ContextCompat.getDrawable(settings, R.drawable.spinner_design_night)
        val spinnerColor = if(isDay) blackSpinner else whiteSpinner
        val borderColor = if(isDay) blackBorder else whiteBorder
        val textColor = if (isDay) Color.parseColor("#000000") else Color.parseColor("#FFFFFF")
        if(isDay){
            settings.findViewById<LinearLayout>(R.id.settings_layout).background = dayBackground
        }
        else{
            settings.findViewById<LinearLayout>(R.id.settings_layout).background = nightBackground
        }

        // Applying the text color and filter adjustments to buttons, icons, and text.
        settings.findViewById<TextView>(R.id.settings_title_textView).setTextColor(textColor)
        settings.findViewById<ImageButton>(R.id.settings_back_btn).setColorFilter(textColor)
        settings.findViewById<TextView>(R.id.breed_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.color_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.acclimation_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.health_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.shade_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.feed_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.manure_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.water_settings_title).setTextColor(textColor)
        settings.findViewById<TextView>(R.id.breed_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.color_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.acclimation_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.health_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.shade_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.feed_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.manure_settings_title).background = borderColor
        settings.findViewById<TextView>(R.id.water_settings_title).background = borderColor
        settings.findViewById<Spinner>(R.id.breed_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.color_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.acclimation_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.health_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.shade_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.feed_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.manure_spinner).background = spinnerColor
        settings.findViewById<Spinner>(R.id.water_spinner).background = spinnerColor

    }
}