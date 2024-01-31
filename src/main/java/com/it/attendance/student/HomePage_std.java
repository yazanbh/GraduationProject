package com.it.attendance.student;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.Adapters.Students.Course;
import com.it.attendance.Adapters.Students.stdAdapter;
import com.it.attendance.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomePage_std extends AppCompatActivity implements com.it.attendance.Adapters.Students.HomePage_std_interFace {
    BottomNavigationView bottomNavigationView;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    ArrayList<Course> courseArrayList;
    stdAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_home_page);
        //initilaize firestore
        db = FirebaseFirestore.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        //recyclerview
        recyclerView=findViewById(R.id.recyclerView_detail1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseArrayList=new ArrayList<Course>();
        myAdapter =new stdAdapter(HomePage_std.this,courseArrayList, this);
        //fetch courses from firestore and view into lecturer
        EventChangeListener();
        recyclerView.setAdapter(myAdapter);
        //swipe refresh layout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your refresh operation here
                // e.g., fetch new data, update UI
                courseArrayList.clear();
                EventChangeListener();
                myAdapter.notifyDataSetChanged();
                // Hide the refresh progress indicator once done
                swipeRefreshLayout.setRefreshing(false);

            }
        });
        //go to another page from navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                return true;
            }
            else if(item.getItemId() == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile_std.class));
                overridePendingTransition(0,0);
                return true;                }

            return false;
        });//end bottom navigation view




    }//end oncreate

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        String stdEmail= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        //filtering the courses for the own lecturer who has created it and show in home page
        db.collection("course")
                .whereArrayContains("enrollStudents",stdEmail).orderBy("cNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if(error != null){
                        Log.e("firestore error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }

                    if (value != null) {
                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType()==DocumentChange.Type.ADDED){
                                courseArrayList.add(dc.getDocument().toObject(Course.class));
                            }
                            myAdapter.notifyDataSetChanged();
                        }
                    }//end if
                });
    }//end function EventChangeListener


    @Override
    public void onItemClick(int postion) {

       /* String name= findViewById(R.id.CourseName_adapter).toString();
        String number= findViewById(R.id.CourseID_adapter).toString();
        Intent intent = new Intent(lecturer_Home_Page.this, Class_Detail_lecturer.class);
        intent.putExtra("Cname",name);
        intent.putExtra("Cnumber",number);
        overridePendingTransition(0,0);
        startActivity(intent);
*/

        db.collection("course")
                .whereArrayContains("enrollStudents",FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .orderBy("cNumber", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Get the list of documents
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();

                            // Extract the string you want to move
                            String courseName = documents.get(postion).getString("cName");
                            String courseNumber = documents.get(postion).getString("cNumber");
                            String courseSection = documents.get(postion).getString("cSection");

                            // Start Activity 2 and pass the string as an intent extra
                            Intent intent = new Intent(HomePage_std.this, Class_detail_std.class);
                            intent.putExtra("name", courseName);
                            intent.putExtra("number", courseNumber+"sec"+courseSection);
                            startActivity(intent);
                             overridePendingTransition(0,0);
                        } else {
                            Log.w(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }
}//end class