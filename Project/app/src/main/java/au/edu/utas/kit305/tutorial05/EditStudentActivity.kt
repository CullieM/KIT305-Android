package au.edu.utas.kit305.tutorial05

import android.Manifest.permission.CAMERA
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.kit305.tutorial05.classes.Mark
import au.edu.utas.kit305.tutorial05.databinding.ActivityEditStudentBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min


class EditStudentActivity : AppCompatActivity() {
    private lateinit var ui: ActivityEditStudentBinding
    private lateinit var currentPhotoPath: String

    //TODO POTENTIALLY UNUSED CAMERA FUNCTIONALITY FROM KIT305
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    //TODO POTENTIALLY UNUSED CAMERA FUNCTIONALITY FROM KIT305
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestToTakeAPicture() {
        requestPermissions(
                arrayOf(CAMERA),
                REQUEST_IMAGE_CAPTURE
        )
    }

    //TODO POTENTIALLY UNUSED CAMERA FUNCTIONALITY FROM KIT305
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takeAPicture()
                } else {
                    Toast.makeText(
                            this,
                            "Cannot access camera, permission denied",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //TODO POTENTIALLY UNUSED CAMERA FUNCTIONALITY FROM KIT305
    private fun takeAPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            try {
                val photoFile: File = createImageFile()!!
                val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "au.edu.utas.kit305.tutorial5",
                        photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
            }
        }
    }

    //TODO POTENTIALLY UNUSED CAMERA FUNCTIONALITY FROM KIT305
    private fun setPic(imageView: ImageView) {
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //Initialise the DB
        val db = Firebase.firestore
        var studentCollection = db.collection("students")

        //Get relevant student information from intent index.
        val studentID = intent.getIntExtra(STUDENT_INDEX, -1)
        var studentObject = students[studentID]

        //Fill textViews with existing student information.
        ui.txtStudentName.setText(studentObject.full_name)
        ui.txtStudentID.setText(studentObject.student_id)

        //Download their image, set it in the imageView. Taken from: https://firebase.google.com/docs/storage/android/download-files#download_in_memory
        var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference.child("studentPictures/" + students[studentID].id + ".jpg")
        mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val takenImage = BitmapFactory.decodeByteArray(it, 0, it.size)
            ui.imageView.setImageBitmap(takenImage)
            Log.d(FIREBASE_TAG, "Downloaded image")
        }.addOnFailureListener {
            Log.d(FIREBASE_TAG, "Failed to download image")
        }

        //Button for saving all the information entered by the user, including photo, name, id, and marks.
        ui.btnSave.setOnClickListener {

            //get the user input
            studentObject.full_name = ui.txtStudentName.text.toString()
            studentObject.student_id = ui.txtStudentID.text.toString()

            //update the local student object
            students[studentID].full_name = ui.txtStudentName.text.toString()
            students[studentID].student_id = ui.txtStudentID.text.toString()

            //Update DB
            var calculateMean = 0
            for (i in 0 until weeks.size) {
                db.collection("weeks")
                        .document(weeks[i].id.toString())
                        .collection("student_marks")
                        .document(studentObject.id.toString())
                        .set(marks[i])
                //Sum total marks for mean mark
                calculateMean += marks[i].mark?.toInt()!!
            }
            //Divide by total weeks to get mean mark
            calculateMean /= weeks.size

            //Update overall mark for the student locally
            students[studentID].overall_mark = calculateMean

            //update the database student document for overall_mark, full_name, id
            studentObject.overall_mark = calculateMean
            studentCollection.document(studentObject.id.toString())
                    .set(studentObject)
                    .addOnSuccessListener {
                        Log.d(FIREBASE_TAG, "Successfully updated student ${studentObject?.id}")
                    }

            //Uploading an image. Taken from: https://firebase.google.com/docs/storage/android/upload-files#upload_from_data_in_memory
            var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference;
            var param: String = "studentPictures/" + students[studentID].id + ".jpg"
            val studentPictureRef = mStorageRef.child(param)
            if (ui.imageView.drawable != null) {
                val bitmap = (ui.imageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                var uploadTask = studentPictureRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.d(FIREBASE_TAG, "Failed to upload image")
                }.addOnSuccessListener { taskSnapshot ->
                    Log.d(FIREBASE_TAG, "Uploaded image")
                }
            }
            var i = Intent(this, StudentActivity::class.java)
            i.putExtra(STUDENT_INDEX, studentID)
            setResult(EDITED_CODE, i)
            finish()
        }

        //Image code taken from https://www.youtube.com/watch?v=DPHkhamDoyc
        ui.imageButton.setOnClickListener {
            //TODO: PERMISSIONS
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, IMAGE_CODE)
            } else {

            }
        }

        //Deleting a Student
        ui.btnDeleteStudent.setOnClickListener {
            val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {

                                //Delete Student from Students collection
                                studentCollection.document(students[studentID].id.toString())
                                        .delete()
                                        .addOnSuccessListener { Log.d(FIREBASE_TAG, "Student successfully deleted!") }
                                        .addOnFailureListener { e -> Log.w(FIREBASE_TAG, "Error deleting student", e) }

                                //Delete Marks data from weeks->student_marks collection
                                for (i in 0 until weeks.size) {
                                    db.collection("weeks")
                                            .document(weeks[i].id.toString())
                                            .collection("student_marks")
                                            .document(students[studentID].id.toString())
                                            .delete()
                                            .addOnSuccessListener { Log.d(FIREBASE_TAG, "Mark successfully deleted") }
                                            .addOnFailureListener { e -> Log.w(FIREBASE_TAG, "Error deleting mark", e) }
                                }
                                var intent = Intent(this, StudentActivity::class.java)
                                intent.putExtra(STUDENT_INDEX, studentID)
                                setResult(DELETED_CODE, intent)
                                finish()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                            }
                        }
                    }
            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Delete " + students[studentID].full_name + "?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
        }

        ui.myList.adapter = MarkAdapter(marks, this)
        ui.myList.layoutManager = LinearLayoutManager(this)
    }

    //Image code taken from https://www.youtube.com/watch?v=DPHkhamDoyc
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //TODO POTENTIALLY UNUSED LECTURE CODE
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic(ui.imageView)
        } else if (requestCode == IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            //Intentionally using thumbnail sized intent data for potential storage limitations
            //Retrieve the image taken and use it in imageView.
            val takenImage = data?.extras?.get("data") as Bitmap
            ui.imageView.setImageBitmap(takenImage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class MarkHolder(var listViewUI: WeekListItemBinding) : RecyclerView.ViewHolder(listViewUI.root) {}
    inner class MarkAdapter(private val marks: MutableList<Mark>, private val context: Context) : RecyclerView.Adapter<EditStudentActivity.MarkHolder>() {

        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditStudentActivity.MarkHolder {
            val listViewUI = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return MarkHolder(listViewUI)
        }

        //Returns the size of the array
        override fun getItemCount(): Int {
            return marks.size
        }

        //Populates each row
        override fun onBindViewHolder(holder: EditStudentActivity.MarkHolder, position: Int) {
            val mark = marks[position]
            holder.listViewUI.txtNumber.text = "Week " + mark.week.toString()
            holder.listViewUI.txtMarkingType.text = mark.mark

            //EditText inside an AlertDialog taken from https://handyopinion.com/show-alert-dialog-with-an-input-field-edittext-in-android-kotlin/
            holder.listViewUI.root.setOnClickListener {
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.hint = mark.mark

                val dialogClickListener =
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    var inputText = input.text.toString()
                                    holder.listViewUI.txtMarkingType.text = inputText
                                    marks[position].mark = inputText
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {

                                }
                            }
                        }

                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(context)
                builder.setView(input)
                builder.setTitle("Edit mark for Week " + mark.week.toString())
                        .setPositiveButton("Save", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener)
                        .show()
            }

        }
    }
}