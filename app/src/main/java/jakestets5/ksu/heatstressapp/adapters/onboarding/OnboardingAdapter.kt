package jakestets5.ksu.heatstressapp.adapters.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * A custom PagerAdapter for managing a ViewPager in an onboarding sequence.
 *
 * @param context The context (typically the activity) used to inflate layouts.
 * @param layouts An array of layout resource IDs that will be displayed as pages in the ViewPager.
 */
class OnboardingAdapter(private val context: Context, private val layouts: IntArray) : PagerAdapter() {

    /**
     * Creates the page for the given position.
     * The page created is added to the container view.
     *
     * @param container The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     * @return The object representing the new page. This is not necessarily the view itself, but
     * an object that can be used to uniquely identify the page view.
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(layouts[position], container, false)
        container.addView(layout)
        return layout
    }

    /**
     * Return the total number of pages available.
     */
    override fun getCount(): Int {
        return layouts.size
    }

    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by instantiateItem(ViewGroup, int).
     *
     * @param view The page view to check for association with object
     * @param object The object to which the view may be associated
     * @return True if view is associated with the key object.
     */
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    /**
     * Removes a page for the given position.
     * The adapter is responsible for removing the view from its container.
     *
     * @param container The containing View from which the page will be removed.
     * @param position The page position to be removed.
     * @param object The same object that was returned by instantiateItem.
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}
