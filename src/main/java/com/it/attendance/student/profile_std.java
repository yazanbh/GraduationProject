package com.it.attendance.student;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.it.attendance.R;

import io.paperdb.Paper;

public class profile_std extends AppCompatActivity {
    ChipNavigationBar bottomNavigationView;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    Context context;
    KAlertDialog pDialogSuccess, pDialogWarining, pDialogProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_profile);
        context = getApplicationContext();
        //initialize FireStore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setItemSelected(R.id.profile,true);
        new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.profile,false), 3000);

        //go to another page from navbar
        bottomNavigationView.setOnItemSelectedListener(i -> {
            if(i==R.id.home){
                startActivity(new Intent(getApplicationContext(), HomePage_std.class));
                overridePendingTransition(0, 0);
            }
            else{
                bottomNavigationView.setItemSelected(R.id.profile,true);
                new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.home,false), 3000);

            }
        }
        );

        //display user profile info

        Paper.init(getApplicationContext());
        //set user email
        String Email =Paper.book().read("Email");
        TextView eid = findViewById(R.id.email_id);
        eid.setText(Email);

        //set user id
        TextView userid = findViewById(R.id.u_id);
        String UserID = Email.substring(0, Email.indexOf("@"));
        userid.setText("ID: " + UserID);


        //set username
        TextView username = findViewById(R.id.user_name);
        String uname =Paper.book().read("name");
        username.setText(uname);


        //set user phone
        TextView phone = findViewById(R.id.phone);
        String phoneNumber = Paper.book().read("phone");
        phone.setText(phoneNumber);


        //LogOut
        ImageView logout = findViewById(R.id.LogOut);
        logout.setOnClickListener(view -> {
            // Sign out the current user
            pDialogWarining = new KAlertDialog(this, KAlertDialog.WARNING_TYPE, false);
            pDialogWarining.setTitleText("Logout");
            pDialogWarining.confirmButtonColor(R.color.blue);
            pDialogWarining.cancelButtonColor(R.color.blue);
            pDialogWarining.setContentText("Are you sure you want to logout?");
            pDialogWarining.setCancelClickListener("Cancel", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogWarining.dismissWithAnimation();
                }
            });
            pDialogWarining.setConfirmClickListener("Logout", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    Paper.init(getApplicationContext());
                    Paper.book().write("isLoggedIn", "false");
                    auth.signOut();
                    Intent intent = new Intent(getApplicationContext(), Login_std.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    finishAffinity();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    pDialogWarining.dismissWithAnimation();
                }
            });
            pDialogWarining.show();

        });//end Logout image event


        //go back page
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {            // back to homepage
            onBackPressed();
        });


        //verified the email address
        LinearLayout verf = findViewById(R.id.verification);
        verf.setOnClickListener(view -> {
            pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogProgress.setTitleText("Loading");
            pDialogProgress.setCancelable(false);
            pDialogProgress.show();
            checkAndSendVerificationEmail();
        });//end linearlayout


        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser.isEmailVerified()) {
            ImageView imageView = findViewById(R.id.checkverf);
            imageView.setImageResource(R.drawable.checkmark);
        } else {

            ImageView imageView = findViewById(R.id.checkverf);
            imageView.setImageResource(0);
        }

        //add card and phone number
        LinearLayout UID = findViewById(R.id.UnversityID);
        boolean card = Paper.book().read("card");
        if(card){
            UID.setVisibility(View.GONE);
        }
        UID.setOnClickListener(view->{
            startActivity(new Intent(this, CardNumber_std.class));
            overridePendingTransition(0, 0);
        });



        //change password
        LinearLayout changePass = findViewById(R.id.changePass);
        changePass.setOnClickListener(view -> {
            pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogProgress.setTitleText("Loading");
            pDialogProgress.setCancelable(false);
            pDialogProgress.show();

            pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE, false);
            pDialogSuccess.setTitleText("Successfully");
            pDialogSuccess.setContentText("Check your mailbox to reset your password!");
            pDialogSuccess.confirmButtonColor(R.color.blue);
            pDialogSuccess.setConfirmClickListener("Ok", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogSuccess.dismissWithAnimation();
                }
            });

            auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    pDialogProgress.dismissWithAnimation();
                    pDialogSuccess.show();
                } else {
                    pDialogProgress.dismissWithAnimation();
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
                pDialogProgress.dismissWithAnimation();
                Toast.makeText(getApplicationContext(), "Email is already verified",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE, false);
            pDialogSuccess.setTitleText("Successfully");
            pDialogSuccess.setContentText("Verification email sent to " + user.getEmail().toString());
            pDialogSuccess.confirmButtonColor(R.color.blue);
            pDialogSuccess.setConfirmClickListener("Ok", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogSuccess.dismissWithAnimation();
                }
            });


            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pDialogProgress.dismissWithAnimation();
                                pDialogSuccess.show();
                            } else {
                                pDialogProgress.dismissWithAnimation();

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
        finish();
    }



    }//end Class
