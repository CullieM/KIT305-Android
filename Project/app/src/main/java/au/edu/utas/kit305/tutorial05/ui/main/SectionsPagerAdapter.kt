package au.edu.utas.kit305.tutorial05.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import au.edu.utas.kit305.tutorial05.StudentListFragment
import au.edu.utas.kit305.tutorial05.WeeksListFragment

private val TAB_TITLES= arrayOf(
    "Students",
    "Weeks"
)
/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position)
        {
            0 -> StudentListFragment()
            1 -> WeeksListFragment()
            else -> StudentListFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return TAB_TITLES[position]
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}