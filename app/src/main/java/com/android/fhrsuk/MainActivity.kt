package com.android.fhrsuk

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
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

class MainActivity : AppCompatActivity(), FragmentVisibleListener {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private val nearbyFragment = NearbyFragment()
    private val searchFragment = SearchFragment()
    private val favouritesFragment = FavouritesFragment()

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
        pagerAdapter.addFragment(favouritesFragment)

        viewPager2.adapter = pagerAdapter

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.bottomNavigation.menu.getItem(0).isChecked = true
                    }
                    1 -> {
                        binding.bottomNavigation.menu.getItem(1).isChecked = true
                    }
                    2 -> {
                        binding.bottomNavigation.menu.getItem(2).isChecked = true
                    }
                }
                super.onPageSelected(position)
            }
        })

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_nearby -> {
                    viewPager2.setCurrentItem(0, true)
                    true
                }
                R.id.menu_search -> {
                    viewPager2.setCurrentItem(1, true)
                    true
                }
                R.id.menu_favourites -> {
                    viewPager2.setCurrentItem(2, true)
                    true
                }
                else -> false
            }
        }
    }

    //FragmentVisibleListener implementation
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is NearbyFragment) {
            fragment.fragmentVisibleListener = this
        }
    }

    override fun onFragmentVisible() {
        hideProgressBar()
    }

    private fun hideProgressBar() {
        binding.progressBar.isVisible = false
    }

    //options menu used for navigation between fragments in addition to swiping
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Set icon alpha, and use viewPager to switch fragment
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.rating_breakdown_info -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.ratings_breakdown_info_dialog)
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Return to nearby fragment, otherwise exit app
    override fun onBackPressed() {

        if (viewPager2.currentItem == 1) {
            viewPager2.setCurrentItem(0, true)
        } else {
            super.onBackPressed()
        }
    }
}