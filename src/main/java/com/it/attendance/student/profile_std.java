package com.it.attendance.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.R;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class profile_std extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FirebaseFirestore db;

    // Get the Firebase auth instance
    private FirebaseAuth auth;
    private AlertDialog mTurnNfcDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_profile);


        //initialize FireStore
        db = FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //go to another page from navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(getApplicationContext(), HomePage_std.class));
                overridePendingTransition(0,0);
                return true;
            } else return item.getItemId() == R.id.profile;
        });
        //display user profile info

        //get email for the current user from FirebaseAuth
        String user= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        //get data for the current user from FireStore
        DocumentReference docRef = null;
        if (user != null) {
            docRef = db.collection("students").document(user);
        }
        if (docRef != null) {
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name").toUpperCase();
                        String id = document.getString("email");
                        // Process the data
                        TextView username = findViewById(R.id.user_name);
                        username.clearComposingText();
                        username.setText(name);
                        TextView userid = findViewById(R.id.u_id);
                        String UID=id.substring(0,id.indexOf("@"));
                        userid.clearComposingText();
                        userid.setText("ID: "+UID);
                        TextView eid = findViewById(R.id.email_id);
                        eid.setText(user);
                    } else {
                        Log.d("TAG", "Document does not exist");
                    }
                } else {
                    Log.d("TAG", "Failed to get document: ", task.getException());
                }
            });
        }//end if doc!=null


        //LogOut
        ImageView logout = findViewById(R.id.LogOut);
        logout.setOnClickListener(view -> {            // Sign out the current user

            Paper.init(getApplicationContext());
            Paper.book().write("isLoggedIn", "false");
            Paper.book().write("type", "student");

            auth.signOut();
            Intent intent = new Intent(getApplicationContext(), Login_std.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finishAffinity();
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        //go back page
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {            // Sign out the current user
          onBackPressed();
        });

        //verified the email address
        LinearLayout verf= findViewById(R.id.verification);
        verf.setOnClickListener(view ->{

            checkAndSendVerificationEmail();
        });

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser.isEmailVerified()){
            ImageView imageView =findViewById(R.id.checkverf);
            imageView.setImageResource(R.drawable.checkmark);
        }


        //change password
        LinearLayout changePass = findViewById(R.id.changePass);
        changePass.setOnClickListener(view ->{
        auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(getApplicationContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show();
                }else{
                Toast.makeText(getApplicationContext(), "Error in sending password reset email", Toast.LENGTH_SHORT).show();
            }
        });//end sendPasswordResetEmail


        });


        //unevirsityID
        LinearLayout UID = findViewById(R.id.UnversityID);
        UID.setOnClickListener(view -> {


        });
    }//end onCreate

    private void checkAndSendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            if (!user.isEmailVerified()) {
                // User is not verified, send verification email
                sendVerificationEmail();
            } else {
                // User is already verified
                Toast.makeText(getApplicationContext(),
                        "Email is already verified",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),
                                        "Verification email sent to " + user.getEmail(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("EmailVerification", "sendEmailVerification", task.getException());
                                Toast.makeText(getApplicationContext(),
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomePage_std.class));
        overridePendingTransition(0, 0);
    }



}//end Class