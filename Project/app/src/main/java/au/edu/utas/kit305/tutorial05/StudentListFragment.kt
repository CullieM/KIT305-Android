package au.edu.utas.kit305.tutorial05

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.databinding.FragmentStudentBinding
import au.edu.utas.kit305.tutorial05.databinding.StudentListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

const val STUDENT_INDEX = "Student_Index"
const val ADD_STUDENT_CODE = 19
var students = mutableListOf<Student>()

private lateinit var inflatedView : FragmentStudentBinding
private lateinit var ui : FragmentStudentBinding

class StudentListFragment : Fragment() {


    override fun onResume() {
        super.onResume()

        inflatedView.myList.adapter?.notifyDataSetChanged()
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        inflatedView = FragmentStudentBinding.inflate(layoutInflater, container, false)
        inflatedView.myList.adapter = StudentAdapter(students)
        inflatedView.myList.layoutManager = LinearLayoutManager(this.activity)
        return inflatedView.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //add new student button
        //TODO THIS IS BROKEN

        //db setup
        val db = Firebase.firestore
        var studentsCollection = db.collection("students")
        studentsCollection
                .get()
                .addOnSuccessListener { result ->
                    Log.d(FIREBASE_TAG, "--- all students ---")
                    for (document in result)
                    {
                        //Log.d(FIREBASE_TAG, document.toString())
                        val student = document.toObject<Student>()
                        Log.d(FIREBASE_TAG, student.toString())
                        students.add(student)
                        (inflatedView.myList.adapter as StudentListFragment.StudentAdapter).notifyDataSetChanged()
                    }
                    students.sortBy{ it.student_id?.toInt() }
                }
    }

    inner class StudentHolder(var ui: StudentListItemBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class StudentAdapter(private val students: MutableList<Student>) : RecyclerView.Adapter<StudentHolder>() {
        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
            val ui = StudentListItemBinding.inflate(layoutInflater, parent, false)
            return StudentHolder(ui)
        }
        //Returns the size of the array
        override fun getItemCount(): Int {
            return students.size
        }
        //Populates each row
        override fun onBindViewHolder(holder: StudentHolder, position: Int) {
            val student = students[position]
            holder.ui.txtName.text = student.full_name + ","
            holder.ui.txtStudentID.text = student.student_id
            holder.ui.txtOverallMark.text = student.overall_mark.toString()

            holder.ui.root.setOnClickListener {
                var i = Intent(holder.ui.root.context, StudentActivity::class.java)
                i.putExtra(STUDENT_INDEX, position)
                startActivity(i)
            }
        }
    }

}
