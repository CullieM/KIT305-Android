package au.edu.utas.kit305.tutorial05

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import au.edu.utas.kit305.tutorial05.databinding.FragmentStudentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var inflatedView = FragmentStudentBinding.inflate(layoutInflater, container, false)
        val db = Firebase.firestore
        var studentCollection = db.collection("students")
        return inflatedView.root
    }
}
