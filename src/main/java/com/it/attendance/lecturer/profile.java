package com.it.attendance.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.R;

import io.paperdb.Paper;


public class profile extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    Context context;
    KAlertDialog pDialogSuccess,pDialogWarining,pDialogProgress;

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
        logout.setOnClickListener(view -> {
            // Sign out the current user
            pDialogWarining = new KAlertDialog(this, KAlertDialog.WARNING_TYPE,false);
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
                    Intent intent = new Intent(getApplicationContext(), Login_lecturer.class);
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
        LinearLayout verf= findViewById(R.id.verification);
        verf.setOnClickListener(view ->{
            pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
            pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogProgress.setTitleText("Loading");
            pDialogProgress.setCancelable(false);
            pDialogProgress.show();
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
            pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
            pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogProgress.setTitleText("Loading");
            pDialogProgress.setCancelable(false);
            pDialogProgress.show();

            pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE,false);
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
                if(task.isSuccessful()){
                    pDialogProgress.dismissWithAnimation();
                    pDialogSuccess.show();
                }else{
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

            pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE,false);
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
        startActivity(new Intent(this, lecturer_Home_Page.class));
        overridePendingTransition(0, 0);
    }

}//end Class