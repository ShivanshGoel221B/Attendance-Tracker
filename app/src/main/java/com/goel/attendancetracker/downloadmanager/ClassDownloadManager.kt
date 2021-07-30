package com.goel.attendancetracker.downloadmanager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.goel.attendancetracker.models.ClassesModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClassDownloadManager extends FileDataModel {
    private static final int WIDTH = 740;
    private static final int HEIGHT = 1300;
    private static final int TITLE_PAGE_HEIGHT = 550;
    private static final int Last_PAGE_HEIGHT = 300;
    public static final int DOWNLOAD_SUCCESSFUL = 1;
    public static final int DOWNLOAD_FAILED = -1;
    private String filePath;
    private final ClassesModel model;
    private PdfDocument document;
    private Paint paint;
    private PdfDocument.PageInfo pageInfo;
    private PdfDocument.Page page;
    private int pageNumber;
    private Canvas canvas;
    private final String organisationName;

    public ClassDownloadManager(ClassesModel model, String organisationName) {
        this.model = model;
        this.organisationName = organisationName;
        this.pageNumber = 0;
        this.setFilePath();
    }

    private void setFilePath(){
        this.filePath = "Attendance Tracker/" + this.getOrganisationName() + "/" + this.model.getClassName() + ".pdf";
    }

    public String getFilePath(){
        return this.filePath;
    }

    public int downloadAttendance(){
        this.initializeDocument();
        File file = new File(Environment.getExternalStorageDirectory(), "/" + this.getFilePath());
        this.addPage(false);
        this.writeToPage();
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
    }

    public void addPage(boolean isLastPage)  {
        this.pageNumber++;
        int height;
        if (this.pageNumber == 1){
            height = TITLE_PAGE_HEIGHT;
        } else if (isLastPage){
            height = Last_PAGE_HEIGHT;
        } else {
            height = HEIGHT;
        }
        this.pageInfo = new PdfDocument.PageInfo.Builder(WIDTH, height, this.pageNumber).create();
        this.page = document.startPage(pageInfo);
        this.canvas = page.getCanvas();
    }

    private void writeToPage(){
        this.createFirstPage(this);
        this.document.finishPage(this.page);
        this.writeAttendance(this);
    }

    private void downloadFile(File file) throws IOException {
        try {
            this.document.writeTo(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            (new File(Environment.getExternalStorageDirectory(), "/Attendance Tracker/" + this.getOrganisationName())).mkdirs();
            this.document.writeTo(new FileOutputStream(file));
        }
    }

    // ===================== GETTERS AND SETTERS ===================== //

    public ClassesModel getModel() {
        return model;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public PdfDocument getDocument() {
        return document;
    }

    public Paint getPaint() {
        return paint;
    }

    public PdfDocument.PageInfo getPageInfo() {
        return pageInfo;
    }

    public PdfDocument.Page getPage() {
        return page;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
