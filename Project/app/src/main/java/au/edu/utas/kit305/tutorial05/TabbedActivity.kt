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
const val EDITED_WEEK_CODE = 17
const val REQUEST_IMAGE_CAPTURE = 18
const val ADD_STUDENT_CODE = 19

//Strings
const val FIREBASE_TAG = "FirebaseLogging: "
const val STUDENT_INDEX = "Student_Index"
const val AVG = "Average"
const val WEEK_INDEX = "Week_Index"

//Lists
var students = mutableListOf<Student>()
val weeks = mutableListOf<Week>()
val marks = mutableListOf<Mark>()
val weekMarks = mutableListOf<Mark>()

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

    fun calculateMark(markingType: String, score: Int): String {
        var filteredMark: String = ""
        when (markingType) {
            "Percentage" -> {
                filteredMark = "$score%"
            }
            "HD/DN/CR/PP/NN" -> {
                filteredMark = when (score) {
                    100 -> "HD+"
                    in 80..99 -> "HD"
                    in 70..79 -> "DN"
                    in 60..69 -> "CR"
                    in 50..59 -> "PP"
                    else -> "NN"
                }
            }
            "A/B/C/D/F" -> {
                filteredMark = when (score) {
                    100 -> "A"
                    in 80..99 -> "B"
                    in 70..79 -> "C"
                    in 60..69 -> "D"
                    else -> "F"
                }
            }
        }
        return filteredMark
    }

    fun calculatePercentage(input: String): String {
        var output = ""
        var percent = "%"
        if (percent in input) {
            input.replace(percent,"")
        }
        if (input.toIntOrNull() in 0..100) {
            return input.toString()
        } else {
            output = when (input) {
                "HD+" -> "100"
                "HD" -> "80"
                "DN" -> "70"
                "CR" -> "60"
                "PP" -> "50"
                "NN" -> "0"
                "A" -> "100"
                "B" -> "80"
                "C" -> "70"
                "D" -> "60"
                "F" -> "0"
                else -> "0"
            }
            return output
        }
    }
}
