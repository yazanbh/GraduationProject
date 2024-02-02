package com.it.attendance.lecturer;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
    public static void readExcelFile(Context context, Uri uri, String CourseNumber) throws IOException {
        Log.e("reading XLSX file","reading XLSX file from resources started");
       try {
           ContentResolver contentResolver = context.getContentResolver();
           InputStream inputStream = contentResolver.openInputStream(uri);
           assert inputStream != null;
           XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
           Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
           FirebaseFirestore db = FirebaseFirestore.getInstance(); //initialize FireStore
           DocumentReference docRef = db.collection("course").document(CourseNumber);
           Map<String, Object> updateData = new HashMap<>();
           // Loop through all rows
           int studentId = 0;
           String name = "";
           for (int i = 0; i <= sheet.getLastRowNum(); i++) {
               // Get the current row
               Row row = sheet.getRow(i);

               // Get the ID cell (assuming it's in the first column)
               Cell idCell = row.getCell(0);
               if (idCell != null && idCell.getCellType() ==Cell.CELL_TYPE_NUMERIC) {
                   studentId = (int) idCell.getNumericCellValue();
                   Log.e("cell student id ", String.valueOf(studentId));
               }

               String Email=(studentId+"@st.aabu.edu.jo");
               // student have an account exists & add student to the course in enrollStudents filed in firebase
               updateData.put("enrollStudents", FieldValue.arrayUnion(Email));
               docRef.update(updateData)
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Log.e("TAG", "Error adding student email "+Email);
                           }
                       });


           }//end for loop
           showToast(context,"students successfully added to course");

           workbook.close();
       } catch (IOException e) {
           e.printStackTrace();
           showToast(context, "Error reading Excel file to add student into course");
       }


    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }//end function

    public static void existed(Context context, Uri uri, String courseNumber){
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            // Loop through all rows
            int studentId = 0;
            String name = "";
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                // Get the current row
                Row row = sheet.getRow(i);

                // Get the ID cell (assuming it's in the first column)
                Cell idCell = row.getCell(0);
                if (idCell != null && idCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    studentId = (int) idCell.getNumericCellValue();
                    Log.e("cell name", String.valueOf(studentId));
                }

                // Get the name cell (assuming it's in the second column)
                Cell nameCell = row.getCell(1);
                if (nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_STRING) {
                    name = nameCell.getStringCellValue();
                    Log.e("cell name", name);
                }
                // Process the extracted name and ID here
                // (e.g., print them, store them in a list, etc.)
                String Email = (studentId + "@st.aabu.edu.jo");
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //check if the student have account in the app or not
                DocumentReference docRef = db.collection("students")
                        .document(Email);
                String finalName = name;
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            // Email does not exist in Firestore
                            Log.e("TAG", "creating a student account ", null);
                            CollectionReference usersRef = db.collection("students");
                            DocumentReference userDocRef = usersRef.document(Email);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("name", finalName);
                            updates.put("email", Email);

                            userDocRef.set(updates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "create a student account successfully");
                                } else {
                                    Log.d("TAG", "Error on create a student account ", task.getException());
                                }
                            });
                        } //end if statement

                    }//end onSuccess


                });//end docRef
                String formattedDate = "test";

                //document reference for attendance
                DocumentReference docReff = db.collection("attendance").document(courseNumber).collection(Email).document(formattedDate);
                // Create a Map to store the document's data (replace with your actual data)
                Map<String, Object> data = new HashMap<>();
                data.put("Email", Email);
                // Add the document to Firestore
                docReff.set(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "attendance document created successfully "+Email);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error creating attendance document "+Email, e);
                            }
                        });
                //end docReff

            }//end for loop

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            showToast(context, "Error reading Excel file to add student into database");
        }
    }//end function
}//end class
