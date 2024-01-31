package com.it.attendance.Adapters.CoursesHomePageLecturer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.attendance.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<course> coursesArraylist;
    private final RecyclerViewInterface recyclerViewInterface;

    public MyAdapter(Context context, ArrayList<course> courses,RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.coursesArraylist = courses;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview_course,parent,false);
        return new MyViewHolder(v,recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        course Course = coursesArraylist.get(position);
        holder.CourseName.setText(Course.cName);
        holder.CourseID.setText(Course.cNumber);
        holder.CourseSection.setText("section "+Course.cSection);
    }

    @Override
    public int getItemCount() {
        return coursesArraylist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView CourseName,CourseID,CourseSection;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            CourseName= itemView.findViewById(R.id.CourseName_adapter);
            CourseID= itemView.findViewById(R.id.CourseID_adapter);
            CourseSection= itemView.findViewById(R.id.CourseSec_adapter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos=getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
