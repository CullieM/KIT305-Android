package au.edu.utas.kit305.tutorial05

import android.os.Parcel
import android.os.Parcelable

class Mark (
        var id : String? = null,
        var week : Int? = null,
        var mark : String? = null
) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(id)
                parcel.writeValue(week)
                parcel.writeString(mark)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<Mark> {
                override fun createFromParcel(parcel: Parcel): Mark {
                        return Mark(parcel)
                }

                override fun newArray(size: Int): Array<Mark?> {
                        return arrayOfNulls(size)
                }
        }
}
