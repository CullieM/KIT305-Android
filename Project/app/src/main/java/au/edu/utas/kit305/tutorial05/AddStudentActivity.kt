package au.edu.utas.kit305.tutorial05

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.kit305.tutorial05.databinding.ActivityAddStudentBinding
private lateinit var ui : ActivityAddStudentBinding

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ui = ActivityAddStudentBinding.inflate(layoutInflater)
    setContentView(ui.root)
    }
}