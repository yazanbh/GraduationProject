package com.it.attendance.lecturer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.it.attendance.MainActivity;
import com.it.attendance.R;
import com.it.attendance.forgot_password;

import io.paperdb.Paper;

public class Login_lecturer extends AppCompatActivity {
    EditText email,pass;
    Button btn;
    TextView signup,ForgotPass;
    FirebaseAuth mAuth;
    KAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_teacher);
        //initilize the variables
        email=findViewById(R.id.teacher_email);
        pass=findViewById(R.id.teacher_password);
        signup=findViewById(R.id.lec_signup);
        signup.setOnClickListener(view -> {
            Intent intent = new Intent(Login_lecturer.this, Signup_Lecturer.class);
            startActivity(intent);
        });
        btn=findViewById(R.id.teacher_signin);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //move from login page to start student or lecturer page after validation
        btn.setOnClickListener(view ->{
        pDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
                LoginUser();

        });

        //forgot password
        ForgotPass=findViewById(R.id.lec_ForgotPassword);
        ForgotPass.setOnClickListener(View->{
            Intent intent = new Intent(Login_lecturer.this, forgot_password.class);
            intent.putExtra("source_page", "LecturerLogin");

            startActivity(intent);
        });

    }//end on create

    private void LoginUser(){
        // get string of email and password and remove any space
        String uemail = email.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if(validate()){
            // Perform Firebase Authentication
            mAuth.signInWithEmailAndPassword(uemail.trim(), password.trim())
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Paper.init(getApplicationContext());
                            Paper.book().destroy();
                            Paper.book().write("Email", uemail.trim());
                            Paper.book().write("Password", password.trim());
                            Paper.book().write("isLoggedIn", "true");
                            Paper.book().write("type", "teacher");
                         //   Toast.makeText(Login_lecturer.this, "Authentication successfully.", Toast.LENGTH_SHORT).show();
                            pDialog.dismissWithAnimation();
                        startActivity(new Intent(Login_lecturer.this, lecturer_Home_Page.class));

                        } else {
                            pDialog.dismissWithAnimation();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login_lecturer.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }//end if

    }//end login function

    public boolean validate(){
        //dialog.dismiss();
        boolean t=true;
        String password=pass.getText().toString();
        String emaill=email.getText().toString();
        if (emaill.isEmpty() || !(emaill.trim().endsWith("@aabu.edu.jo"))){
            email.setError("Email is not valid");
            email.requestFocus();
            t=false;
        }
        if(password.isEmpty() || password.length()<7){
            pass.setError("password must be 7 character at least");
            pass.requestFocus();
            t=false;
        }

        return t;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
    }


}//end class