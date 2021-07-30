package com.goel.attendancetracker.database

object Params {
    const val DB_VERSION = 1
    const val DB_NAME = "attendance_db"

    // KEYS
    const val ORGANISATIONS = "_organisations"
    const val TARGET = "target"
    const val ATTENDANCE = "attendance"
    const val NAME = "name"
    const val HISTORY = "history"
    const val ROOT_TABLE = "$ORGANISATIONS(s_no INTEGER PRIMARY KEY, $NAME TEXT, $ATTENDANCE INTEGER, $TARGET INTEGER)"
    const val NEW_TABLE = "(class_sno INTEGER PRIMARY KEY, $NAME TEXT, $ATTENDANCE INTEGER, $TARGET INTEGER, $HISTORY TEXT)"
    var OPEN_ORG: String? = null
    const val CLASS_DATA_ARRAY = "com.goel.attendancetracker.database.classDataArray"
}