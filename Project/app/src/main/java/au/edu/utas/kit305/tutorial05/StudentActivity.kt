package au.edu.utas.kit305.tutorial05

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.databinding.ActivityMainBinding
import au.edu.utas.kit305.tutorial05.databinding.ActivityStudentBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

const val LAUNCH_SECOND_ACTIVITY = 2
const val AVG = "Average"
const val ONE_MEGABYTE: Long = 1024 * 1024
class StudentActivity : AppCompatActivity() {
    private lateinit var ui : ActivityStudentBinding
    private val marks = mutableListOf<Mark>()

    //Upon returning from Edit Student Activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == DELETED_CODE)
        {
            val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
            students.removeAt(studentID)
            students = students.filterNotNull().toMutableList()
            finish()
        }else if (resultCode == EDITED_CODE) {
            //Retrieve studentID (INDEX) from Intent
            val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
            var studentObject = students[studentID]
            //Redraw the text fields
            ui.txtStudentID.text = studentObject.student_id
            ui.txtStudentName.text = studentObject.full_name
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //View binding
        ui = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //Retrieve studentID (INDEX) from intent, upon selecting from StudentListFragment
        val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
        var studentObject = students[studentID]

        //DOWNLOAD IMAGE FROM CLOUDSTORE
        var mStorageRef : StorageReference = FirebaseStorage.getInstance().reference;
        var childStorageRef = mStorageRef.child("studentPictures/" + students[studentID].id + ".jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024
        mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val takenImage = it as Bitmap
            ui.imageView.setImageBitmap(takenImage)
        }.addOnFailureListener {
            // Handle any errors
        }

        //Pre-fill the TextViews
        ui.average.text = AVG
        ui.txtStudentName.text = studentObject.full_name
        ui.txtStudentID.text = studentObject.student_id
        ui.txtOverallMark.text = studentObject.overall_mark.toString()

        //Button for going to EditStudentActivity, passing the student index.
        ui.btnEdit.setOnClickListener {
            val i = Intent(this, EditStudentActivity::class.java)
            i.putExtra(STUDENT_INDEX, studentID)
            startActivityForResult(i, LAUNCH_SECOND_ACTIVITY)
        }
        //Share in plain text button
        ui.btnShare.setOnClickListener {
            var sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                var textToShare = students[studentID].full_name + "\n\n" + ""
                for(i in 0 until marks.size) {
                    textToShare += "Week " + marks[i].week + "\n"
                    textToShare += "Mark:" + marks[i].mark + "\n\n"
                }
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"}
            startActivity(Intent.createChooser(sendIntent, "Share via..."))

        }
        //db setup
        val db = Firebase.firestore
        for (i in 0 until weeks.size) {
            var marksCollection = db.collection("weeks").document(weeks[i].id.toString()).collection("student_marks")
            marksCollection
                    .whereEqualTo("id", students[studentID].id)
                    .get()
                    .addOnSuccessListener { result ->
                        Log.d(FIREBASE_TAG, "--- all marks ---")
                        for (document in result) {
                            //VERBOSE Logcat
                            //Log.d(FIREBASE_TAG, document.toString())
                            val mark = document.toObject<Mark>()
                            Log.d(FIREBASE_TAG, mark.toString())
                            marks.add(mark)
                            (ui.myList.adapter as StudentActivity.MarkAdapter).notifyDataSetChanged()
                        }
                        marks.sortBy { it.week }
                    }

        }

        ui.myList.adapter = MarkAdapter(marks)
        ui.myList.layoutManager = LinearLayoutManager(this)
    }

    inner class MarkHolder(var listViewUI: WeekListItemBinding) : RecyclerView.ViewHolder(listViewUI.root) {}
    inner class MarkAdapter(private val marks: MutableList<Mark>) : RecyclerView.Adapter<StudentActivity.MarkHolder>() {
        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentActivity.MarkHolder {
            val listViewUI = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return MarkHolder(listViewUI)
        }
        //Returns the size of the array
        override fun getItemCount(): Int {
            return marks.size
        }
        //Populates each row
        override fun onBindViewHolder(holder: StudentActivity.MarkHolder, position: Int) {
            val mark = marks[position]
            holder.listViewUI.txtNumber.text = "Week " + mark.week.toString()
            holder.listViewUI.txtMarkingType.text = mark.mark

        }
    }
}