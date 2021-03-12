package com.goel.attendancetracker.downloadmanager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.goel.attendancetracker.classes.ClassesModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadManager {
    public static final String TITLE = "Attendance Tracker";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 600;
    public static final int DOWNLOAD_SUCCESSFUL = 1;
    public static final int DOWNLOAD_FAILED = -1;
    private String filePath;
    private final ClassesModel model;
    PdfDocument document;
    Paint paint;
    PdfDocument.PageInfo pageInfo;
    PdfDocument.Page page;
    Canvas canvas;

    public DownloadManager(ClassesModel model) {
        this.model = model;
        this.setFilePath();
    }

    private void setFilePath(){
        this.filePath = "Attendance Tracker" + "/" + this.model.getClassName() + ".pdf";
    }

    public String getFilePath(){
        return this.filePath;
    }

    public int downloadAttendance(){
        this.initializeDocument();
        File file = new File(Environment.getExternalStorageDirectory(), "/" + this.getFilePath());
        this.createPage();
        try {
            downloadFile(file);
            this.document.close();
            return DOWNLOAD_SUCCESSFUL;
        }
        catch (IOException e) {
            e.printStackTrace();
            this.document.close();
            return DOWNLOAD_FAILED;
        }
    }

    private void initializeDocument(){
        this.document = new PdfDocument();
        this.paint = new Paint();
        this.pageInfo = new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, 1).create();
        this.page = document.startPage(pageInfo);
        this.canvas = page.getCanvas();
    }

    private void createPage()  {
        this.canvas.drawText(TITLE, 10, 10, this.paint);
        this.document.finishPage(this.page);
    }


    private void downloadFile(File file) throws IOException {
        try {
            this.document.writeTo(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            (new File(Environment.getExternalStorageDirectory(), "/Attendance Tracker")).mkdirs();
            this.document.writeTo(new FileOutputStream(file));
        }
    }
}
