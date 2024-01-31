package com.it.attendance.lecturer;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.Adapters.ClassDetailLeacturer.StudentAdapter;
import com.it.attendance.Adapters.ClassDetailLeacturer.TakeAttendanceAdapter;
import com.it.attendance.Adapters.ClassDetailLeacturer.stdShow;
import com.it.attendance.R;
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;

public class Class_Detail_lecturer extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface {
    BottomNavigationView bottomNavigationView;
    TextView className , total_students;
    FirebaseFirestore db;
    Toolbar bar;
    Button add_std;
    String CourseNumber;
    RecyclerView recyclerView;
    ArrayList<stdShow> stdArrayList;
    StudentAdapter stdAdapter;
    TakeAttendanceAdapter takeAttendanceAdapter;

    List<String> list;
    private NfcAdapter mNfcAdapter;
    private boolean mIntentFromCreate , start_take_attendance;
    private cardNfcUtils mCardNfcUtils;
    private ProgressDialog mProgressDialog;

    private String mDoNotMoveCardMessage;
    private String mUnknownEmvCardMessage;
    private String mCardWithLockedNfcMessage;
    private AlertDialog mTurnNfcDialog;
    private CardNfcAsyncTask mCardNfcAsyncTask;
    private boolean mIsScanNow;
    int counter=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_detail);
        //initilaize firestore
        db = FirebaseFirestore.getInstance();
        //get the course data from Lecturer_Home_Page
        String CourseName = getIntent().getStringExtra("name");
        CourseNumber = getIntent().getStringExtra("number");

        Paper.init(getApplicationContext());
        Paper.book().delete("courseNumber");
        Paper.book().write("courseNumber", CourseNumber);

        //set course name
        className = findViewById(R.id.classname_detail);
        className.setText(CourseName);


        //tool bar
        bar = findViewById(R.id.toolbar_class_detail);
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.delete) {
                    delete(CourseNumber);
                    return true;
                }
                return true;
            }
        });
        //bottom nav bar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        //disable center button in navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));
                overridePendingTransition(0, 0);
                return true;
            }else if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        // add excele file of students
        add_std=findViewById(R.id.add_students);
        add_std.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //   startActivity(new Intent(Class_Detail_lecturer.this, lecturer_addStudent.class));
                //   overridePendingTransition(0, 0);

                OpenDialog();
            }
        });


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Button take_attendance = findViewById(R.id.submit_attendance_btn);
        if (mNfcAdapter == null){
            Toast.makeText(Class_Detail_lecturer.this, "NFC is not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Class_Detail_lecturer.this, "NFC is supported on this device", Toast.LENGTH_SHORT).show();
            mCardNfcUtils = new cardNfcUtils(this);
            createProgressDialog();
            initNfcMessages();
            mIntentFromCreate = true;
            onNewIntent(getIntent());
        }

        recyclerView=findViewById(R.id.recyclerView_detail);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stdArrayList=new ArrayList<stdShow>();
        stdAdapter=new StudentAdapter(getApplicationContext(),stdArrayList);
        takeAttendanceAdapter=new TakeAttendanceAdapter(getApplicationContext(),stdArrayList);

        //fetch names from firestore and view into lecturer
        getEnrollStudents();
        recyclerView.setAdapter(stdAdapter);
       // stdAdapter.notifyDataSetChanged();
       // takeAttendanceAdapter.notifyDataSetChanged();

        take_attendance.setOnClickListener(view->{
            //getDate();
            if(counter%2 != 0){
                start_take_attendance=true;
                counter++;
                take_attendance.setText("Stop take attendance");
              //  stdArrayList.clear();
              //  getEnrollStudents();
                recyclerView.setAdapter(takeAttendanceAdapter);
                takeAttendanceAdapter.notifyDataSetChanged();
            }
            else if(counter%2==0){
                start_take_attendance=false;
                take_attendance.setText("Start take attendance");
                counter++;

                stdArrayList.clear();
                getEnrollStudents();
                recyclerView.setAdapter(stdAdapter);
                stdAdapter.notifyDataSetChanged();

            }

        });


        //swipe refresh layout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your refresh operation here
                // e.g., fetch new data, update UI
                stdArrayList.clear();
                getEnrollStudents();
                stdAdapter.notifyDataSetChanged();
                // Hide the refresh progress indicator once done
                swipeRefreshLayout.setRefreshing(false);

            }
        });


    }//end onCreate

    @SuppressLint("NotifyDataSetChanged")
    public void getEnrollStudents() {
        FirebaseFirestore.getInstance().collection("course")
                .document(CourseNumber).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        list =(List<String>) documentSnapshot.get("enrollStudents");
                        Log.e("on successssss", list.toString());
                        if(!list.isEmpty()){
                            try {
                                //get total students in the course
                                total_students=findViewById(R.id.total_students_detail);
                                total_students.setText(list.size()+" students");

                                db = FirebaseFirestore.getInstance();
                                db.collection("students").whereIn("email",list).addSnapshotListener((value, error) -> {

                                    if(error != null){
                                        Log.e("firestore error", Objects.requireNonNull(error.getMessage()));
                                        return;
                                    }

                                    if (value != null&& !value.isEmpty()) {
                                        Log.d("Snapshot not empty", "Found enrolled students");
                                        for(DocumentChange dc : value.getDocumentChanges()){
                                            if(dc.getType()==DocumentChange.Type.ADDED){
                                                stdArrayList.add(dc.getDocument().toObject(stdShow.class));
                                            }
                                            stdAdapter.notifyDataSetChanged();
                                        }
                                    }//end if
                                    else {
                                        Log.e("Snapshot empty", "No enrolled students found");

                                    }
                                });

                            } catch (IllegalArgumentException e) {
                                Log.e("TAG", "Error fetching students: " + e.getMessage());
                                Toast.makeText(Class_Detail_lecturer.this, "Please Add Students", Toast.LENGTH_SHORT).show();
                                OpenDialog();
                                // Display a user-friendly message to the user, like "No students found in the course."
                                // You can also consider offering alternative actions, like manually adding students.
                            }

                        }//end if(!list.isEmpty())


                    }//end onSuccess
                });



    }//end function getEnrollStudents


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, lecturer_Home_Page.class));
        overridePendingTransition(0, 0);
    }

    private void delete(String CourseNumber) {
        db.collection("course").document(CourseNumber)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(Class_Detail_lecturer.this, "Successfully Delete Course", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));
                        overridePendingTransition(0, 0);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting document", e);
                        Toast.makeText(Class_Detail_lecturer.this, "Cannot Delete Course", Toast.LENGTH_SHORT).show();

                    }
                });

    }//end delete function

    private void OpenDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_add_student);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        EditText stdName = dialog.findViewById(R.id.name_student_popup);
        EditText stdID = dialog.findViewById(R.id.regNo_student_popup);
        TextView addstd = dialog.findViewById(R.id.add_btn_popup);
        TextView cancel = dialog.findViewById(R.id.cancel_btn_popup);
        TextView addFromFile = dialog.findViewById(R.id.excel);
        //close dialog box on click the cancel button
        cancel.setOnClickListener(view->{
            dialog.dismiss();
        });
        //add students to firebase
        addstd.setOnClickListener(view->{
            boolean t=true;
            if(stdName.getText().toString().length()<10){
                stdName.setError("Name is not valid");
                stdName.requestFocus();
                t=false;
            }
            if(stdID.getText().toString().length()!=10){
                stdID.setError("student ID is not valid");
                stdID.requestFocus();
                t=false;
            }
            if(t){
                AddStudentToCourse(stdID.getText().toString()+"@st.aabu.edu.jo",stdName.getText().toString());
                dialog.dismiss();
            }
        });
        addFromFile.setOnClickListener(view->{
            showFileChooser();
            dialog.dismiss();
        });

    }//end opendialog function


    private void AddStudentToCourse(String stdEmail, String stdName){
        //check if the student have account in the app or not
        DocumentReference docRef = db.collection("students")
                .document(stdEmail);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // student have an account exists & add student to the course in enrollStudents filed in firebase
                    String CourseNumber = getIntent().getStringExtra("number");
                    DocumentReference docRef = db.collection("course").document(CourseNumber);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("enrollStudents",  FieldValue.arrayUnion(stdEmail));
                    docRef.update(updateData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Class_Detail_lecturer.this, "Student added successfully to the course!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("TAG", "Error adding student email", e);
                                }
                            });


                } else {
                    // Document does not exist & save username and email to firestore
                    Log.e("TAG", "Error student dont have account ", null);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("students")
                            .document(stdEmail);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                                // Email does not exist in Firestore
                                Log.e("TAG", "creating a student account ", null);
                                CollectionReference usersRef = db.collection("students");
                                DocumentReference userDocRef = usersRef.document(stdEmail);
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("name", stdName);
                                updates.put("email", stdEmail);

                                userDocRef.set(updates).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "create a student account successfully");
                                        AddStudentToCourse(stdEmail,stdName);

                                    } else {
                                        Log.d("TAG", "Error on create a student account ", task.getException());
                                    }
                                });
                            } //end if statement

                        }//end onSuccess
                    });

                }
            }
        });
    }//end function


    private void showFileChooser(){
        Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try{
            startActivityForResult(Intent.createChooser(intent,"Select a file!"),100);
        }
        catch (Exception e){
            Toast.makeText(this,"please install a file manager",Toast.LENGTH_SHORT).show();

        }
    }//end function
    @SuppressLint("SetTextI18n")
    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              @Nullable Intent data) {
        if(requestCode==100 && resultCode==RESULT_OK && data!=null){
            TextView txt = findViewById(R.id.total_students_detail);
            Uri uri = data.getData();
            ExcelReader.existed(this,uri,CourseNumber); // adding student to firebase who use nfc tag
            try {
                ExcelReader.readExcelFile(this, uri,CourseNumber); //add students to course
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    String getDate(){
        Calendar mCalendar = Calendar.getInstance();
        Calendar calendars = Calendar.getInstance();
        Locale englishLocale = Locale.ENGLISH; // Or Locale.US for US English


        int day = calendars.get(Calendar.DAY_OF_MONTH);
        int month =calendars.get(Calendar.MONTH);
        int year =calendars.get(Calendar.YEAR);


        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);


        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy",englishLocale);
        dateFormat.setCalendar(mCalendar);
        Date today = Calendar.getInstance().getTime();
        String selectedDate = dateFormat.format(today);
       // Toast.makeText(Class_Detail_lecturer.this,selectedDate,Toast.LENGTH_SHORT).show();
        Log.e("date",selectedDate);

        return selectedDate;
    }

    void getStatus(){
        Paper.init(getApplicationContext());

        String cNumber= Paper.book().read("courseNumber");

        Log.e("yazaaaaaaaaaaan",cNumber);
        db = FirebaseFirestore.getInstance();

        //list to get IsPresent Boolean if equal to true or false
        List<Boolean> presentList = new ArrayList<>();
        presentList.add(true);
        presentList.add(false);

        //document reference for attendance
        CollectionReference collectionRef = db.collection("attendance").document(cNumber).collection("2000901023@st.aabu.edu.jo");

        Query query = collectionRef.whereIn("IsPresent",presentList);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            int present=0;
                            int absent=0;

                            for (DocumentSnapshot document : task.getResult()) {
                                boolean isPresent = document.getBoolean("IsPresent");
                                Log.e("successfully", "Adapter done");

                                if (isPresent) {
                                    present = present + 1;
                                    Log.e("successfully", "present done" + String.valueOf(present));
                                }//end if
                                else {
                                    absent = absent + 1;
                                    Log.e("successfully", "absent done" + String.valueOf(absent));
                                }//end else
                            }//end for loop

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("successfully", "Adapter failuer");

                    }
                });



    }//end function


    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()){
            showTurnOnNfcDialog();
        } else if (mNfcAdapter != null){
            Log.e("hamza","nfc activeated");
            mCardNfcUtils.enableDispatch();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mCardNfcUtils.disableDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                    .build();
        }
    }//end


    private void createProgressDialog(){
        String title = "Scanning ...";
        String mess = "Please do not remove or move card during reading.";
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }//end function
    private void initNfcMessages(){
        mDoNotMoveCardMessage = "Please do not move card, try again !";
        mCardWithLockedNfcMessage = "NFC is locked on this card.";
        mUnknownEmvCardMessage = "Unknown EMV card";
    }//end function


    private void showTurnOnNfcDialog(){
        if (mTurnNfcDialog == null) {
            String title = "NFC is turned off.";
            String mess = "You need turn on NFC module for scanning. Wish turn on it now?";
            String pos = "Turn on";
            String neg = "Dismiss";
            mTurnNfcDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(mess)
                    .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16) {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            } else {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    }).create();
        }
        mTurnNfcDialog.show();
    }




    @Override
    public void startNfcReadCard() {
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {
        String card = mCardNfcAsyncTask.getCardNumber();
        card = getPrettyCardNumber(card);
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();

        String mess = "card number : "+card;
        Toast.makeText(this,mess,Toast.LENGTH_SHORT).show();
        Log.d("card",mess);
        getEmailFromCardNumber(card);

    }

     void getEmailFromCardNumber(String card) {
         db.collection("students")
                 .whereEqualTo("card", card)
                 .get()
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         for (DocumentSnapshot document : task.getResult()) {
                             // Access document data using document.get("fieldName")
                             String email = document.getString("email");
                             String name = document.getString("name");
                             //set attendance to true
                             TakeAttendanceUsingCardAndEmail(email,name);
                         }
                         if (task.getResult().size() == 0) {
                             // No document found with the given card number
                         Toast.makeText(Class_Detail_lecturer.this, "The card number is not registered in the database", Toast.LENGTH_SHORT).show();

                         }
                     } else {
                         // Handle error
                     }
                 });
    }//end getEmailFromCardNumber

    void TakeAttendanceUsingCardAndEmail(String Email, String Name) {
        if (list.contains(Email)) {
            String today = getDate();
            Map<String, Object> data = new HashMap<>();
            data.put("day", today.substring(0, 2));
            data.put("month", today.substring(3, 5));
            data.put("year", today.substring(6, 10));
            data.put("Email", Email);
            data.put("name", Name);
            data.put("IsPresent", true);


            CollectionReference collectionRef = db.collection("attendance").document(CourseNumber).collection(Email);
            collectionRef.document(today).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("TAG", "Attendance updated successfully!");
                    Toast.makeText(Class_Detail_lecturer.this, "Attendance updated successfully!", Toast.LENGTH_SHORT).show();
                    takeAttendanceAdapter.notifyDataSetChanged();
                    stdAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                Log.e("TAG", "Error updating attendance", e);
                // Handle errors gracefully (e.g., display error message to user)
            });

        }
        else if(list==null){
            Toast.makeText(Class_Detail_lecturer.this, "Please add student before take attendance!", Toast.LENGTH_SHORT).show();
        }//end else if
        else{
            Toast.makeText(Class_Detail_lecturer.this, "The Student is not in this Course!", Toast.LENGTH_SHORT).show();
        }//end else
    }//end function


    @Override
    public void doNotMoveCardSoFast() {
        Toast.makeText(Class_Detail_lecturer.this, mDoNotMoveCardMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unknownEmvCard() {
        Toast.makeText(Class_Detail_lecturer.this, mUnknownEmvCardMessage, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void cardWithLockedNfc() {
        Toast.makeText(Class_Detail_lecturer.this, mCardWithLockedNfcMessage, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void finishNfcReadCard() {
        mProgressDialog.dismiss();
        mCardNfcAsyncTask = null;
        mIsScanNow = false;
    }

    private String getPrettyCardNumber(String card){
        String div = "-";
        return  card.substring(0,4) + div + card.substring(4,8) + div + card.substring(8,12)
                +div + card.substring(12,16);
    }


}//end class