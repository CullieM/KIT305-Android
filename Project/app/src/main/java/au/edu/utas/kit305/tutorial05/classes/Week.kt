package au.edu.utas.kit305.tutorial05.classes

import android.os.Parcel
import android.os.Parcelable

class Week (
        var id : String? = null,
        var number : Int? = null,
        var marking_type : String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeValue(number)
        parcel.writeString(marking_type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Week> {
        override fun createFromParcel(parcel: Parcel): Week {
            return Week(parcel)
        }

        override fun newArray(size: Int): Array<Week?> {
            return arrayOfNulls(size)
        }
    }
}
