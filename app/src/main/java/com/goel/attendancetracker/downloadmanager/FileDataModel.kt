package com.goel.attendancetracker.downloadmanager

import android.graphics.*
import com.goel.attendancetracker.models.ClassesModel
import com.google.android.gms.common.util.ArrayUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

abstract class FileDataModel {
    fun createFirstPage(manager: ClassDownloadManager) {
        val paint = manager.paint
        val canvas = manager.canvas
        val attendance = manager.model.attendance
        val target = manager.model.target
        val color = if (attendance >= target) safeColor else if (attendance > target * 0.75f) lowColor else dangerColor

        // SET IMAGE
        canvas.drawBitmap(logo, 10f, 15f, paint)

        // SET TITLE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = TITLE_SIZE.toFloat()
        paint.typeface = Typeface.create(fonts[2], Typeface.NORMAL)
        canvas.drawText(TITLE, 135f, 76f, paint)

        //SET TAG LINE
        paint.textSize = TEXT_SIZE_SMALL.toFloat()
        paint.typeface = Typeface.create(fonts[3], Typeface.NORMAL)
        canvas.drawText("Track Your Attendance With Ease", 135f, 108f, paint)
        paint.strokeWidth = 4f
        canvas.drawLine(10f, 130f, (manager.pageInfo.pageWidth - 10).toFloat(), 130f, paint)

        // SET ORGANISATION NAME
        paint.textSize = 36f
        paint.isUnderlineText = true
        paint.typeface = Typeface.create(fonts[4], Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            manager.organisationName.uppercase(Locale.getDefault()), manager.pageInfo.pageWidth
                .toFloat() / 2, 185f, paint
        )
        paint.isUnderlineText = false

        //SET NAME AND TARGET
        paint.textSize = TITLE_NAME_SIZE.toFloat()
        paint.typeface = Typeface.create(fonts[3], Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subject: ", 150f, 240f, paint)
        canvas.drawText("Target: ", 150f, 290f, paint)
        canvas.drawText("Attendance: ", 200f, 360f, paint)
        paint.typeface = Typeface.create(fonts[3], Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(manager.model.name.uppercase(Locale.getDefault()), 170f, 240f, paint)
        paint.typeface = Typeface.create(fonts[2], Typeface.BOLD)
        canvas.drawText("$target%", 170f, 290f, paint)

        // SET ATTENDANCE
        val total = manager.model.presentCount + manager.model.absentCount
        val status = manager.model.presentCount.toString() + "/" + total
        paint.typeface = Typeface.create(fonts[2], Typeface.BOLD)
        canvas.drawText(status, 220f, 360f, paint)
        paint.color = color
        paint.textSize = 96f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$attendance%", manager.pageInfo.pageWidth.toFloat() / 2, 470f, paint)
    }

    private fun createLastPage(manager: ClassDownloadManager) {
        manager.addPage(true)
        manager.paint.textAlign = Paint.Align.LEFT
        manager.paint.textSize = TITLE_NAME_SIZE.toFloat()
        manager.paint.color = Color.rgb(0, 0, 0)
        manager.canvas.drawText("This data is generated by: ", 50f, 100f, manager.paint)
        manager.paint.textSize = 40f
        manager.paint.color = Color.rgb(0, 104, 205)
        manager.canvas.drawBitmap(logo, 50f, 130f, manager.paint)
        manager.canvas.drawText("Attendance Tracker", 170f, 190f, manager.paint)
        manager.document?.finishPage(manager.page)
    }

    fun writeAttendance(manager: ClassDownloadManager) {
        val years = getSortedYears(manager)
        val months = getSortedMonths(manager, years)
        for (year in years) {
            for (month in months[year]!!) {
                val monthName = monthsArray[month]
                manager.addPage(false)
                manager.canvas.drawBitmap(table, 20f, 190f, manager.paint)
                val presentArray = getMonthData(manager.model, year, month, PRESENT)
                val absentArray = getMonthData(manager.model, year, month, ABSENT)
                setMonthTitle(
                    manager.canvas,
                    manager.paint,
                    year,
                    monthName,
                    sumOf(presentArray),
                    sumOf(absentArray)
                )
                writeAttendanceArray(manager.canvas, manager.paint, presentArray, absentArray)
                manager.document?.finishPage(manager.page)
            }
        }
        createLastPage(manager)
    }

    private fun setMonthTitle(
        canvas: Canvas,
        paint: Paint,
        year: Int,
        month: String,
        presents: Int,
        absents: Int
    ) {
        val total = presents + absents
        val percentage = presents * 100 / total
        paint.typeface = Typeface.create(fonts[3], Typeface.BOLD)
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(0, 0, 0)
        canvas.drawText("$month, $year", 40f, 50f, paint)
        paint.textSize = 36f
        canvas.drawText("This Month: ", 120f, 125f, paint)
        paint.typeface = Typeface.create(fonts[2], Typeface.NORMAL)
        canvas.drawText("$presents/$total ($percentage%)", 330f, 125f, paint)
    }

    private fun writeAttendanceArray(
        canvas: Canvas,
        paint: Paint,
        presentArray: IntArray,
        absentArray: IntArray
    ) {
        val xPresent = 185
        val xAbsent = 303
        val yCommon = 307
        val xOffset = 360
        val yOffset = 58
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 46f
        var x: Int
        var y: Int
        // SET PRESENTS
        x = xPresent
        y = yCommon
        paint.color = safeColor
        for (i in presentArray.indices) {
            if (i == 15) {
                x = xPresent + xOffset
                y = yCommon
            }
            canvas.drawText(presentArray[i].toString(), x.toFloat(), y.toFloat(), paint)
            y += yOffset
        }

        //SET ABSENTS
        x = xAbsent
        y = yCommon
        paint.color = dangerColor
        for (i in absentArray.indices) {
            if (i == 15) {
                x = xAbsent + xOffset
                y = yCommon
            }
            canvas.drawText(absentArray[i].toString(), x.toFloat(), y.toFloat(), paint)
            y += yOffset
        }
    }

    private fun getMonthData(model: ClassesModel, year: Int, month: Int, index: Int): IntArray {
        val data = IntArray(31)
        Arrays.fill(data, 0)
        val query = getFormattedValue(year) + getFormattedValue(month)
        val history: JSONObject = try {
            JSONObject(model.classHistory)
        } catch (e: JSONException) {
            return data
        }
        try {
            val keys = history.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.substring(0, 6) == query) {
                    val date = key.substring(6).toInt()
                    val dateData = history[key] as JSONArray
                    data[date - 1] = dateData.getInt(index)
                }
            }
        } catch (e: JSONException) {
            return data
        }
        return data
    }

