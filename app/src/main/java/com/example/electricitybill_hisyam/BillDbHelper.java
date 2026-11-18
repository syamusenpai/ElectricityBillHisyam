package com.example.electricitybill_hisyam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Local SQLite database that stores every bill estimate (inputs and outputs). */
public class BillDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "voltwise.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "bills";
    private static final String COL_ID = "id";
    private static final String COL_MONTH = "month";
    private static final String COL_MONTH_INDEX = "month_index";
    private static final String COL_UNITS = "units";
    private static final String COL_TOTAL = "total_charges";
    private static final String COL_REBATE = "rebate_percent";
    private static final String COL_FINAL = "final_cost";
    private static final String COL_CREATED = "created_at";

    public BillDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MONTH + " TEXT NOT NULL, "
                + COL_MONTH_INDEX + " INTEGER NOT NULL, "
                + COL_UNITS + " INTEGER NOT NULL, "
                + COL_TOTAL + " REAL NOT NULL, "
                + COL_REBATE + " REAL NOT NULL, "
                + COL_FINAL + " REAL NOT NULL, "
                + COL_CREATED + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    private ContentValues toValues(Bill bill) {
        ContentValues v = new ContentValues();
        v.put(COL_MONTH, bill.month);
        v.put(COL_MONTH_INDEX, BillCalculator.monthIndex(bill.month));
        v.put(COL_UNITS, bill.units);
        v.put(COL_TOTAL, bill.totalCharges);
        v.put(COL_REBATE, bill.rebatePercent);
        v.put(COL_FINAL, bill.finalCost);
        return v;
    }

    public long insert(Bill bill) {
        ContentValues v = toValues(bill);
        v.put(COL_CREATED, System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE, null, v);
    }

    public boolean update(Bill bill) {
        return getWritableDatabase().update(TABLE, toValues(bill),
                COL_ID + " = ?", new String[]{String.valueOf(bill.id)}) > 0;
    }

    public boolean delete(long id) {
        return getWritableDatabase().delete(TABLE,
                COL_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    /** True if another record (other than excludeId) already uses this month. */
    public boolean monthExists(String month, long excludeId) {
        try (Cursor c = getReadableDatabase().query(TABLE, new String[]{COL_ID},
                COL_MONTH + " = ? AND " + COL_ID + " != ?",
                new String[]{month, String.valueOf(excludeId)}, null, null, null, "1")) {
            return c.moveToFirst();
        }
    }

    /** All bills, ordered January to December (newest first within a month). */
    public List<Bill> getAll() {
        List<Bill> bills = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE, null, null, null, null, null,
                COL_MONTH_INDEX + " ASC, " + COL_ID + " DESC")) {
            while (c.moveToNext()) bills.add(fromCursor(c));
        }
        return bills;
    }

    public Bill getById(long id) {
        try (Cursor c = getReadableDatabase().query(TABLE, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    private Bill fromCursor(Cursor c) {
        return new Bill(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_MONTH)),
                c.getInt(c.getColumnIndexOrThrow(COL_UNITS)),
                c.getDouble(c.getColumnIndexOrThrow(COL_TOTAL)),
                c.getDouble(c.getColumnIndexOrThrow(COL_REBATE)),
                c.getDouble(c.getColumnIndexOrThrow(COL_FINAL)));
    }
}
