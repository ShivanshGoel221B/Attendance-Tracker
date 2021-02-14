package com.goel.attendancetracker.database;

public class Params {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "attendance_db";

    // KEYS

    public static final String ORGANISATIONS = "_organisations";
    public static final String TARGET = "target";
    public static final String ATTENDANCE = "attendance";
    public static final String NAME = "name";
    public static final String HISTORY = "history";

    public static final String ROOT_TABLE = ORGANISATIONS + "(s_no INTEGER PRIMARY KEY, "
                                                            + NAME + " TEXT, "
                                                            + ATTENDANCE + " INTEGER, "
                                                            + TARGET + " INTEGER)";

    public static final String NEW_TABLE = "(class_sno INTEGER PRIMARY KEY, "
                                            + NAME + " TEXT, "
                                            + ATTENDANCE + " INTEGER, "
                                            + TARGET + " INTEGER, "
                                            + HISTORY + " TEXT)";

    public static String OPEN_ORG;

    public static final String CLASS_DATA_ARRAY = "com.goel.attendancetracker.database.classDataArray";
}
