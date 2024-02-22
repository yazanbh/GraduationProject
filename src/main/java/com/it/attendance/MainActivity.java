package com.it.attendance;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.lecturer.Login_lecturer;
import com.it.attendance.lecturer.lecturer_Home_Page;
import com.it.attendance.student.HomePage_std;
import com.it.attendance.student.Login_std;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    CardView teacher,std;
    FirebaseAuth mAuth;
    KAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_your_role);
        // Check internet connection initially
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
       Paper.init(getApplicationContext());

       String email= Paper.book().read("Email");
       String password =Paper.book().read("Password");
       String isLoggedIn = Paper.book().read("isLoggedIn");
       String type= Paper.book().read("type");

        Log.e("Paper","email: "+ email +"\n password "+ password +"\n isLogin? "+ isLoggedIn +"\n type :"+type);
        if(isLoggedIn!=null && isLoggedIn.equals("true")){
            pDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Logging in");
            pDialog.setCancelable(false);
            pDialog.show();
                if(type.equals("teacher")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        pDialog.dismissWithAnimation();
                                        startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));

                                    }//end if
                                }//end oncomplete

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pDialog.dismissWithAnimation();
                                    Toast.makeText(MainActivity.this, "something wrong, please login to your teacher account", Toast.LENGTH_SHORT).show();
                                }
                            });
                }//end if(type.equals("teacher"))
            else if(type.equals("student")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        // Get a reference to the document
                                        Paper.init(getApplicationContext());
                                        Paper.book().destroy();
                                        Paper.book().write("Email", email);
                                        Paper.book().write("Password", password);
                                        Paper.book().write("isLoggedIn", "true");
                                        Paper.book().write("type", "student");

                                        pDialog.dismissWithAnimation();
                                        startActivity(new Intent(getApplicationContext(), HomePage_std.class));

                                    }//end if
                                }
                            })//end onComplete
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pDialog.dismissWithAnimation();
                                    Toast.makeText(MainActivity.this, "something wrong, please login to your student account", Toast.LENGTH_SHORT).show();
                                }
                            });
                }//end else if(type.equals("student")){

        }//if(isLoggedIn)


        std=findViewById(R.id.std);
        teacher=findViewById(R.id.teacher);

        std.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Login_std.class);
            startActivity(intent);
        });

        teacher.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Login_lecturer.class);
            startActivity(intent);
        });


    }//end onCreate

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            finish();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
            // The above code sets doubleBackToExitPressedOnce back to false after 2 seconds (2000 milliseconds).
            // Adjust the delay as needed.
        }
    }//end onBackPressed

    private boolean isNetworkAvailable() {
        Context context = getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new KAlertDialog(this, KAlertDialog.ERROR_TYPE,false)
                .setTitleText("No Internet Connection")
                .setContentText("Please connect to an internet network to proceed.")

                .setConfirmClickListener("OK", new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        finish();
                    }
                })
                .show();
    }

}//end class