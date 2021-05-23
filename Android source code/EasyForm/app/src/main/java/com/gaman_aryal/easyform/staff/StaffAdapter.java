package com.gaman_aryal.easyform.staff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private final List<StaffModel> allStaff;
    private final Context context;
    private int Count;
    private final OnStaffListClickedListener onStaffListClickedListener;

    public StaffAdapter(List<StaffModel> allStaff, Context context, OnStaffListClickedListener onStaffListClickedListener) {
        this.allStaff = allStaff;
        this.context = context;
        this.onStaffListClickedListener = onStaffListClickedListener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        holder.setStaffs(allStaff.get(position),onStaffListClickedListener);
    }

    @Override
    public int getItemCount() {
        Count = allStaff.size();
        return Count;
    }

    public class StaffViewHolder extends RecyclerView.ViewHolder {

        TextView name, gender, count, officeID, post, address;
        ImageView photo, staffOpt;
        LinearLayout idSection, addSection, genderSection;
        OnStaffListClickedListener onStaffListClickedListener;

        @SuppressLint("SetTextI18n")
        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);

            count = ((Activity) context).findViewById(R.id.countOfStaff);
            count.setText("("+Count+")");

            name = itemView.findViewById(R.id.sName);
            gender = itemView.findViewById(R.id.sGender);
            photo = itemView.findViewById(R.id.sPhoto);
            officeID = itemView.findViewById(R.id.sOfficeID);
            post = itemView.findViewById(R.id.sPost);
            address = itemView.findViewById(R.id.sAdd);
            staffOpt = itemView.findViewById(R.id.staffOption);

            idSection = itemView.findViewById(R.id.idSection);
            genderSection = itemView.findViewById(R.id.genderSection);
            addSection = itemView.findViewById(R.id.addSection);
        }

        @SuppressLint("SetTextI18n")
        public void setStaffs(final StaffModel eachUser, OnStaffListClickedListener onStaffListClickedListener) {
            this.onStaffListClickedListener = onStaffListClickedListener;

            if (eachUser.getID().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                name.setText(eachUser.getFullName()+" (Me)");
            }else {
                name.setText(eachUser.getFullName());
            }
            gender.setText(eachUser.getGender());
            address.setText(eachUser.getAddress());
            officeID.setText(eachUser.getOfficeID());
            post.setText(eachUser.getPost());
            Picasso.get()
                    .load(eachUser.getProfile_Photo())
                    .into(photo);

            staffOpt.setOnClickListener(v ->
                    onStaffListClickedListener.onStaffOptClicked(
                            eachUser.getID(),
                            eachUser.getFullName(),
                            eachUser.getProfile_Photo()
                    ));

            photo.setOnClickListener(v -> {
                Intent profileIntent = new Intent(context, My_Profile.class);
                profileIntent.putExtra("STAFF_ID", eachUser.getID());
                context.startActivity(profileIntent);
            });
        }
    }

    public interface OnStaffListClickedListener {
        void onStaffOptClicked(String staff_id, String staff_name, String staff_photo_url);
    }
}
