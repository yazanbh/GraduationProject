package com.it.attendance.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.MainActivity;
import com.it.attendance.R;
import com.it.attendance.student.HomePage_std;
import com.it.attendance.student.Login_std;
import com.saadahmedsoft.popupdialog.PopupDialog;
import com.saadahmedsoft.popupdialog.Styles;
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener;

import io.paperdb.Paper;


public class profile extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_profile);
        context =getApplicationContext();
        //initialize FireStore
        db = FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //go to another page from navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));
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
            docRef = db.collection("lecturer").document(user);
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

            PopupDialog.getInstance(this)
                    .setStyle(Styles.STANDARD)
                    .setHeading("        Confirm Logout")
                    .setDescription("Are you sure you want to logout?")
                    .setPopupDialogIcon(R.drawable.logout)
                    .setPopupDialogIconTint(R.color.red_new)
                    .setCancelable(false)
                    .setPositiveButtonText("Confirm")
                    .showDialog(new OnDialogButtonClickListener() {
                        @Override
                        public void onPositiveClicked(Dialog dialog) {
                            super.onPositiveClicked(dialog);

                            Paper.init(getApplicationContext());
                            Paper.book().write("isLoggedIn", "false");
                            Paper.book().write("type", "student");

                            auth.signOut();
                            Intent intent = new Intent(getApplicationContext(), Login_lecturer.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            finishAffinity();
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();


                        }

                        @Override
                        public void onNegativeClicked(Dialog dialog) {
                            super.onNegativeClicked(dialog);
                        }
                    });


        });//end Logout image event

        //go back page
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {            // Sign out the current user
            onBackPressed();
        });

        //verified the email address
        LinearLayout verf= findViewById(R.id.verification);
        verf.setOnClickListener(view ->{
            checkAndSendVerificationEmail();
        });//end linearlayout

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser.isEmailVerified()){
            ImageView imageView =findViewById(R.id.checkverf);
            imageView.setImageResource(R.drawable.checkmark);
        }
        else{

            ImageView imageView =findViewById(R.id.checkverf);
            imageView.setImageResource(0);
        }


        //change password
        LinearLayout changePass = findViewById(R.id.changePass);
        changePass.setOnClickListener(view ->{
            auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
               /*     PopupDialog.getInstance(this)
                            .setStyle(Styles.IOS)
                            .setHeading("Password reset email sent!")
                            .setDescription("check your email box")
                            .setCancelable(false)
                            .setPositiveButtonText("Ok")

                            .showDialog(new OnDialogButtonClickListener() {
                                @Override
                                public void onPositiveClicked(Dialog dialog) {
                                    super.onPositiveClicked(dialog);
                                }
                            });*/
                    
                  //  Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error in sending password reset email", Toast.LENGTH_SHORT).show();
                }
            });//end sendPasswordResetEmail


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

                PopupDialog.getInstance(this)
                        .setStyle(Styles.ALERT)
                        .setDescription("Email is already verified")
                        .setCancelable(false)
                        .showDialog(new OnDialogButtonClickListener() {
                            @Override
                            public void onDismissClicked(Dialog dialog) {
                                super.onDismissClicked(dialog);
                            }
                        });//end dialog
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
        startActivity(new Intent(this, lecturer_Home_Page.class));
        overridePendingTransition(0, 0);
    }

}//end Class