package com.it.attendance;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.lecturer.lecturer_Home_Page;
import com.it.attendance.student.HomePage_std;

import io.paperdb.Paper;

public class SplashScreen extends AppCompatActivity {

    int Delay = 2500;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mAuth = FirebaseAuth.getInstance();
        Paper.init(getApplicationContext());

        String email= Paper.book().read("Email","cc");
        String password =Paper.book().read("Password");
        String isLoggedIn = Paper.book().read("isLoggedIn");
        String type= Paper.book().read("type","");

        Log.e("Paper","email: "+ email +"\n password "+ password +"\n isLogin? "+ isLoggedIn +"\n type :"+type);

        // Simulate a short delay (optional)
        new Handler().postDelayed(() -> {
            if (isLoggedIn!=null && isLoggedIn.equals("true")) {
                // Navigate to main activity after successful auto login
                Delay = 0;
                if(type.equals("teacher")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));

                                    }//end if
                                }//end oncomplete

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SplashScreen.this, "something wrong, please login to your teacher account", Toast.LENGTH_SHORT).show();
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

                                        startActivity(new Intent(getApplicationContext(), HomePage_std.class));
                                        finish();
                                    }//end if
                                }
                            })//end onComplete
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SplashScreen.this, "something wrong, please login to your student account", Toast.LENGTH_SHORT).show();
                                }
                            });
                }//end else if(type.equals("student")){


            } else {
                // Navigate to login activity if not logged in
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, Delay); // Adjust delay if needed




        // Get a reference to the document

        String collection = "students";

        if(type.equals("teacher")){
             collection = "lecturer";

        }
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(collection).document(email);
        // Get the document
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Paper.init(getApplicationContext());
                        // Extract data from the document
                        String Name =document.getString("name");
                        Paper.book().write("name",Name);

                        if(document.getString("card")!=null){
                            Paper.book().write("card",true);
                        }
                        else{
                            Paper.book().write("card",false);
                        }
                        if(document.getString("phone")!=null){
                            String phoneNumber =document.getString("phone");
                            Paper.book().write("phone",phoneNumber.substring(0, 3) + " "
                                    + phoneNumber.substring(3));
                        }

                        // Do something with the data
                        Log.d(ContentValues.TAG, "Document existed");

                    } else {
                        Log.d(ContentValues.TAG, "No such document!");
                    }
                } else {
                    Log.w(ContentValues.TAG, "Error getting document", task.getException());
                }
            }
        });







    }//end onCreate
}//end Class