package com.android.fhrsuk

//TODO ProgressBar disappears too soon
//TODO Add ViewPager to swipe between fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.fhrsuk.nearbyList.NearbyFragment
import com.android.fhrsuk.search.SearchFragment
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private val nearbyFragment = NearbyFragment()
    private val searchFragment = SearchFragment()

    private lateinit var searchIcon: MenuItem
    private lateinit var listIcon: MenuItem

    private val iconSelectedAlpha: Int = 255
    private val iconUnselectedAlpha: Int = 137

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    searchFragment, "search_fragment"
                )
                .hide(searchFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    nearbyFragment, "nearby_fragment"
                )
                .commit()
        }
        else{
            //restart activity in instance of process death
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

            return
        }
    }

    //options menu used for navigation between fragments
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_switch_fragment, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        searchIcon = menu!!.findItem(R.id.switch_to_search)
        listIcon = menu.findItem(R.id.switch_to_list)

        searchIcon.icon.alpha = iconUnselectedAlpha

        return super.onPrepareOptionsMenu(menu)
    }

    //show/hide fragments on navigation selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when {
            item.itemId == R.id.switch_to_list -> {

                this.listIcon.icon.alpha = iconSelectedAlpha
                this.searchIcon.icon.alpha = iconUnselectedAlpha

                supportFragmentManager.beginTransaction()
                    .hide(searchFragment)
                    .show(nearbyFragment)
                    .commit()
            }

            item.itemId == R.id.switch_to_search -> {

                this.listIcon.icon.alpha = iconUnselectedAlpha
                this.searchIcon.icon.alpha = iconSelectedAlpha

                supportFragmentManager.beginTransaction()
                    .hide(nearbyFragment)
                    .show(searchFragment)
                    .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("search_fragment")!!.isVisible) {
            this.listIcon.icon.alpha = iconSelectedAlpha
            this.searchIcon.icon.alpha = iconUnselectedAlpha

            supportFragmentManager.beginTransaction()
                .hide(searchFragment)
                .show(nearbyFragment)
                .commit()
        } else {
            super.onBackPressed()
        }
    }
}
