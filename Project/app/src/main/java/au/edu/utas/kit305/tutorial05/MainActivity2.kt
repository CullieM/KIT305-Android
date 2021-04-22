package au.edu.utas.kit305.tutorial05

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import au.edu.utas.kit305.tutorial05.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.storage.FirebaseStorage

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main2)
       val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
       val viewPager: ViewPager = findViewById(R.id.view_pager)
       viewPager.adapter = sectionsPagerAdapter
       val tabs: TabLayout = findViewById(R.id.tabs)
       tabs.setupWithViewPager(viewPager)
    }
}