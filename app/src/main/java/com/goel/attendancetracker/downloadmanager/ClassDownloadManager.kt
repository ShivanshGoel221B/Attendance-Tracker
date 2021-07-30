package com.goel.attendancetracker.downloadmanager

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Environment
import com.goel.attendancetracker.models.ClassesModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ClassDownloadManager(// ===================== GETTERS AND SETTERS ===================== //
    val model: ClassesModel, val organisationName: String
) : FileDataModel() {
    val filePath = "Attendance Tracker/$organisationName/${model.className}.pdf"
    var document: PdfDocument? = null
        private set
    lateinit var paint: Paint
        private set
    lateinit var pageInfo: PageInfo
        private set
    lateinit var page: PdfDocument.Page
        private set
    private var pageNumber = 0
    lateinit var canvas: Canvas
        private set

    fun downloadAttendance(): Int {
        initializeDocument()
        val file = File(Environment.getExternalStorageDirectory(), "/$filePath")
        addPage(false)
        writeToPage()
        return try {
            downloadFile(file)
            document!!.close()
            DOWNLOAD_SUCCESSFUL
        } catch (e: IOException) {
            e.printStackTrace()
            document!!.close()
            DOWNLOAD_FAILED
        }
    }

    private fun initializeDocument() {
        document = PdfDocument()
        paint = Paint()
    }

    fun addPage(isLastPage: Boolean) {
        pageNumber++
        val height: Int = when {
            pageNumber == 1 -> {
                TITLE_PAGE_HEIGHT
            }
            isLastPage -> {
                Last_PAGE_HEIGHT
            }
            else -> {
                HEIGHT
            }
        }
        pageInfo = PageInfo.Builder(WIDTH, height, pageNumber).create()
        page = document!!.startPage(pageInfo)
        canvas = page.canvas
    }

    private fun writeToPage() {
        createFirstPage(this)
        document!!.finishPage(page)
        writeAttendance(this)
    }

    @Throws(IOException::class)
    private fun downloadFile(file: File) {
        try {
            document!!.writeTo(FileOutputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            File(
                Environment.getExternalStorageDirectory(),
                "/Attendance Tracker/$organisationName"
            )
                .mkdirs()
            document!!.writeTo(FileOutputStream(file))
        }
    }

    companion object {
        private const val WIDTH = 740
        private const val HEIGHT = 1300
        private const val TITLE_PAGE_HEIGHT = 550
        private const val Last_PAGE_HEIGHT = 300
        const val DOWNLOAD_SUCCESSFUL = 1
        const val DOWNLOAD_FAILED = -1
    }
}