package com.example.fetchingdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "school.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL("CREATE TABLE student_info (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, grade TEXT)");
        db.execSQL("CREATE TABLE student_schedule (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, subject TEXT, day TEXT, time TEXT)");
        db.execSQL("CREATE TABLE student_subject (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, subject TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade
        db.execSQL("DROP TABLE IF EXISTS student_info");
        db.execSQL("DROP TABLE IF EXISTS student_schedule");
        db.execSQL("DROP TABLE IF EXISTS student_subject");
        onCreate(db);
    }

    public void importExcelData(InputStream excelFile) {
        SQLiteDatabase db = null;
        Workbook workbook = null;

        try {
            // Use the provided InputStream directly
            workbook = new XSSFWorkbook(excelFile);
            db = this.getWritableDatabase();

            // Import data from student info sheet
            Sheet studentInfoSheet = workbook.getSheetAt(0);
            for (Row row : studentInfoSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                ContentValues values = new ContentValues();

                // Get name
                if (row.getCell(0) != null) {
                    values.put("name", getCellStringValue(row.getCell(0)));
                }

                // Get age
                if (row.getCell(1) != null) {
                    if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        values.put("age", (int) row.getCell(1).getNumericCellValue());
                    } else {
                        Log.e("DBHandler", "Expected numeric value for age, but found: " + row.getCell(1).getCellType());
                    }
                }

                // Get grade
                if (row.getCell(2) != null) {
                    values.put("grade", getCellStringValue(row.getCell(2)));
                }

                db.insert("student_info", null, values);
            }
            Log.d("DBHandler", "Data imported from student_info.");

            // Import data from student schedule sheet
            Sheet studentScheduleSheet = workbook.getSheetAt(1);
            for (Row row : studentScheduleSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                ContentValues values = new ContentValues();

                // Get student_id
                if (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) {
                    values.put("student_id", (int) row.getCell(0).getNumericCellValue());
                }

                // Get subject
                if (row.getCell(1) != null) {
                    values.put("subject", getCellStringValue(row.getCell(1)));
                }

                // Get day
                if (row.getCell(2) != null) {
                    values.put("day", getCellStringValue(row.getCell(2)));
                }

                // Get time
                if (row.getCell(3) != null) {
                    values.put("time", getCellStringValue(row.getCell(3)));
                }

                db.insert("student_schedule", null, values);
            }
            Log.d("DBHandler", "Data imported from student_schedule.");

            // Import data from student subject sheet
            // Import data from student subject sheet
            Sheet studentSubjectSheet = workbook.getSheetAt(2);
            for (Row row : studentSubjectSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                ContentValues values = new ContentValues();

                // Get student_id
                if (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) {
                    values.put("student_id", (int) row.getCell(0).getNumericCellValue());
                }

                // Get subject
                if (row.getCell(1) != null) {
                    values.put("subject", getCellStringValue(row.getCell(1)));
                }

                long result = db.insert("student_subject", null, values);
                if (result == -1) {
                    Log.e("DBHandler", "Failed to insert data into student_subject: " + values);
                } else {
                    Log.d("DBHandler", "Inserted into student_subject: " + values);
                }
            }
            Log.d("DBHandler", "Data imported from student_subject.");

            // Show success message
            if (context != null) {
                Toast.makeText(context.getApplicationContext(), "Data imported successfully!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("DBHandler", "Error importing data: " + e.getMessage());
            if (context != null) {
                Toast.makeText(context.getApplicationContext(), "Error importing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } finally {
            // Ensure resources are closed properly
            try {
                if (db != null) {
                    db.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
                if (excelFile != null) {
                    excelFile.close(); // Close the InputStream
                }
            } catch (Exception e) {
                Log.e("DBHandler", "Error closing resources: " + e.getMessage());
            }
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM student_info");
        db.execSQL("DELETE FROM student_schedule");
        db.execSQL("DELETE FROM student_subject");
        db.close();

        // Show success message
        if (context != null) {
            Toast.makeText(context.getApplicationContext(), "All records deleted successfully!", Toast.LENGTH_LONG).show();
        }
    }

    // Method to retrieve all student info records
    // Method to retrieve all student info records
    public List<String> getAllStudentInfo() {
        List<String> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM student_info", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String record = "ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("id")) +
                            ", Name: " + cursor.getString(cursor.getColumnIndexOrThrow("name")) +
                            ", Age: " + cursor.getInt(cursor.getColumnIndexOrThrow("age")) +
                            ", Grade: " + cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    records.add(record);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return records;
    }

    // Method to retrieve all student schedule records
    public List<String> getAllStudentSchedule() {
        List<String> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM student_schedule", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String record = "ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("id")) +
                            ", Student ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("student_id")) +
                            ", Subject: " + cursor.getString(cursor.getColumnIndexOrThrow("subject")) +
                            ", Day: " + cursor.getString(cursor.getColumnIndexOrThrow("day")) +
                            ", Time: " + cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    records.add(record);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return records;
    }

    // Method to retrieve all student subject records
    public List<String> getAllStudentSubjects() {
        List<String> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM student_subject", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String record = "ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("id")) +
                            ", Student ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("student_id")) +
                            ", Subject: " + cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                    records.add(record);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return records;
    }

    public List<String> getAllRecords() {

        // Retrieve student info records
        List<String> studentInfoRecords = getAllStudentInfo();
        List<String> allRecords = new ArrayList<>(studentInfoRecords);

        // Retrieve student schedule records
        List<String> studentScheduleRecords = getAllStudentSchedule();
        allRecords.addAll(studentScheduleRecords);

        // Retrieve student subject records
        List<String> studentSubjectRecords = getAllStudentSubjects();
        allRecords.addAll(studentSubjectRecords);

        return allRecords;
    }
}