package com.example.fetchingdata;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private DBHandler dbHandler;
    private TextView dataTextView; // TextView to display imported data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure you have this layout file

        // Initialize the database handler
        dbHandler = new DBHandler(this);

        // Initialize the upload, delete, and display buttons
        Button uploadButton = findViewById(R.id.uploadButton); // Ensure this ID matches your XML layout
        Button deleteAllButton = findViewById(R.id.deleteButton); // Ensure this ID matches your XML layout
        Button displayDataButton = findViewById(R.id.displayDataButton); // New button for displaying data
        dataTextView = findViewById(R.id.dataTextView); // TextView for displaying data

        // Request permissions
        requestPermissions();

        // Set up the button click listener to open file chooser
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check permissions again before opening the file picker
                if (hasPermissions()) {
                    openFilePicker();
                } else {
                    requestPermissions();
                }
            }
        });

        // Set up the button click listener for deleting all records
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.deleteAllRecords(); // Call the delete method
                Toast.makeText(MainActivity.this, "All records deleted.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the button click listener for displaying data
        displayDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImportedData(); // Call the method to display data
            }
        });
    }

    private void displayImportedData() {
        List<String> importedData = dbHandler.getAllRecords(); // Fetch data from the database
        StringBuilder dataBuilder = new StringBuilder();

        for (String record : importedData) {
            dataBuilder.append(record).append("\n"); // Append each record to the StringBuilder
        }

        dataTextView.setText(dataBuilder.toString()); // Display the data in the TextView
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, use the new media permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
            }
        } else {
            // For Android versions below 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // Set the MIME type for .xlsx files
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // Handle the file selection and import data into the database
                importDataFromUri(uri);
            }
        }
    }

    private void importDataFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                // Call the importExcelData method from DBHandler
                dbHandler.importExcelData(inputStream);
                Toast.makeText(this, "Data imported successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to open the file.", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to import data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while importing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                openFilePicker(); // Open file picker if permission is granted
            } else {
                Toast.makeText(this, "Permission denied! The app may not function properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}