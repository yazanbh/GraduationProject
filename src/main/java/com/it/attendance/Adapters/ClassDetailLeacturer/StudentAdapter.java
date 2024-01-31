package com.it.attendance.Adapters.ClassDetailLeacturer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.it.attendance.R;
import com.it.attendance.lecturer.Class_Detail_lecturer;

import org.checkerframework.checker.optional.qual.Present;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<stdShow> studentList;
    FirebaseFirestore db;
    Context context;


    public StudentAdapter(Context context,List<stdShow> studentList) {
        this.studentList = studentList;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lecturer_student_details_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        stdShow student = studentList.get(position);
        holder.nameText.setText(student.getName());
        holder.emailText.setText(student.getEmail());
        Paper.init(context);

        String cNumber= Paper.book().read("courseNumber");

Log.e("yazaaaaaaaaaaan",cNumber);
        db = FirebaseFirestore.getInstance();

        //list to get IsPresent Boolean if equal to true or false
        List<Boolean> presentList = new ArrayList<>();
        presentList.add(true);
        presentList.add(false);

        //document reference for attendance
        CollectionReference collectionRef = db.collection("attendance").document(cNumber).collection(student.getEmail());

        Query query = collectionRef.whereIn("IsPresent",presentList);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            int present=0;
                            int absent=0;

                            for (DocumentSnapshot document : task.getResult()) {
                                boolean isPresent = document.getBoolean("IsPresent");
                                Log.e("successfully", "Adapter done");

                                if (isPresent) {
                                    present = present + 1;
                                    Log.e("successfully", "present done" + String.valueOf(present));
                                }//end if
                                else {
                                    absent = absent + 1;
                                    Log.e("successfully", "absent done" + String.valueOf(absent));
                                }//end else
                            }//end for loop

                            holder.Present.setText(String.valueOf(present));
                            holder.Absent.setText(String.valueOf(absent));

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("successfully", "Adapter failuer");

                    }
                });



    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText,emailText,Absent,Present;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.student_name_detail_adapter);
            emailText = itemView.findViewById(R.id.student_regNo_detail_adapter);
            Absent=itemView.findViewById(R.id.absent);
            Present=itemView.findViewById(R.id.present);
        }
    }
}

