package jakestets5.ksu.heatstressapp.activities

import jakestets5.ksu.heatstressapp.adapters.onboarding.OnboardingAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import jakestets5.ksu.heatstressapp.R

/**
 * Activity to manage the onboarding process for first-time users.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: OnboardingAdapter

    /**
     * Sets up initial configurations and triggers the onboarding process
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if onboarding has already been completed and if so, navigate to MainActivity.
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)

        if (isOnboardingComplete) {
            // Onboarding completed, launch main activity
            val editor = prefs.edit()
            editor.putBoolean("onboarding_complete", true)
            editor.apply()

            val onboardingIntent = Intent(this, MainActivity::class.java)
            startActivity(onboardingIntent)
        }

        setContentView(R.layout.activity_onboarding)

        val layouts = intArrayOf(
            R.layout.onboarding_screen1,
            R.layout.onboarding_screen2,
            R.layout.onboarding_screen3,
            R.layout.onboarding_screen4,
            R.layout.onboarding_screen5
        )
        // Initialize the adapter with the layouts and set up the ViewPager.
        adapter = OnboardingAdapter(this, layouts)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter

        // Set up the tab layout with custom views for each tab as indicators.
        val tabLayout = findViewById<TabLayout>(R.id.tabDots)
        for (i in 0 until adapter.count) {
            val tab = tabLayout.newTab()
            tab.setCustomView(R.layout.custom_tab)
            tabLayout.addTab(tab)
        }
        tabLayout.setupWithViewPager(viewPager, true)

        // Button to go to the next onboarding screen or finish the onboarding process.
        val btnNext = findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            val current = viewPager.currentItem + 1
            if (current < layouts.size) {
                viewPager.currentItem = current
            } else {
                val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putBoolean("onboarding_complete", true)
                    apply()
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Button to navigate back to the previous onboarding screen.
        val btnPrevious = findViewById<Button>(R.id.btnPrevious)
        btnPrevious.setOnClickListener {
            val current = viewPager.currentItem - 1
            if (current < layouts.size) {
                viewPager.currentItem = current
            } else {
                viewPager.currentItem = current + 1
            }
        }
    }
}