package au.edu.utas.kit305.tutorial05

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.classes.Student
import au.edu.utas.kit305.tutorial05.classes.Week
import au.edu.utas.kit305.tutorial05.databinding.ActivityEditWeekBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditWeekActivity : AppCompatActivity() {
    private lateinit var ui: ActivityEditWeekBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityEditWeekBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //Retrieve intent data for relevant week.
        val weekID = intent.getIntExtra(WEEK_INDEX, -1)
        var weekObject = weeks[weekID]

        ui.txtWeekNumber.text = "Week " + weekObject.number.toString()
        ui.myList.adapter = MarkAdapter(weekMarks, this)
        ui.myList.layoutManager = LinearLayoutManager(this)

        //Marking Schema spinner
        val spinner = ui.spinner
        val markingTypes = arrayOf("Percentage", "HD/DN/CR/PP/NN", "A/B/C/D/F")
        val spinnerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                markingTypes
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = spinnerAdapter
        ui.spinner.setSelection(markingTypes.indexOf(weekObject.marking_type))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                weeks[weekID].marking_type = markingTypes[position]
                ui.myList.adapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        //Save button TODO THIS IS A MESS
        ui.floatingActionButton.setOnClickListener {

            //Update local data
            weekObject.marking_type = ui.spinner.selectedItem.toString()
            weeks[weekID].marking_type = ui.spinner.selectedItem.toString()

            //Update DB data for marks
            val db = Firebase.firestore
            var calculateMean = 0
            for (i in 0 until weekMarks.size) {
                db.collection("weeks")
                        .document(weekObject.id.toString())
                        .collection("student_marks")
                        .document(weekMarks[i].id.toString())
                        .set(weekMarks[i])
                //Sum total marks for mean mark
                calculateMean += weekMarks[i].mark?.toInt()!!
            }
            //Divide by total weeks to get mean mark
            calculateMean /= weekMarks.size

            //Update overall mark for the week locally
            weeks[weekID].overall_mark = calculateMean

            //Update DB Data for week
            weekObject.overall_mark = calculateMean
            val editDB = Firebase.firestore
            editDB.collection("weeks")
                    .document(weekObject.id.toString())
                    .set(weekObject)

            var i = Intent(this, WeekActivity::class.java)
            i.putExtra(WEEK_INDEX, weekID)
            setResult(EDITED_WEEK_CODE)
            finish()
        }

    }

    inner class MarkHolder(var listViewUI: WeekListItemBinding) : RecyclerView.ViewHolder(listViewUI.root) {}

    inner class MarkAdapter(private val weekMarks: MutableList<Mark>, private val context: Context) : RecyclerView.Adapter<EditWeekActivity.MarkHolder>() {
        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditWeekActivity.MarkHolder {
            val listViewUI = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return MarkHolder(listViewUI)
        }

        //Returns the size of the array
        override fun getItemCount(): Int {
            return weekMarks.size
        }

        //Populates each row
        override fun onBindViewHolder(holder: EditWeekActivity.MarkHolder, position: Int) {
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
            val funClass = TabbedActivity()
            holder.listViewUI.txtMarkingType.text = funClass.calculateMark(filteredWeekList[0].marking_type.toString(), mark.mark?.toInt()!!)

            //EditText inside an AlertDialog taken from https://handyopinion.com/show-alert-dialog-with-an-input-field-edittext-in-android-kotlin/
            holder.listViewUI.root.setOnClickListener {
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                var tempString = funClass.calculatePercentage(mark.mark!!)
                tempString = funClass.calculateMark(filteredWeekList[0].marking_type.toString(), tempString.toInt())
                input.hint = tempString

                val dialogClickListener =
                        DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    var inputText = input.text.toString()
                                    val funClass = TabbedActivity()

                                    //Turn user input to a percentage
                                    inputText = funClass.calculatePercentage(inputText)
                                    //Store the percentage
                                    weekMarks[position].mark = inputText
                                    //Translate to the appropriate marking schema (If you enter HD for week 3 it will be converted to 80, then converted to whatever is appropriate for that week.)
                                    inputText = funClass.calculateMark(filteredWeekList[0].marking_type.toString(), inputText.toInt())
                                    //Fill the textView
                                    holder.listViewUI.txtMarkingType.text = inputText
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {

                                }
                            }
                        }

                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(context)
                builder.setView(input)
                builder.setTitle("Edit mark for $name")
                        .setPositiveButton("Save", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener)
                        .show()
            }
        }
    }
}