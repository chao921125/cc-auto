package org.autojs.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.autojs.app.ui.ManageFragment
import org.autojs.app.ui.ScriptsFragment
import org.autojs.app.ui.SettingsFragment
import org.autojs.app.ui.TutorialFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scripts -> switchFragment(ScriptsFragment())
                R.id.nav_manage -> switchFragment(ManageFragment())
                R.id.nav_tutorial -> switchFragment(TutorialFragment())
                R.id.nav_settings -> switchFragment(SettingsFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_scripts
        }
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}
