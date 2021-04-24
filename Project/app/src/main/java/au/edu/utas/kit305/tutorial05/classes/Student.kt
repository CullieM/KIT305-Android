package au.edu.utas.kit305.tutorial05.classes

import android.os.Parcel
import android.os.Parcelable

class Student (
        var id : String? = null,
        var student_id : String? = null,
        var full_name : String? = null,
        var overall_mark: Int? = null,
        var image: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(student_id)
        parcel.writeString(full_name)
        parcel.writeValue(overall_mark)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Student> {
        override fun createFromParcel(parcel: Parcel): Student {
            return Student(parcel)
        }

        override fun newArray(size: Int): Array<Student?> {
            return arrayOfNulls(size)
        }
    }
}