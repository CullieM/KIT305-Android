package au.edu.utas.kit305.tutorial05

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.classes.Student
import au.edu.utas.kit305.tutorial05.classes.Week
import au.edu.utas.kit305.tutorial05.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
//GLOBAL VARIABLES

//View Binding Declarations


//Ints (Intent Codes)
const val LAUNCH_SECOND_ACTIVITY = 2
const val EDITED_CODE = 14
const val IMAGE_CODE = 15
const val DELETED_CODE = 16
const val REQUEST_IMAGE_CAPTURE = 18
const val ADD_STUDENT_CODE = 19
//Strings
const val FIREBASE_TAG = "FirebaseLogging: "
const val STUDENT_INDEX = "Student_Index"
const val AVG = "Average"
const val WEEK_INDEX = "Week_Index"
//Lists
var students = mutableListOf<Student>()
val marks = mutableListOf<Mark>()
val weeks = mutableListOf<Week>()
//Other
const val ONE_MEGABYTE: Long = 1024 * 1024

class TabbedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_tabbed)
       val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
       val viewPager: ViewPager = findViewById(R.id.view_pager)
       viewPager.adapter = sectionsPagerAdapter
       val tabs: TabLayout = findViewById(R.id.tabs)
       tabs.setupWithViewPager(viewPager)
    }
}