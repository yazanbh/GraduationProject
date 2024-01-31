package com.it.attendance.lecturer;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.Adapters.CoursesHomePageLecturer.MyAdapter;
import com.it.attendance.Adapters.CoursesHomePageLecturer.RecyclerViewInterface;
import com.it.attendance.Adapters.CoursesHomePageLecturer.course;
import com.it.attendance.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class lecturer_Home_Page extends AppCompatActivity implements RecyclerViewInterface {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    ArrayList<course> courseArrayList;
    MyAdapter myAdapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_home_page);
        //initilaize firestore
        db = FirebaseFirestore.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        //recyclerview
        recyclerView=findViewById(R.id.recyclerView_detail);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseArrayList=new ArrayList<course>();
        myAdapter =new MyAdapter(lecturer_Home_Page.this,courseArrayList,this);
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
            } else if(item.getItemId() == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
                overridePendingTransition(0,0);
                return true;                }

            return false;
        });//end bottom navigation view


        //start fab
        fab=findViewById(R.id.fab_main);
        fab.setOnClickListener(view -> OpenDialog());//end fab onClickListener


    }//end onCreate

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        String CourseCreatedBy= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        //filtering the courses for the own lecturer who has created it and show in home page
    db.collection("course")
            .whereEqualTo("CreatedBy",CourseCreatedBy).orderBy("cNumber", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {

                if(error != null){
                    Log.e("firestore error", Objects.requireNonNull(error.getMessage()));
                    return;
                }

                if (value != null) {
                    for(DocumentChange dc : value.getDocumentChanges()){
                        if(dc.getType()==DocumentChange.Type.ADDED){
                            courseArrayList.add(dc.getDocument().toObject(course.class));
                        }
                        myAdapter.notifyDataSetChanged();
                    }
                }//end if
            });
    }//end function EventChangeListener

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, lecturer_Home_Page.class));
        overridePendingTransition(0, 0);
    }

    private void OpenDialog(){
        Dialog dialog = new  Dialog(this,android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.lecturer_insert_class_dialog);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        EditText mCName = dialog.findViewById(R.id.CourseName);
        EditText mCID = dialog.findViewById(R.id.CourseID);
        EditText mSec = dialog.findViewById(R.id.CourseSection);
        TextView create = dialog.findViewById(R.id.button_createClass);

        //start onClick
        create.setOnClickListener(v -> {
            boolean validate = true;
            if (mCName.getText().toString().equals("")) {
                mCName.setError("Course name cannot be empty");
                mCName.requestFocus();
                validate=false;
            }
            if (mCID.getText().toString().equals("")||mCID.getText().toString().length()<6){
                mCID.setError("Course ID is not valid");
                mCID.requestFocus();
                validate=false;
            }
            if( mSec.getText().toString().equals("")){
                mSec.setError("Section cannot be empty");
                mSec.requestFocus();
                validate=false;
            }
            if(validate){
                String cnum=mCID.getText().toString()+"sec"+mSec.getText().toString();
                //store course name and course number into firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersRef = db.collection("course");
                DocumentReference userDocRef = usersRef.document(cnum);//course number
                //get email for the current user from FirebaseAuth to filter the courses in home page
                String CourseCreatedBy= FirebaseAuth.getInstance().getCurrentUser().getEmail();

                Map<String, Object> updates = new HashMap<>();
                updates.put("cNumber", mCID.getText().toString());
                updates.put("cName", mCName.getText().toString());
                updates.put("cSection", mSec.getText().toString());
                updates.put("CreatedBy", CourseCreatedBy);
                updates.put("enrollStudents",new ArrayList<String>());

                userDocRef.set(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "Course saved successfully");
                        Toast.makeText(lecturer_Home_Page.this, "Successfully created course", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Log.d("TAG", "Error saving Course : ", task.getException());
                    }
                });

                myAdapter.notifyDataSetChanged();

            }//end if

        });//end create.setOnClick

    }// end opendialog
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
                            Intent intent = new Intent(lecturer_Home_Page.this, Class_Detail_lecturer.class);
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