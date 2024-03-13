package com.example.pawtrack

import android.os.AsyncTask
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
class OverpassQueryTask(private val listener: OverpassQueryListener) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    interface OverpassQueryListener {
        fun onPlaceFound(placeInfo: JSONObject)
    }

    override fun doInBackground(vararg urls: String): ArrayList<JSONObject> {
        val url = URL(urls[0])
        val urlConnection = url.openConnection() as HttpURLConnection
        val inputStreamReader = InputStreamReader(urlConnection.inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        val response = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            response.append(line)
        }

        bufferedReader.close()
        inputStreamReader.close()

        return parseResponse(response.toString())
    }

    override fun onPostExecute(result: ArrayList<JSONObject>) {
        super.onPostExecute(result)
        for (placeInfo in result) {
            listener.onPlaceFound(placeInfo)
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
