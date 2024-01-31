package com.it.attendance.Adapters.Students;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.attendance.Adapters.CoursesHomePageLecturer.course;
import com.it.attendance.R;

import java.util.ArrayList;

public class stdAdapter extends RecyclerView.Adapter<stdAdapter.MyViewHolder> {

    Context context;
    ArrayList<Course> coursesArraylist;
    private final HomePage_std_interFace homePage_std_interFace;

    public stdAdapter(Context context, ArrayList<Course> courses,HomePage_std_interFace homePage_std_interFace) {
        this.context = context;
        this.coursesArraylist = courses;
        this.homePage_std_interFace = homePage_std_interFace;
    }

    @NonNull
    @Override
    public stdAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview_course,parent,false);
        return new stdAdapter.MyViewHolder(v,homePage_std_interFace);
    }

    @Override
    public void onBindViewHolder(@NonNull stdAdapter.MyViewHolder holder, int position) {
        Course course = coursesArraylist.get(position);
        holder.CourseName.setText(course.getCname());
        holder.CourseID.setText(course.getcNumber());
        holder.CourseSection.setText("section "+course.getcSection());
    }

    @Override
    public int getItemCount() {
        return coursesArraylist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView CourseName,CourseID,CourseSection;

        public MyViewHolder(@NonNull View itemView,HomePage_std_interFace homePage_std_interFace) {
            super(itemView);

            CourseName= itemView.findViewById(R.id.CourseName_adapter);
            CourseID= itemView.findViewById(R.id.CourseID_adapter);
            CourseSection= itemView.findViewById(R.id.CourseSec_adapter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(homePage_std_interFace != null){
                        int pos=getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            homePage_std_interFace.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
