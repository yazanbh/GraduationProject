package com.it.attendance;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.it.attendance.lecturer.Login_lecturer;
import com.it.attendance.student.Login_std;

public class forgot_password extends AppCompatActivity {
    EditText Email;
    private FirebaseAuth mAuth;
    Button reset;
    ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        mAuth = FirebaseAuth.getInstance();
        Email=findViewById(R.id.email_account);
        reset=findViewById(R.id.reset_pass);
        reset.setOnClickListener(view -> {
        if(Email.getText().toString().equals("")){
            Toast.makeText(forgot_password.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        }
        else{
            //onComplete
            mAuth.sendPasswordResetEmail(Email.getText().toString()).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(forgot_password.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(forgot_password.this, MainActivity.class));
                }else{
                    Toast.makeText(forgot_password.this, "Error in sending password reset email", Toast.LENGTH_SHORT).show();
                }
            });//end sendPasswordResetEmail
        }//end outer else

            });//end onClickListener


        String sourcePage = getIntent().getStringExtra("source_page");
        backButton=findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if (sourcePage.equals("StudentLogin")) {
                startActivity(new Intent(this, Login_std.class));
            } else if (sourcePage.equals("LecturerLogin")) {
                startActivity(new Intent(this, Login_lecturer.class));
            }

        });

    }//end onCreate

}//end class

