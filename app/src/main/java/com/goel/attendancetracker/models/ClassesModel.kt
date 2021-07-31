package com.goel.attendancetracker.models

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class ClassesModel (
    var id: Int = 0,
    var name: String = "",
    var classCounter: String = "0/0",
    var attendance: Int = 100,
    var target: Int = 100,
    var classHistory: String = "{}"
    ){

    // UPDATE NEW ATTENDANCE
    val presentCount: Int
        get() {
            val history: JSONObject = try {
                JSONObject(classHistory)
            } catch (e: JSONException) {
                return 0
            }
            // UPDATE NEW ATTENDANCE
            var present = 0
            try {
                val keys = history.keys()
                while (keys.hasNext()) {
                    val dateData = history[keys.next()] as JSONArray
                    present += dateData.getInt(0)
                }
            } catch (e: JSONException) {
                return 0
            }
            return present
        }

    fun getPresentCount(date: String?): Int {
        val history: JSONObject = try {
            JSONObject(classHistory)
        } catch (e: JSONException) {
            return 0
        }
        // INITIALIZING INITIAL DATE HISTORY
        val dateHistory: JSONArray = try {
            history[date!!] as JSONArray
        } catch (e: Exception) {
            return 0
        }
        return try {
            dateHistory.getInt(0)
        } catch (e: JSONException) {
            0
        }
    }

    val absentCount: Int
        get() {
            val history: JSONObject = try {
                JSONObject(classHistory)
            } catch (e: JSONException) {
                return 0
            }
            var absent = 0
            try {
                val keys = history.keys()
                while (keys.hasNext()) {
                    val dateData = history[keys.next()] as JSONArray
                    absent += dateData.getInt(1)
                }
            } catch (e: JSONException) {
                return 0
            }
            return absent
        }

    fun getAbsentCount(date: String?): Int {
        val history: JSONObject = try {
            JSONObject(classHistory)
        } catch (e: JSONException) {
            return 0
        }
        val dateHistory: JSONArray = try {
            history[date!!] as JSONArray
        } catch (e: Exception) {
            return 0
        }
        return try {
            dateHistory.getInt(1)
        } catch (e: JSONException) {
            0
        }
    }

    fun setClassCounter() {
        val present = presentCount
        val absent = absentCount
        val total = present + absent
        classCounter = "$present/$total"
    }
}