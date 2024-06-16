package com.example.pawtrack.Pet

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PetFragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val parsedList: List<Map<String, String?>>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return parsedList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = FirstFragment()
        fragment.arguments = Bundle().apply {
            putString(ARG_PET_NAME, parsedList[position]["n"])
            putString(ARG_PET_ACTIVITY, parsedList[position]["a_c"])
            putString(ARG_PET_TRACKER_ID, parsedList[position]["t_i"])
            putString(ARG_PET_TRACKER_STATUS, parsedList[position]["t_s"])
            putString(ARG_PET_PHOTO_BITMAP_URL, parsedList[position]["p_p"])
        }
        return fragment
    }

    companion object {
        const val ARG_PET_PHOTO_BITMAP_URL = "pet_photo_bitmap_url"
        const val ARG_PET_TRACKER_STATUS = "pet_tracker_status"
        const val ARG_PET_TRACKER_ID = "pet_tracker_id"
        const val ARG_PET_ACTIVITY = "pet_activity"
        const val ARG_PET_NAME = "pet_name"
    }
}
