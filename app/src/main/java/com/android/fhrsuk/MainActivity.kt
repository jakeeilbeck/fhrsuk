package com.android.fhrsuk

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.fhrsuk.adapters.ViewPagerAdapter
import com.android.fhrsuk.databinding.MainActivityBinding
import com.android.fhrsuk.favourites.FavouritesFragment
import com.android.fhrsuk.nearbyList.FragmentVisibleListener
import com.android.fhrsuk.nearbyList.NearbyFragment
import com.android.fhrsuk.search.SearchFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

private const val ICON_SELECTED_ALPHA: Int = 255
private const val ICON_UNSELECTED_ALPHA: Int = 137

class MainActivity : AppCompatActivity(), FragmentVisibleListener {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private val nearbyFragment = NearbyFragment()
//    private val searchFragment = SearchFragment()
    private val searchFragment = FavouritesFragment()

    private lateinit var searchIcon: MenuItem
    private lateinit var listIcon: MenuItem
    private lateinit var viewPager2: ViewPager2
    private lateinit var binding: MainActivityBinding

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

            viewPager2 = binding.viewPager
            val pagerAdapter = ViewPagerAdapter(
                supportFragmentManager,
                lifecycle
            )

            pagerAdapter.addFragment(nearbyFragment)
            pagerAdapter.addFragment(searchFragment)

            viewPager2.adapter = pagerAdapter
    }

    //FragmentVisibleListener implementation
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is NearbyFragment){
            fragment.fragmentVisibleListener = this
        }
    }

    override fun onFragmentVisible() {
        hideProgressBar()
    }

    private fun hideProgressBar(){
        binding.progressBar.isVisible = false
    }

    //options menu used for navigation between fragments in addition to swiping
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_switch_fragment, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        searchIcon = menu!!.findItem(R.id.switch_to_search)
        listIcon = menu.findItem(R.id.switch_to_list)

        searchIcon.icon.alpha = ICON_UNSELECTED_ALPHA

        //Set icon alpha if user switched between fragments
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        listIcon.icon.alpha = ICON_SELECTED_ALPHA
                        searchIcon.icon.alpha = ICON_UNSELECTED_ALPHA
                    }
                    1 -> {
                        listIcon.icon.alpha = ICON_UNSELECTED_ALPHA
                        searchIcon.icon.alpha = ICON_SELECTED_ALPHA
                        //Hide progressBar if user switches to Search before Nearby List has loaded
                        hideProgressBar()
                    }
                }
            }
        })
        return super.onPrepareOptionsMenu(menu)
    }

    //Set icon alpha, and use viewPager to switch fragment
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.switch_to_list -> {

                listIcon.icon.alpha = ICON_SELECTED_ALPHA
                searchIcon.icon.alpha = ICON_UNSELECTED_ALPHA

                viewPager2.setCurrentItem(0, true)
            }

            R.id.switch_to_search -> {

                listIcon.icon.alpha = ICON_UNSELECTED_ALPHA
                searchIcon.icon.alpha = ICON_SELECTED_ALPHA

                viewPager2.setCurrentItem(1, true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Return to nearby fragment, otherwise exit app
    override fun onBackPressed() {

        if (viewPager2.currentItem == 1) {
            this.listIcon.icon.alpha = ICON_SELECTED_ALPHA
            this.searchIcon.icon.alpha = ICON_UNSELECTED_ALPHA
            viewPager2.setCurrentItem(0, true)

        } else {
            super.onBackPressed()
        }
    }
}