package com.goel.attendancetracker.models

data class OrganisationsModel (
    var id: Int = 0,
    var organisationName: String = "",
    var organisationAttendancePercentage: Int = 100,
    var requiredAttendance: Int = 100)