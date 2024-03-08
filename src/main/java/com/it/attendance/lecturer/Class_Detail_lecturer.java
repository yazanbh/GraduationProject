package com.it.attendance.lecturer;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.helper.widget.MotionEffect;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.it.attendance.Adapters.ClassDetailLeacturer.AttendanceViewInterface;
import com.it.attendance.Adapters.ClassDetailLeacturer.StudentAdapter;
import com.it.attendance.Adapters.ClassDetailLeacturer.TakeAttendanceAdapter;
import com.it.attendance.Adapters.ClassDetailLeacturer.stdShow;
import com.it.attendance.CardEncrypt;
import com.it.attendance.R;
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.paperdb.Paper;

public class Class_Detail_lecturer extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface, AttendanceViewInterface {
    ChipNavigationBar bottomNavigationView;
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
    private boolean mIntentFromCreate;
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
        bar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.delete) {
                delete(CourseNumber);
                return true;
            }
            return true;
        });
        //bottom nav bar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        //go to another page from navbar
        bottomNavigationView.setOnItemSelectedListener(i -> {
                    if(i==R.id.home) {
                        onBackPressed();
                    }
                    else if (i == R.id.profile) {
                        startActivity(new Intent(getApplicationContext(), profile.class));
                        overridePendingTransition(0, 0);
                    }

                }

        );

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
        stdAdapter=new StudentAdapter(Class_Detail_lecturer.this,stdArrayList,this);
        takeAttendanceAdapter=new TakeAttendanceAdapter(getApplicationContext(),stdArrayList);

        //fetch names from firestore and view into lecturer
        getEnrollStudents();
        recyclerView.setAdapter(stdAdapter);

        take_attendance.setOnClickListener(view->{
            //getDate();
            if(counter%2 != 0){
                counter++;
                take_attendance.setText("Stop take attendance");
                recyclerView.setAdapter(takeAttendanceAdapter);
                takeAttendanceAdapter.notifyDataSetChanged();
            }
            else if(counter%2==0){
                take_attendance.setText("Start take attendance");
                counter++;
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
                takeAttendanceAdapter.notifyDataSetChanged();
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
                                total_students.setText("0 Students");

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
                                    stdArrayList.clear();
                                    getEnrollStudents();
                                    stdAdapter.notifyDataSetChanged();
                                    takeAttendanceAdapter.notifyDataSetChanged();
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
            takeAttendanceAdapter.notifyDataSetChanged();
            stdAdapter.notifyDataSetChanged();
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


    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
            if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
                showTurnOnNfcDialog();
            } else if (mNfcAdapter != null) {
                Log.e("hamza", "nfc activeated");
                mCardNfcUtils.enableDispatch();

        }//end if(start_take_attendance)
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
            KAlertDialog dialog =   new KAlertDialog(this, KAlertDialog.WARNING_TYPE,false);
            dialog.setTitleText(title);
            dialog.setContentText(mess);
            dialog.confirmButtonColor(R.color.blue);
            dialog.cancelButtonColor(R.color.blue);
            dialog.setConfirmClickListener(pos, new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16) {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                                finish();
                            } else {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                finish();
                            }

                        }
                    });
            dialog.setCancelClickListener(neg, new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            dialog.dismissWithAnimation();
                        }
                    })
                    .show();

    }

    }//end function




    @Override
    public void startNfcReadCard() {
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {
        String card = null;
        try {
            card = CardEncrypt.encrypt(mCardNfcAsyncTask.getCardNumber());
            Log.w(TAG,"card number has encrypted from " + mCardNfcAsyncTask.getCardNumber() +" to "+ card);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        // String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
       // String cardType = mCardNfcAsyncTask.getCardType();

        String mess = "card number : "+ card;
        Toast.makeText(this,mess,Toast.LENGTH_SHORT).show();
        Log.w("card",mess);
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
                         Toast.makeText(Class_Detail_lecturer.this, "البطاقة غير مسجلة لدينا", Toast.LENGTH_SHORT).show();
                         Log.e(TAG,"card number not found");
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
                    Log.d(TAG, "Attendance updated successfully!");
                    Toast.makeText(Class_Detail_lecturer.this, "تم تسجيل الطالب "+Name+" حاضرا", Toast.LENGTH_SHORT).show();
                    takeAttendanceAdapter.notifyDataSetChanged();
                    stdAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                Log.e("TAG", "فشل تسجيل الحضور للطالب "+Name, e);
                // Handle errors gracefully (e.g., display error message to user)
                Toast.makeText(Class_Detail_lecturer.this, "فشل تسجيل الحضور للطالب "+Name, Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Attendance updating failed!");

            });

        }
        else if(list.isEmpty()){
            Toast.makeText(Class_Detail_lecturer.this, "الرجاء اضافة طلاب الى المساق", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"no students in this course");

        }//end else if
        else if (!list.contains(Email)){
            Toast.makeText(Class_Detail_lecturer.this, "هذا الطالب غير مسجل في المساق", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"student is not enrolled in this course");

        }//end else if


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


    @Override
    public void onItemClick(int postion) {

        db.collection("students").whereIn("email",list).orderBy("email", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Get the list of documents
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();

                            // Extract the string you want to move
                            String email = documents.get(postion).getString("email");
                            String name = documents.get(postion).getString("name");

                            // Start Activity 2 and pass the string as an intent extra
                            Intent intent = new Intent(Class_Detail_lecturer.this, StudentDetail.class);
                            intent.putExtra("Email",email);
                            intent.putExtra("number", CourseNumber);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        } else {
                            Log.w(MotionEffect.TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }
}//end class