package com.example.pawtrack.Pet

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.lifecycle.Lifecycle


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
    fun setArguments(position: Int) {
        ARG_PET_PHOTO_BITMAP_URL = parsedList[position]["p_p"]
        ARG_PET_TRACKER_STATUS = parsedList[position]["t_s"]
        ARG_PET_TRACKER_ID = parsedList[position]["t_i"]
        ARG_PET_ACTIVITY = parsedList[position]["a_c"]
        ARG_PET_NAME = parsedList[position]["n"]
    }
    companion object {
        var ARG_PET_PHOTO_BITMAP_URL: String? = null
        var ARG_PET_TRACKER_STATUS: String? = null
        var ARG_PET_TRACKER_ID: String? = null
        var ARG_PET_ACTIVITY: String? = null
        var ARG_PET_NAME: String? = null
    }
}