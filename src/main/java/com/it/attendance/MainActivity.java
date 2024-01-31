package com.it.attendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.it.attendance.lecturer.Login_lecturer;
import com.it.attendance.lecturer.lecturer_Home_Page;
import com.it.attendance.student.HomePage_std;
import com.it.attendance.student.Login_std;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    CardView teacher,std;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_your_role);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
       Paper.init(getApplicationContext());

       String email= Paper.book().read("Email");
       String password =Paper.book().read("Password");
       String isLoggedIn = Paper.book().read("isLoggedIn");
       String type= Paper.book().read("type");

        Log.e("Paper","email: "+ email +"\n password "+ password +"\n isLogin? "+ isLoggedIn +"\n type :"+type);
        if(isLoggedIn!=null && isLoggedIn.equals("true")){
                if(type.equals("teacher")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));

                                    }//end if
                                }//end oncomplete
                            });
                }//end if(type.equals("teacher"))
            else if(type.equals("student")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(), HomePage_std.class));

                                    }//end if
                                }//end oncomplete
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
    }

}//end class