package com.example.pawtrack.Pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.pawtrack.R


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
public const val ARG_PET_NAME = "pet_name"
public const val ARG_PET_ACTIVITY = "pet_activity"
public const val ARG_PET_TRACKER_ID = "pet_tracker_id"
public const val ARG_PET_TRACKER_STATUS = "pet_tracker_status"
public const val ARG_PET_PHOTO_BITMAP_URL = "pet_photo_bitmap_url"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment() {
    private var petName: String? = null
    private var petActivity: String? = null
    private var petTrackerID: String? = null
    private var petTrackerStatus: String? = null
    private var petPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            petName = it.getString(PetFragmentPageAdapter.ARG_PET_NAME)
            petActivity = it.getString(PetFragmentPageAdapter.ARG_PET_ACTIVITY)
            petTrackerID = it.getString(PetFragmentPageAdapter.ARG_PET_TRACKER_ID)
            petTrackerStatus = it.getString(PetFragmentPageAdapter.ARG_PET_TRACKER_STATUS)
            petPhotoUrl = it.getString(PetFragmentPageAdapter.ARG_PET_PHOTO_BITMAP_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_detail, container, false)
        view.findViewById<TextView>(R.id.PetNameText).text = petName
        val petActivityMap = mapOf(
            1 to "Very active",
            2 to "Active",
            3 to "Normal",
            4 to "Inactive",
            5 to "Very inactive"
        )
        val petActivityWord = petActivityMap[petActivity?.toInt()] ?: "Unknown"
        view.findViewById<TextView>(R.id.PetActivityText).text = petActivityWord
        if (petTrackerID == "0" || petTrackerID.isNullOrEmpty()){
            view.findViewById<TextView>(R.id.PetTracketText).visibility = View.GONE
            view.findViewById<TextView>(R.id.trackIdLabel).visibility = View.GONE
        }
        else{
            view.findViewById<TextView>(R.id.PetTracketText).text = petTrackerID
        }


        petPhotoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .into(view.findViewById<ImageView>(R.id.PetPhotoImage))
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */

        const val ARG_PET_NAME = "pet_name"
        const val ARG_PET_ACTIVITY = "pet_activity"
        const val ARG_PET_TRACKER_ID = "pet_tracker_id"
        const val ARG_PET_TRACKER_STATUS = "pet_tracker_status"
        const val ARG_PET_PHOTO_BITMAP_URL = "pet_photo_bitmap_url"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString(PetFragmentPageAdapter.ARG_PET_NAME, petName)
                    putString(PetFragmentPageAdapter.ARG_PET_ACTIVITY, petActivity)
                    putString(PetFragmentPageAdapter.ARG_PET_TRACKER_ID, petTrackerID)
                    putString(PetFragmentPageAdapter.ARG_PET_TRACKER_STATUS, petTrackerStatus)
                    putString(PetFragmentPageAdapter.ARG_PET_PHOTO_BITMAP_URL, petPhotoUrl)
                }
            }
    }
}