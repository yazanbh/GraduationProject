package com.it.attendance.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.R;
import com.it.attendance.lecturer.lecturer_Home_Page;
import com.it.attendance.lecturer.profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Class_detail_std extends AppCompatActivity {
    FirebaseFirestore db;
    ImageView img_btn;
    TextView cname,PresentCount,AbsentCount,percent;
    SwipeRefreshLayout swipeLayout;
    double present,absent;
    BottomNavigationView bottomNavigationView;


    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_class_detail);

        pageData();
        //back button of top|left in page
        img_btn=findViewById(R.id.img_btn_back);
        img_btn.setOnClickListener(v -> {
            onBackPressed();
        });


        swipeLayout =  findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your refresh operation here
                // e.g., fetch new data, update UI
                pageData();

                // Hide the refresh progress indicator once done
                swipeLayout.setRefreshing(false);

            }

        });

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                onBackPressed();
                return true;
            } else if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile_std.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });


    }//end onCreate


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomePage_std.class));
        overridePendingTransition(0, 0);
    }






    void pageData(){

        CalendarView calendarView = findViewById(R.id.calendarView);
        List<EventDay> events = new ArrayList<>();
        String courseName = getIntent().getStringExtra("name");
        Log.e("course name",courseName);
        String courseNumber = getIntent().getStringExtra("number");
        Log.e("course number",courseNumber);

        present=0;
        absent=0;
        //set course name in text view of top page
        cname=findViewById(R.id.CourseName);
        cname.setText(courseName);
        //text views for attendance rate
        PresentCount=findViewById(R.id.presentCount);
        AbsentCount=findViewById(R.id.absentCount);
        percent=findViewById(R.id.overallAttendance);

        //get email for the current user from FirebaseAuth
        String Email= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();


        db = FirebaseFirestore.getInstance();

        //list to get IsPresent Boolean if equal to true or false
        List<Boolean> presentList = new ArrayList<>();
        presentList.add(true);
        presentList.add(false);

        //document reference for attendance
        CollectionReference collectionRef = db.collection("attendance").document(courseNumber).collection(Email);

        Query query = collectionRef.whereIn("IsPresent",presentList);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String day = document.getString("day");
                                String month = document.getString("month");
                                String year = document.getString("year");
                                boolean isPresent = document.getBoolean("IsPresent");

                                Calendar mCalendar = Calendar.getInstance();
                                //set date to calendar
                                mCalendar.set(Calendar.YEAR, Integer.parseInt(year));
                                mCalendar.set(Calendar.MONTH, (Integer.parseInt(month))-1);
                                mCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                                if(isPresent) {
                                    present=present+1;
                                    events.add(new EventDay(mCalendar, R.drawable.present));
                                    Log.e("successfully","present done"+String.valueOf(present));
                                }//end if
                                else{
                                    absent= absent+1;
                                    events.add(new EventDay(mCalendar, R.drawable.absent));
                                    Log.e("successfully","absent done"+String.valueOf(absent));
                                }//end else

                            }//end for loop

                            //set present count in text view
                            PresentCount.setText(String.valueOf((int)present));

                            AbsentCount.setText(String.valueOf((int)absent));

                            if(present!=0 || absent!=0) {
                                double OverallAttendance = (((present / (present + absent))) * 100);
                                String formattedPresent = String.format(Locale.ENGLISH,"%.1f", OverallAttendance);
                                percent.setText(formattedPresent+" %");
                                Log.e("percentage",String.valueOf(OverallAttendance));
                            }
                            else{ percent.setText("0 %");}

                            //set event to calendar view
                            calendarView.setEvents(events);
                        } else {
                            Log.w("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });//end OnCompleteListiner

    }

}//end class