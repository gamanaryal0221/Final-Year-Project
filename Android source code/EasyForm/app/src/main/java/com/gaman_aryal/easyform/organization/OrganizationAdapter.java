package com.gaman_aryal.easyform.organization;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;

import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {

    private final List<OrganizationModel> organizationList;
    private final Context context;
    private final OnOrganizationClickedListener onOrganizationClickedListener;

    public OrganizationAdapter(List<OrganizationModel> organizationList, Context context, OnOrganizationClickedListener onOrganizationClickedListener) {
        this.organizationList = organizationList;
        this.context = context;
        this.onOrganizationClickedListener = onOrganizationClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        view = layoutInflater.inflate(R.layout.single_organization,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(organizationList.get(position),onOrganizationClickedListener);
    }

    @Override
    public int getItemCount() {
        return organizationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView organizationName, organizationAdd;
        Button organizationNameLister;
        OnOrganizationClickedListener onOrganizationClickedListener;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            organizationName = itemView.findViewById(R.id.organizationName);
            organizationAdd = itemView.findViewById(R.id.organizationAdd);

            organizationNameLister = ((Activity)context).findViewById(R.id.organizationChooser);
        }

        @SuppressLint("SetTextI18n")
        public void setData(final OrganizationModel organizations, OnOrganizationClickedListener onOrganizationClickedListener) {

            this.onOrganizationClickedListener = onOrganizationClickedListener;
            organizationName.setText(organizations.getOrganizationName());
            organizationAdd.setText("Address :- "+organizations.getAddress());
            itemView.setOnClickListener(view -> {
                onOrganizationClickedListener.onOrganizationSelected(organizations.getID(),organizations.getOrganizationName());
            });
        }
    }
    public interface OnOrganizationClickedListener{
        void onOrganizationSelected(String org_id, String org_name);
    }
}
