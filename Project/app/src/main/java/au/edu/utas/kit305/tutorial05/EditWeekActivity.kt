package au.edu.utas.kit305.tutorial05

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import au.edu.utas.kit305.tutorial05.databinding.ActivityEditWeekBinding

class EditWeekActivity : AppCompatActivity() {
    private lateinit var ui : ActivityEditWeekBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityEditWeekBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //Marking Schema spinner
        val spinner = ui.spinner
        val items = arrayOf("Percentage", "HD/DN/CR/PP/NN", "A/B/C/D/F")
        val spinnerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                items
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Display the selected item text on text view
                //ui.txtMarkingType.text = "Spinner selected : ${parent.getItemAtPosition(position).toString()}"
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }
}