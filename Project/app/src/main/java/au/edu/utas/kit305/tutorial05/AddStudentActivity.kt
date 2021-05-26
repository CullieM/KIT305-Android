package au.edu.utas.kit305.tutorial05

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.classes.Student
import au.edu.utas.kit305.tutorial05.databinding.ActivityAddStudentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

private lateinit var ui : ActivityAddStudentBinding

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ui = ActivityAddStudentBinding.inflate(layoutInflater)
    setContentView(ui.root)

        //Button to take a picture.
        //Image code taken from https://www.youtube.com/watch?v=DPHkhamDoyc
        ui.imageButton2.setOnClickListener{
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, IMAGE_CODE)
            }else{
            }
        }

        //Save button
        ui.btnSave.setOnClickListener {
            //Get the user input for a blank student object.
            val inputID = ui.editStudentID.text.toString()
            val inputName = ui.editName.text.toString()
            val newStudent = Student(
                    id = inputID,
                    student_id = inputID,
                    full_name = inputName,
                    overall_mark = 0,
                    image = ""
            )
            //Create a blank Mark object to be set for each week for the new student.
            val db = Firebase.firestore
            var studentsCollection = db.collection("students")
            val blankMark = Mark(
                    id = inputID,
                    week = -1,
                    mark = "0"
            )
            //Update the database
            studentsCollection.document(inputID)
                .set(newStudent)
                .addOnSuccessListener {
                    Log.d(FIREBASE_TAG, "Successfully added student $inputName")
                    //Update local students
                    students.add(newStudent)
                    //Set the students mark in each week to be 0.
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
                    var intent = Intent(this, StudentActivity::class.java)
                    intent.putExtra(STUDENT_INDEX, inputID)
                    setResult(ADD_STUDENT_CODE, intent)
                    finish()
                }
            //Add a Firebase storage image for the new student.
            //Path to store image.
            val studentPictureRef : StorageReference = FirebaseStorage.getInstance().reference.child("studentPictures/" + ui.editStudentID.text + ".jpg")
            //Taken from https://firebase.google.com/docs/storage/android/upload-files#upload_from_data_in_memory
            if(ui.imageView2.drawable != null){
                val bitmap = (ui.imageView2.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                var uploadTask = studentPictureRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                    Log.d(FIREBASE_TAG,"Failed to upload image")
                }.addOnSuccessListener { _ ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Log.d(FIREBASE_TAG,"Uploaded image")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //After taking a picture.
        if(requestCode == IMAGE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //Intentionally using thumbnail sized intent data for potential storage limitations
            //Place photo into imageView.
            val takenImage = data.extras?.get("data") as Bitmap
            ui.imageView2.setImageBitmap(takenImage)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}