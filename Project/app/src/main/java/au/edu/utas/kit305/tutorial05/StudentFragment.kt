package au.edu.utas.kit305.tutorial05

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.databinding.FragmentStudentBinding
import au.edu.utas.kit305.tutorial05.databinding.StudentListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentFragment : Fragment() {
    val students = mutableListOf<Student>(
        Student(full_name = "Yasir Griffiths", student_id = "000001", overall_mark = 100),
        Student(full_name = "Stuart Calhoun", student_id = "000002", overall_mark = 100),
        Student(full_name = "Sonny Parsons", student_id = "000003", overall_mark = 100),
        Student(full_name = "Dannielle Dowling", student_id = "000004", overall_mark = 100),
        Student(full_name = "Anjali Seymour", student_id = "000005", overall_mark = 100)
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var inflatedView = FragmentStudentBinding.inflate(layoutInflater, container, false)

        //db setup
        val db = Firebase.firestore
        var studentCollection = db.collection("students")


        inflatedView.myList.adapter = StudentAdapter(students)
        inflatedView.myList.layoutManager = LinearLayoutManager(context!!)
        return inflatedView.root
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
            holder.ui.txtName.text = student.full_name
            holder.ui.txtStudentID.text = student.student_id
            holder.ui.txtOverallMark.text = student.overall_mark.toString()
        }
    }
}
