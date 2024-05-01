package jakestets5.ksu.heatstressapp.helpers.ui

import android.graphics.Color
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.ForecastActivity

/**
 * A utility class to assist in updating the user interface of the ForecastActivity based on the time of day.
 *
 * @param forecastActivity An instance of ForecastActivity, the context in which the UI updates are applied.
 */
class ForecastUIHelper(private val forecastActivity: ForecastActivity) {

    /**
     * Adjusts the user interface of the ForecastActivity based on the time of day. This includes changing background
     * images and text color themes to reflect day or night time visual themes.
     *
     * @param isDay A boolean value indicating whether it is currently day (true) or night (false).
     */
    fun nightAndDay(isDay: Boolean){
        val dayBackground = ContextCompat.getDrawable(forecastActivity, R.drawable.background_day)
        val nightBackground = ContextCompat.getDrawable(forecastActivity, R.drawable.background_night)
        val textColor = if (isDay) Color.parseColor("#000000") else Color.parseColor("#FFFFFF")
        if(isDay) {
            forecastActivity.findViewById<RelativeLayout>(R.id.forecast_view).background = dayBackground
            forecastActivity.findViewById<ImageButton>(R.id.forecast_back_btn).setColorFilter(textColor)
        }
        else{
            forecastActivity.findViewById<RelativeLayout>(R.id.forecast_view).background = nightBackground
            forecastActivity.findViewById<ImageButton>(R.id.forecast_back_btn).setColorFilter(textColor)
        }
    }
}