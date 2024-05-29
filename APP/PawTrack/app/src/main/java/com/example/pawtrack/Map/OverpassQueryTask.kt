package com.example.pawtrack.Map

import android.os.AsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
class OverpassQueryTask(private val listener: OverpassQueryListener, private val iconResource: Int) : AsyncTask<Pair<String, Int>, Void, ArrayList<JSONObject>>() {


    interface OverpassQueryListener {
        fun onPlaceFound(placeInfo: JSONObject, ic: Int)
    }


    override fun doInBackground(vararg urlIconPairs: Pair<String, Int>): ArrayList<JSONObject> {
        val places = ArrayList<JSONObject>()

        for ((url, iconResource) in urlIconPairs) {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            val inputStreamReader = InputStreamReader(urlConnection.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val response = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                response.append(line)
            }

            bufferedReader.close()
            inputStreamReader.close()

            places.addAll(parseResponse(response.toString()))
        }

        return places
    }

    override fun onPostExecute(result: ArrayList<JSONObject>) {
        super.onPostExecute(result)
        for (placeInfo in result) {
            // Pass the resource ID of the marker icon as the second parameter
            listener.onPlaceFound(placeInfo, iconResource)
        }
    }

    private fun parseResponse(response: String): ArrayList<JSONObject> {
        val Places = ArrayList<JSONObject>()

        val jsonObject = JSONObject(response)
        val elementsArray = jsonObject.getJSONArray("elements")

        for (i in 0 until elementsArray.length()) {
            val element = elementsArray.getJSONObject(i)
            Places.add(element)
        }

        return Places
    }
}
