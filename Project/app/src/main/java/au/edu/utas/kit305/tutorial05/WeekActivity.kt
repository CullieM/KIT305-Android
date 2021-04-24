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
import au.edu.utas.kit305.tutorial05.databinding.ActivityWeekBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class WeekActivity : AppCompatActivity() {

    private lateinit var ui : ActivityWeekBinding
    private val weekMarks = mutableListOf<Mark>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityWeekBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val weekIndex = intent.getIntExtra(WEEK_INDEX, -1)
        var weekObject = weeks[weekIndex]

        var calculateMean: Int = 0
        ui.txtStudentName.text = "Week " + weekObject.number

        //db setup
        val db = Firebase.firestore
        var marksCollection = db.collection("weeks").document(weekObject.id.toString()).collection("student_marks")
        marksCollection
                .get()
                .addOnSuccessListener { result ->
                    Log.d(FIREBASE_TAG, "---Week " + weekObject.number + " Marks ---")
                    for (document in result) {
                        //Log.d(FIREBASE_TAG, document.toString())
                        val weekMark = document.toObject<Mark>()
                        Log.d(FIREBASE_TAG, weekMark.toString())
                        calculateMean += weekMark.mark?.toInt()!!
                        weekMarks.add(weekMark)
                        (ui.myList.adapter as MarkAdapter).notifyDataSetChanged()
                    }
                    calculateMean /= weekMarks.size
                    ui.txtClassAverage.text = calculateMean.toString()
                }
        ui.myList.adapter = MarkAdapter(weekMarks)
        ui.myList.layoutManager = LinearLayoutManager(this)

        //TODO Change mark display based on marking_type
        ui.txtMarkingType.text = weekObject.marking_type
        ui.txtClassAverageHeading.text = "Average"

        Log.d(FIREBASE_TAG, "weekMarks.size is : "+weekMarks.size.toString())
        Log.d(FIREBASE_TAG, "calculateMean.toString() is : $calculateMean")

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
            var name: String
            var filteredList: List<Student> = students.filter { it.id == mark.id }

            if (filteredList.isNotEmpty()){
                name = filteredList[0].full_name.toString()
            }else{
                name = "Deleted Student"
            }
            holder.listViewUI.txtNumber.text = name
            holder.listViewUI.txtMarkingType.text = mark.mark
            //TODO Change mark display based on marking_type
        }
    }
}