    private fun getSortedMonths(
        manager: ClassDownloadManager,
        years: IntArray
    ): HashMap<Int, IntArray> {
        val months = HashMap<Int, IntArray>()
        val history: JSONObject = try {
            JSONObject(manager.model.classHistory)
        } catch (e: JSONException) {
            return months
        }
        for (year in years) {
            val tempMonths = HashSet<Int>()
            val keys = history.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.substring(0, 4).toInt() == year) {
                    tempMonths.add(Integer.valueOf(key.substring(4, 6)))
                }
            }
            val yearData = ArrayUtils.toPrimitiveArray(tempMonths)
            sort(yearData)
            months[year] = yearData
        }
        return months
    }

    private fun getSortedYears(manager: ClassDownloadManager): IntArray {
        val tempSet = HashSet<Int>()
        val history: JSONObject = try {
            JSONObject(manager.model.classHistory)
        } catch (e: JSONException) {
            return intArrayOf()
        }
        val keys = history.keys()
        while (keys.hasNext()) {
            val year = keys.next().substring(0, 4)
            tempSet.add(Integer.valueOf(year))
        }
        val years = ArrayUtils.toPrimitiveArray(tempSet)
        sort(years)
        return years
    }

    private fun sort(array: IntArray) {
        for (i in 0 until array.size - 1) {
            if (array[i] > array[i + 1]) {
                val temp = array[i]
                array[i] = array[i + 1]
                array[i + 1] = temp
            }
        }
    }

    private fun sumOf(array: IntArray): Int {
        var sum = 0
        for (item in array) sum += item
        return sum
    }

    private fun getFormattedValue(value: Int): String {
        return if (value >= 10) value.toString() else "0$value"
    }

    companion object {
        val monthsArray = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        const val TITLE = "Attendance Tracker"
        private const val TITLE_SIZE = 54
        private const val TITLE_NAME_SIZE = 34
        private const val TEXT_SIZE_SMALL = 22
        private const val PRESENT = 0
        private const val ABSENT = 1
        lateinit var fonts: Array<Typeface?>
        lateinit var logo: Bitmap
        lateinit var table: Bitmap
        private val safeColor = Color.rgb(4, 170, 81)
        private val lowColor = Color.rgb(245, 102, 0)
        private val dangerColor = Color.rgb(255, 7, 58)
    }
}