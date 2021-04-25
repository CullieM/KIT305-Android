package au.edu.utas.kit305.tutorial05

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.classes.Week
import au.edu.utas.kit305.tutorial05.databinding.ActivityStudentBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class StudentActivity : AppCompatActivity() {

    private lateinit var ui: ActivityStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //View binding
        ui = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //Retrieve studentID (INDEX) from intent, upon selecting from StudentListFragment
        val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
        var studentObject = students[studentID]

        //Download image from Firebase storage, taken from: https://firebase.google.com/docs/storage/android/download-files#download_in_memory
        var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference.child("studentPictures/" + students[studentID].id + ".jpg")
        mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val takenImage = BitmapFactory.decodeByteArray(it, 0, it.size)
            ui.imageView.setImageBitmap(takenImage)
            Log.d(FIREBASE_TAG, "Downloaded image")
        }.addOnFailureListener {
            Log.d(FIREBASE_TAG, "Failed to download image")
        }

        //Pre-fill the TextViews with relevant student information.
        ui.average.text = AVG
        ui.txtStudentName.text = studentObject.full_name
        ui.txtStudentID.text = studentObject.student_id
        ui.txtOverallMark.text = studentObject.overall_mark.toString() + "%"

        //Button for going to EditStudentActivity, passing the student index.
        ui.btnEdit.setOnClickListener {
            val i = Intent(this, EditStudentActivity::class.java)
            i.putExtra(STUDENT_INDEX, studentID)
            startActivityForResult(i, LAUNCH_SECOND_ACTIVITY)
        }

        //Share in plain text button
        ui.btnShare.setOnClickListener {
            val funClass = TabbedActivity()
            var sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                var textToShare = students[studentID].full_name + "\n"
                textToShare += "Student_ID: " + students[studentID].id + "\n"
                textToShare += "Overall Mark: " + students[studentID].overall_mark + "%\n\n"

                for (i in 0 until marks.size) {
                    val filteredMarkList: List<Week> = weeks.filter { it.number == marks[i].week }
                    textToShare += "Week " + marks[i].week
                    textToShare += ": " + funClass.calculateMark(filteredMarkList[0].marking_type.toString(),marks[i].mark?.toInt()!!) + "\n\n"
                }
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "Share via..."))

        }
        //Clear global variable marks as information may retain from other students.
        //Marks is global for simplicity between StudentActivity and EditStudentActivity.
        marks.clear()

        //Retrieve marks for the relevant student for each week.
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //If user deleted current student from EditStudentActivity, update locally and go back to list.
        if (resultCode == DELETED_CODE) {
            val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
            students.removeAt(studentID)
            students = students.filterNotNull().toMutableList()
            finish()

        } else
        //If user edited student from EditStudentActivity, updated locally.
            if (resultCode == EDITED_CODE) {

                //Retrieve relevant studentID (INDEX) from Intent
                val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
                var studentObject = students[studentID]
                val funClass = TabbedActivity()

                //Redraw the text fields
                ui.txtStudentID.text = studentObject.student_id
                ui.txtStudentName.text = studentObject.full_name
                ui.txtOverallMark.text = studentObject.overall_mark.toString()+"%"

                //Redraw the list of marks.
                ui.myList.adapter?.notifyDataSetChanged()

                //Download updated photo.
                //Taken from: https://firebase.google.com/docs/storage/android/download-files#download_in_memory
                var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference.child("studentPictures/" + students[studentID].id + ".jpg")
                mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    val takenImage = BitmapFactory.decodeByteArray(it, 0, it.size)
                    ui.imageView.setImageBitmap(takenImage)
                    Log.d(FIREBASE_TAG, "Downloaded image")
                }.addOnFailureListener {
                    Log.d(FIREBASE_TAG, "Failed to download image")
                }
            }
    }

    //Adapter for RecyclerView.
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
            val filteredWeekList: List<Week> = weeks.filter { it.number == mark.week }
            val funClass = TabbedActivity()
            holder.listViewUI.txtMarkingType.text = funClass.calculateMark(filteredWeekList[0].marking_type.toString(), mark.mark?.toInt()!!)

        }
    }
}