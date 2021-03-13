package com.goel.attendancetracker.downloadmanager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.goel.attendancetracker.R;

public abstract class FileDataModel {

    public static final String TITLE = "Attendance Tracker";
    private static final int TITLE_SIZE = 54;
    private static final int TITLE_NAME_SIZE = 34;
    private static final int NAME_SIZE = 28;
    private static final int TEXT_SIZE = 22;
    private static final int ROW_HEIGHT = 26;
    private static final int REGULAR_STROKE = 2;
    public static final int MEDIUM_STROKE = 4;
    private static final int BOLD_STROKE = 6;
    public static Typeface[] fonts;
    public static Bitmap logo;
    public static int safeColor, lowColor, dangerColor;

    public void createFirstPage(ClassDownloadManager manager){
        Paint paint = manager.getPaint();
        Canvas canvas = manager.getCanvas();
        int attendance = manager.getModel().getClassAttendancePercentage();
        int target = manager.getModel().getRequiredAttendance();
        int color;
        if (attendance >= target)
            color = safeColor;
        else if (attendance > target*0.75f)
            color = lowColor;
        else
            color = dangerColor;

        // SET IMAGE
        canvas.drawBitmap(logo, 10, 15, paint);

        // SET TITLE
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(TITLE_SIZE);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.NORMAL));
        canvas.drawText(TITLE, 135, 76, paint);

        //SET TAG LINE
        paint.setTextSize(TEXT_SIZE);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.NORMAL));
        canvas.drawText("Track Your Attendance With Ease", 135, 108, paint);
        paint.setStrokeWidth(4);
        canvas.drawLine(10, 130, manager.getPageInfo().getPageWidth()-10, 130, paint);

        // SET ORGANISATION NAME
        paint.setTextSize(36);
        paint.setUnderlineText(true);
        paint.setTypeface(Typeface.create(fonts[4], Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(manager.getOrganisationName().toUpperCase(), (float) manager.getPageInfo().getPageWidth()/2, 185, paint);
        paint.setUnderlineText(false);

        //SET NAME AND TARGET
        paint.setTextSize(TITLE_NAME_SIZE);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.BOLD));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Subject: ", 150, 240, paint);
        canvas.drawText("Target: ", 150, 290, paint);
        canvas.drawText("Attendance: ", 200, 360, paint);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(manager.getModel().getClassName().toUpperCase(), 170, 240, paint);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.BOLD));
        canvas.drawText(target + "%", 170, 290, paint);

        // SET ATTENDANCE
        int total = manager.getModel().getPresentCount() + manager.getModel().getAbsentCount();
        String status = manager.getModel().getPresentCount() + "/" + total;
        paint.setTypeface(Typeface.create(fonts[2], Typeface.BOLD));
        canvas.drawText(status, 220, 360, paint);
        paint.setColor(color);
        paint.setTextSize(96);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(attendance + "%", (float) manager.getPageInfo().getPageWidth()/2, 470, paint);
    }

    public void writeAttendance(ClassDownloadManager manager){

    }
}
