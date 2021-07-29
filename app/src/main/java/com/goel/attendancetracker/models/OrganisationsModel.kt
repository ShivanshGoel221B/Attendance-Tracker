package com.goel.attendancetracker.models

data class OrganisationsModel (
    var id: Int = 0,
    var name: String = "",
    var attendance: Int = 100,
    var target: Int = 100)