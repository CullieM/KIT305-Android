package au.edu.utas.kit305.tutorial05

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.classes.Student
import au.edu.utas.kit305.tutorial05.classes.Week
import au.edu.utas.kit305.tutorial05.databinding.ActivityWeekBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class WeekActivity : AppCompatActivity() {

    private lateinit var ui: ActivityWeekBinding
    private var calculateMean: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityWeekBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val weekIndex = intent.getIntExtra(WEEK_INDEX, -1)
        var weekObject = weeks[weekIndex]

        ui.txtStudentName.text = "Week " + weekObject.number

        //db setup
        val db = Firebase.firestore
        weekMarks.clear()
        var marksCollection = db.collection("weeks").document(weekObject.id.toString()).collection("student_marks")
        marksCollection
                .get()
                .addOnSuccessListener { result ->
                    Log.d(FIREBASE_TAG, "---Week " + weekObject.number + " Marks ---")
                    calculateMean = 0
                    for (document in result) {
                        //Log.d(FIREBASE_TAG, document.toString())
                        val weekMark = document.toObject<Mark>()
                        Log.d(FIREBASE_TAG, weekMark.toString())
                        calculateMean += weekMark.mark?.toInt()!!
                        weekMarks.add(weekMark)
                        (ui.myList.adapter as MarkAdapter).notifyDataSetChanged()
                    }
                    calculateMean /= weekMarks.size
                    var filteredMark: String = ""
                    val funClass = TabbedActivity()
                    ui.txtClassAverage.text = funClass.calculateMark(weekObject?.marking_type!!,calculateMean)
                }
        ui.myList.adapter = MarkAdapter(weekMarks)
        ui.myList.layoutManager = LinearLayoutManager(this)

        //TODO Change mark display based on marking_type
        ui.txtMarkingType.text = weekObject.marking_type
        ui.txtClassAverageHeading.text = "Average"

        //Share in plain text button
        ui.btnShare.setOnClickListener {
            var sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                var textToShare = "Week " + weeks[weekIndex].number + "\n"
                textToShare += "Class Average: $calculateMean\n\n"
                for (i in 0 until weekMarks.size) {
                    val mark = weekMarks[i]
                    var filteredList: List<Student> = students.filter { it.id == mark.id }
                    if (filteredList.isNotEmpty()) {
                        val name = filteredList[0].full_name.toString()
                        textToShare += name + "\n"
                        textToShare += "Mark:" + weekMarks[i].mark + "\n\n"
                    }
                }
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "Share via..."))
        }

        //Edit the marks for the work, and the schema.
        ui.btnEdit.setOnClickListener {
            val i = Intent(this, EditWeekActivity::class.java)
            i.putExtra(WEEK_INDEX, weekIndex)
            startActivityForResult(i, EDITED_WEEK_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == EDITED_WEEK_CODE && resultCode == EDITED_WEEK_CODE) {
            val weekID = intent.getIntExtra(WEEK_INDEX, -1)
            ui.txtMarkingType.text = weeks[weekID].marking_type.toString()
            ui.myList.adapter?.notifyDataSetChanged()


            val funClass = TabbedActivity()
            ui.txtClassAverage.text = funClass.calculateMark(weeks[weekID].marking_type.toString(),calculateMean)

            //TODO GET Rid of this Log.d(FIREBASE_TAG, funClass.calculateMark(weeks[weekID].marking_type.toString(),calculateMean))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class MarkHolder(var listViewUI: WeekListItemBinding) : RecyclerView.ViewHolder(listViewUI.root) {}
    inner class MarkAdapter(private val weekMarks: MutableList<Mark>) : RecyclerView.Adapter<WeekActivity.MarkHolder>() {
        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekActivity.MarkHolder {
            val listViewUI = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return MarkHolder(listViewUI)
        }

        //Returns the size of the array
        override fun getItemCount(): Int {
            return weekMarks.size
        }

        //Populates each row
        override fun onBindViewHolder(holder: WeekActivity.MarkHolder, position: Int) {
            val mark = weekMarks[position]
            val filteredWeekList: List<Week> = weeks.filter { it.number == mark.week }
            var name: String
            var filteredStudentList: List<Student> = students.filter { it.id == mark.id }

            if (filteredStudentList.isNotEmpty()) {
                name = filteredStudentList[0].full_name.toString()
            } else {
                name = "Deleted Student"
            }
            holder.listViewUI.txtNumber.text = name
            //var filteredMark: String = ""
            val funClass = TabbedActivity()
            holder.listViewUI.txtMarkingType.text = funClass.calculateMark(filteredWeekList[0].marking_type.toString(), mark.mark?.toInt()!!)
            /*
            when (filteredWeekList[0].marking_type) {
                "Percentage" -> {
                    filteredMark = mark.mark + "%"
                }
                "HD/DN/CR/PP/NN" -> {
                    filteredMark = when(mark.mark?.toInt()){
                        100 -> "HD+"
                        in 80..99 -> "HD"
                        in 70..79 -> "DN"
                        in 60..69 -> "CR"
                        in 50..59 -> "PP"
                        else -> "NN"
                    }
                }
                "A/B/C/D/F" -> {
                    filteredMark = when(mark.mark?.toInt()) {
                        100 -> "A"
                        in 80..99 -> "B"
                        in 70..79 -> "C"
                        in 60..69 -> "D"
                        else -> "F"
                    }
                }
            }
            holder.listViewUI.txtMarkingType.text = filteredMark
            */
            //TODO Change mark display based on marking_type
        }
    }
}