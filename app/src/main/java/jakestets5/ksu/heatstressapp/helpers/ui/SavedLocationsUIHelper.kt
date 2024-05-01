package jakestets5.ksu.heatstressapp.helpers.ui

import android.graphics.Color
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.SavedLocationsActivity
/**
 * A utility class to assist in updating the user interface of the ForecastActivity based on the time of day.
 *
 * @param savedLocations An instance of SavedLocationsActivity, the context in which the UI updates are applied.
 */
class SavedLocationsUIHelper(private val savedLocations: SavedLocationsActivity) {
    /**
     * Adjusts the user interface of the SavedLocationsActivity based on the time of day. This includes changing background
     * images and text color themes to reflect day or night time visual themes.
     *
     * @param isDay A boolean value indicating whether it is currently day (true) or night (false).
     */
    fun nightAndDay(isDay: Boolean) {
        val dayBackground = ContextCompat.getDrawable(savedLocations, R.drawable.background_day)
        val nightBackground = ContextCompat.getDrawable(savedLocations, R.drawable.background_night)

        val textColor = if (isDay) Color.parseColor("#000000") else Color.parseColor("#FFFFFF")
        if (isDay) {
            savedLocations.findViewById<RelativeLayout>(R.id.saved_locations_view).background =
                dayBackground
        } else {
            savedLocations.findViewById<RelativeLayout>(R.id.saved_locations_view).background =
                nightBackground
        }

        savedLocations.findViewById<TextView>(R.id.saved_locations_title_textView).setTextColor(textColor)
        savedLocations.findViewById<ImageButton>(R.id.saved_locations_back_btn).setColorFilter(textColor)
    }
}