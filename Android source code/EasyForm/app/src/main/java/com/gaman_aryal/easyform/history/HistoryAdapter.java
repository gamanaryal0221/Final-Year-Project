package com.gaman_aryal.easyform.history;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.staff.All_Staff;
import com.gaman_aryal.easyform.staff.My_Profile;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryModel> allhistory;
    private final Context context;
    private final String organization_id;
    private final boolean isThisOrganization;

    public HistoryAdapter(List<HistoryModel> allhistory, String organization_id, boolean isThisOrganization, Context context) {
        this.allhistory = allhistory;
        this.context = context;
        this.organization_id = organization_id;
        this.isThisOrganization = isThisOrganization;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_history,parent,false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.setData(allhistory.get(position));
    }

    @Override
    public int getItemCount() {
        return allhistory.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView historyContent, historyTime;
        CircleImageView historyPhoto;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            historyContent = itemView.findViewById(R.id.historyContent);
            historyTime = itemView.findViewById(R.id.historyTime);
            historyPhoto = itemView.findViewById(R.id.historyPhoto);
        }

        public void setData(final HistoryModel eachHistory) {
            historyContent.setText(Html.fromHtml(eachHistory.getContent()));
            historyTime.setText(eachHistory.getTime());
            Picasso.get()
                    .load(eachHistory.getPhoto())
                    .into(historyPhoto);

            historyPhoto.setOnClickListener(v -> {
                Intent profileIntent = new Intent(context, My_Profile.class);
                profileIntent.putExtra("STAFF_ID", eachHistory.getStaffID());
                context.startActivity(profileIntent);
            });
            itemView.setOnClickListener(v -> {

                switch (eachHistory.getType()){
                    case "Pending":
                        if (isThisOrganization) moveToStaffList("REQUESTS");
                        break;
                    case "Deleted":
                        if (isThisOrganization) moveToStaffList("DELETED_REQUESTS");
                        break;
                    case "Approved":
                        moveToStaffList("STAFFS");
                        break;
                    case "Fired":
                        moveToStaffList("FIRED_STAFFS");
                        break;
                }

            });
        }

        private void moveToStaffList(String type_of_user) {

            Intent staff_list = new Intent(context, All_Staff.class);
            staff_list.putExtra("ORGANIZATION_ID",organization_id);
            staff_list.putExtra("IS_THIS_ORGANIZATION",isThisOrganization);
            staff_list.putExtra("TYPE_OF_USER",type_of_user);
            context.startActivity(staff_list);
        }
    }
}
