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
import au.edu.utas.kit305.tutorial05.classes.Week
import au.edu.utas.kit305.tutorial05.databinding.FragmentWeeksBinding
import au.edu.utas.kit305.tutorial05.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class WeeksListFragment : Fragment() {
    private lateinit var inflatedView: FragmentWeeksBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflatedView = FragmentWeeksBinding.inflate(layoutInflater, container, false)
        weeks.clear()
        //db setup
        val db = Firebase.firestore
        var weeksCollection = db.collection("weeks")
        weeksCollection
            .get()
            .addOnSuccessListener { result ->
                Log.d(FIREBASE_TAG, "--- all weeks ---")
                for (document in result)
                {
                    //Log.d(FIREBASE_TAG, document.toString())
                    val student = document.toObject<Week>()
                    Log.d(FIREBASE_TAG, student.toString())

                    weeks.add(student)
                    (inflatedView.myList.adapter as WeeksListFragment.WeekAdapter).notifyDataSetChanged()
                }
                weeks.sortBy { it.number }
            }
        inflatedView.myList.adapter = WeekAdapter(weeks)
        inflatedView.myList.layoutManager = LinearLayoutManager(this.activity)
        return inflatedView.root
    }
    override fun onResume() {
        super.onResume()
        inflatedView.myList.adapter?.notifyDataSetChanged()
    }
    inner class WeekHolder(var ui: WeekListItemBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class WeekAdapter(private val weeks: MutableList<Week>) : RecyclerView.Adapter<WeekHolder>() {
        //Inflates a new row, and wraps it in a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekHolder {
            val ui = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return WeekHolder(ui)
        }
        //Returns the size of the array
        override fun getItemCount(): Int {
            return weeks.size
        }
        //Populates each row
        override fun onBindViewHolder(holder: WeekHolder, position: Int) {
            val week = weeks[position]
            holder.ui.txtNumber.text = "Week " + week.number.toString()
            holder.ui.txtMarkingType.text = week.marking_type

            holder.ui.root.setOnClickListener {
                var i = Intent(holder.ui.root.context, WeekActivity::class.java)
                i.putExtra(WEEK_INDEX, position)
                startActivity(i)
            }
        }
    }
}