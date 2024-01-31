package com.it.attendance.lecturer;

import static android.provider.Settings.Secure;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.MainActivity;
import com.it.attendance.R;

import java.util.HashMap;
import java.util.Map;

public class Signup_Lecturer extends AppCompatActivity {
    TextView signup;
    EditText Name,Email,pass1,pass2;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_lecturer);
        //get device id to restrict users opens another account
        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //declare variables to save it on firebase
        Name=findViewById(R.id.signup_lec_name);
        Email=findViewById(R.id.signup_lec_email);
        pass1=findViewById(R.id.signup_lec_pass1);
        pass2=findViewById(R.id.signup_lec_pass2);
        signup=findViewById(R.id.signup_lec_btn);
        signup.setOnClickListener(view -> {
            if(validate()){

                mAuth.createUserWithEmailAndPassword(Email.getText().toString(), pass1.getText().toString())
                        .addOnSuccessListener(authResult -> {
                           /* mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){
                                       // User successfully signed up.
                                       Toast.makeText(Sign_Up_Page.this, "user Registered Successfully, Please verify your email. ", Toast.LENGTH_SHORT).show();
                                   }
                               }
                           });*/
                            Toast.makeText(Signup_Lecturer.this, "Lecturer Registered Successfully. ", Toast.LENGTH_SHORT).show();

                            //store into firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference usersRef = db.collection("lecturer");
                            DocumentReference userDocRef = usersRef.document(Email.getText().toString());
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("deviceId", deviceId);
                            updates.put("name", Name.getText().toString());
                            updates.put("email", Email.getText().toString());

                            userDocRef.set(updates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "Device ID saved successfully");

                                } else {
                                    Log.d("TAG", "Error saving device ID: ", task.getException());
                                }
                            });

                            //move to login page
                            Intent intent = new Intent(Signup_Lecturer.this, MainActivity.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Toast.makeText(Signup_Lecturer.this, "cannot sign up", Toast.LENGTH_SHORT).show());
            }//end if


        });//end setOnClickListener
    }//end onCreate
    public boolean validate(){
        boolean t=true;
        String username =Name.getText().toString();
        String pass=pass1.getText().toString();
        String confirmpass=pass2.getText().toString();
        String email=Email.getText().toString();
        if(username.isEmpty() || username.length()<10){
            Name.setError("your username is not valid");
            Name.requestFocus();
            t=false;
        }
        if (email.isEmpty() || !(email.endsWith("@aabu.edu.jo")||email.endsWith("@st.aabu.edu.jo"))){
            Email.setError("Email is not valid");
            Email.requestFocus();
            t=false;
        }
        if(pass.isEmpty() || pass.length()<7){
            pass1.setError("password must be 7 character at least");
            pass1.requestFocus();
            t=false;
        }
        if(confirmpass.isEmpty() || !confirmpass.equals(pass)){
            pass2.setError("password not match!");
            pass2.requestFocus();
            t=false;
        }
        if(pass.isEmpty()&&confirmpass.isEmpty()){
            pass1.setError("password must be 7 character at least");
            pass1.requestFocus();
            pass2.setError("password must be 7 character at least");
            pass2.requestFocus();
            t=false;
        }
        return t;
    }//end function
}//end class