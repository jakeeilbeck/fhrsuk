package com.android.fhrsuk

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.fhrsuk.nearbyList.NearbyFragment
import com.android.fhrsuk.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private val nearbyFragment = NearbyFragment()
    private val searchFragment = SearchFragment()

    private lateinit var searchIcon: MenuItem
    private lateinit var listIcon: MenuItem

    private val iconSelectedAlpha: Int = 255
    private val iconUnselectedAlpha: Int = 137

    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {

            viewPager2 = findViewById(R.id.view_pager)
            val pagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

            pagerAdapter.addFragment(nearbyFragment)
            pagerAdapter.addFragment(searchFragment)

            viewPager2.adapter = pagerAdapter

        } else {
            //Prevent viewPager2 causing crashes on process death in onPrepareOptionsMenu
            //by ensuring that an instance exists
            if (!this::viewPager2.isInitialized) {
                viewPager2 = findViewById(R.id.view_pager)
                val pagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

                pagerAdapter.addFragment(nearbyFragment)
                pagerAdapter.addFragment(searchFragment)

                viewPager2.adapter = pagerAdapter
            }

            return
        }
    }

    //options menu used for navigation between fragments in addition to swipe
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_switch_fragment, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Set initial icon alpha
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        searchIcon = menu!!.findItem(R.id.switch_to_search)
        listIcon = menu.findItem(R.id.switch_to_list)

        searchIcon.icon.alpha = iconUnselectedAlpha

        //Set icon alpha if user swiped between fragments
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        listIcon.icon.alpha = iconSelectedAlpha
                        searchIcon.icon.alpha = iconUnselectedAlpha
                    }
                    1 -> {
                        listIcon.icon.alpha = iconUnselectedAlpha
                        searchIcon.icon.alpha = iconSelectedAlpha
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

                listIcon.icon.alpha = iconSelectedAlpha
                searchIcon.icon.alpha = iconUnselectedAlpha

                viewPager2.setCurrentItem(0, true)
            }

            R.id.switch_to_search -> {

                listIcon.icon.alpha = iconUnselectedAlpha
                searchIcon.icon.alpha = iconSelectedAlpha

                viewPager2.setCurrentItem(1, true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Return to nearby fragment, otherwise exit app
    override fun onBackPressed() {

        if (viewPager2.currentItem == 1) {
            this.listIcon.icon.alpha = iconSelectedAlpha
            this.searchIcon.icon.alpha = iconUnselectedAlpha
            viewPager2.setCurrentItem(0, true)

        } else {
            super.onBackPressed()
        }
    }
}