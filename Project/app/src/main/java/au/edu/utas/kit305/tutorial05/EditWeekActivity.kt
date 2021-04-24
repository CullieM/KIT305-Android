package au.edu.utas.kit305.tutorial05

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import au.edu.utas.kit305.tutorial05.databinding.ActivityEditWeekBinding
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
                // Display the selected item text on text view
                //ui.txtMarkingType.text = "Spinner selected : ${parent.getItemAtPosition(position).toString()}"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        ui.floatingActionButton.setOnClickListener {

            //Update local data
            weekObject.marking_type = ui.spinner.selectedItem.toString()
            weeks[weekID].marking_type = ui.spinner.selectedItem.toString()

            //Update DB Data
            val editDB = Firebase.firestore
            editDB.collection("weeks")
                    .document(weekObject.id.toString())
                    .set(weekObject)

            var i = Intent(this, WeekActivity::class.java)
            i.putExtra(WEEK_INDEX,weekID)
            setResult(EDITED_WEEK_CODE)
            finish()
        }

    }
}