package com.it.attendance.Adapters.ClassDetailLeacturer;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.R;
import com.it.attendance.lecturer.Class_Detail_lecturer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.paperdb.Paper;

public class TakeAttendanceAdapter extends RecyclerView.Adapter<TakeAttendanceAdapter.ViewHolder> {

    private List<stdShow> studentList;
    FirebaseFirestore db;
    Context context;



    public TakeAttendanceAdapter(Context context,List<stdShow> studentList) {
        this.studentList = studentList;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.take_attendance_info_student, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        stdShow student = studentList.get(position);
        holder.nameText.setText(student.getName());
        holder.emailText.setText(student.getEmail().substring(0,student.getEmail().indexOf("@")));
        Paper.init(context);

        String cNumber= Paper.book().read("courseNumber");

        Log.d("take_attendance_info_student_dialog",cNumber);
        String today =getDate();
        Map<String, Object> data = new HashMap<>();
        data.put("day", today.substring(0,2));
        data.put("month",today.substring(3,5));
        data.put("year",today.substring(6,10));
        data.put("Email",student.getEmail());
        data.put("name",student.getName());
        db = FirebaseFirestore.getInstance();

        //get attendance status
        DocumentReference docRef = db.collection("attendance")
                .document(cNumber).collection(student.getEmail()).document(getDate());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "Document exist");

                        boolean boolValue = document.getBoolean("IsPresent");
                       data.put("IsPresent",boolValue);
                        Log.e("bool value",String.valueOf(boolValue));
                        holder.checkBox.setChecked(boolValue); // Set the checkbox state based on the retrieved value
                    } else {
                        data.put("IsPresent",false);
                        // Handle the case where the document doesn't exist
                        Log.d("TAG", "Document does not exist, the email "+student.getEmail()+" set to absent");

                    }
                    db = FirebaseFirestore.getInstance();
                    //document reference for attendance
                    CollectionReference collectionRef = db.collection("attendance").document(cNumber).collection(student.getEmail());
                    collectionRef.document(today).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("TakeAttendanceAdapter", "auto attendance set");
                        }
                    }) .addOnFailureListener(e -> {
                        Log.e("TAG", "Error set auto attendance", e);
                        // Handle errors gracefully (e.g., display error message to user)
                    });

                } else {
                    // Handle any errors that occurred during the get operation
                    Log.d("TAG", "Error getting document: ", task.getException());
                }
            }
        });


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()){
                    data.put("IsPresent",true);
                }else {
                    data.put("IsPresent", false);
                }
                db = FirebaseFirestore.getInstance();

                //document reference for attendance
                CollectionReference collectionRef = db.collection("attendance").document(cNumber).collection(student.getEmail());
                collectionRef.document(today).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("TAG", "Attendance updated successfully!");
                    }
                }) .addOnFailureListener(e -> {
                    Log.e("TAG", "Error updating attendance", e);
                    // Handle errors gracefully (e.g., display error message to user)
                });

            Log.w("taaaaaaag inside func",String.valueOf(holder.checkBox.isChecked()));

            }

        });


    }//end onBindViewHolder

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText,emailText;
        private CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.student_name_detail_adapter);
            emailText = itemView.findViewById(R.id.student_regNo_detail_adapter);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }


    String getDate(){
        Calendar mCalendar = Calendar.getInstance();
        Calendar calendars = Calendar.getInstance();
        Locale englishLocale = Locale.ENGLISH; // Or Locale.US for US English


        int day = calendars.get(Calendar.DAY_OF_MONTH);
        int month =calendars.get(Calendar.MONTH);
        int year =calendars.get(Calendar.YEAR);


        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);


        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy",englishLocale);
        dateFormat.setCalendar(mCalendar);
        Date today = Calendar.getInstance().getTime();
        String selectedDate = dateFormat.format(today);
        //Toast.makeText(context,selectedDate,Toast.LENGTH_SHORT).show();
        Log.e("date",selectedDate);

        return selectedDate;
    }

}//end class



