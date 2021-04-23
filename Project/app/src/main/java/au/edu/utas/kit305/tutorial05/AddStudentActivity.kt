package au.edu.utas.kit305.tutorial05

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import au.edu.utas.kit305.tutorial05.databinding.ActivityAddStudentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.reflect.Array.set

private lateinit var ui : ActivityAddStudentBinding

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ui = ActivityAddStudentBinding.inflate(layoutInflater)
    setContentView(ui.root)
        ui.btnSave.setOnClickListener {
            //get the user input
            var inputID = ui.editStudentID.text.toString()
            var inputName = ui.editName.text.toString()
            val student = Student(
                id = inputID,
                student_id = inputID,
                full_name = inputName,
                overall_mark = 0,
                image = ""
            )
            //update the database
            val db = Firebase.firestore
            var studentsCollection = db.collection("students")
            val blankMark = Mark(
                id = inputID,
                week = -1,
                mark = "0"
            )
            studentsCollection.document(inputID)
                .set(student)
                .addOnSuccessListener {
                    Log.d(FIREBASE_TAG, "Successfully added student $inputName")
                    students.add(student)
                    for(i in 0 until weeks.size) {
                        blankMark.week = weeks[i].number
                        db.collection("weeks")
                            .document(weeks[i].id.toString())
                            .collection("student_marks")
                            .document(inputID)
                            .set(blankMark)
                            .addOnSuccessListener{Log.d(FIREBASE_TAG, "Successfully added mark")}
                    }
                    //return to the list
                    finish()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
    }
}