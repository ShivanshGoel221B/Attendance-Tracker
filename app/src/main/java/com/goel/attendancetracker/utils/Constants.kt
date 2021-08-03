package com.goel.attendancetracker.utils

object Constants {
    const val DB_VERSION = 1
    const val DB_NAME = "attendance_db"

    // KEYS
    const val ORGANISATIONS = "_organisations"
    const val TARGET = "target"
    const val ATTENDANCE = "attendance"
    const val NAME = "name"
    const val HISTORY = "history"
    const val ROOT_TABLE =
        "$ORGANISATIONS(s_no INTEGER PRIMARY KEY, $NAME TEXT, $ATTENDANCE INTEGER, $TARGET INTEGER)"
    const val NEW_TABLE =
        "(class_sno INTEGER PRIMARY KEY, $NAME TEXT, $ATTENDANCE INTEGER, $TARGET INTEGER, $HISTORY TEXT)"
    var OPEN_ORG: String? = null

    /////////////////
    const val APP_URL = "https://play.google.com/store/apps/details?id=com.goel.attendancetracker"
    const val DEV_URL = "https://play.google.com/store/apps/dev?id=8638438299858248860"
    const val ABOUT_URL = "https://goelapplications.tech/about"
    const val PRIVACY_POLICY = "https://goelapplications.tech/privacy-policy/project-1"
    const val TERMS = "https://goelapplications.tech/terms/project-1"

    //Validity
    private const val MAX_NAME_LENGTH = 15

    fun getNameValidity(name: String): HashMap<Boolean, String> {
        val validNameChars = "qwertyuiopasdfghjklzxcvbnm "
        val hashMap = HashMap<Boolean, String>()
        when {
            name.isEmpty() -> {
                hashMap[false] = "Name can not be empty"
            }
            name.length > MAX_NAME_LENGTH -> {
                hashMap[false] = "Name length should not exceed $MAX_NAME_LENGTH"
            }
            else -> {
                name.forEach {
                    if (it.lowercase() !in validNameChars) {
                        hashMap[false] = "Name should only contain alphabets and spaces"
                        return hashMap
                    } else
                        hashMap[true] = name.trim()
                }
            }
        }
        return hashMap
    }

    fun getTargetValidity(target: String): HashMap<Boolean, String> {
        val trimmedTarget = target.trim()
        val hashMap = HashMap<Boolean, String>()
        try {
            val organisationTarget = trimmedTarget.toInt()
            if (organisationTarget <= 0 || organisationTarget > 100)
                hashMap[false] = "Please enter a number from 1 to 100"
            else
                hashMap[true] = trimmedTarget
        } catch (e: Exception) {
            hashMap[false] = "Please enter a valid number"
        }
        return hashMap
    }
}