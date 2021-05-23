package com.gaman_aryal.easyform.history;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class History extends AppCompatActivity implements View.OnClickListener{

    ProgressDialog pd;

    String ORGANIZATION_ID, History_Type;
    boolean IS_THIS_ORGANIZATION;
    DatabaseReference realtimeReference;

    List<HistoryModel> allHistories;
    RecyclerView recyclerView;
    TextView noHistories;
    HistoryAdapter historyAdapter;

    TextView historyTitle, filterer, ALL, REQUESTS, APPROVED, DELETED, FIRED;
    LinearLayout filterOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_history);

        if (getIntent().getExtras() != null) {
            ORGANIZATION_ID = getIntent().getExtras().getString("ORGANIZATION_ID");
            IS_THIS_ORGANIZATION = getIntent().getExtras().getBoolean("IS_THIS_ORGANIZATION");

            realtimeReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(ORGANIZATION_ID +"/HISTORY");
        }
        pd = new ProgressDialog(this);

        allHistories = new ArrayList<>();

        init();
        settingRecyclerView();
        pd.setTitle("Show History");
        pd.setMessage("extracting all history ...");
        pd.show();
        History_Type = "All";
        filterHistory();

    }

    private void init() {
        historyTitle = findViewById(R.id.historyTitle);
        filterer = findViewById(R.id.filterer);
        filterer.setText(R.string.filter_his);
        filterer.setOnClickListener(this);
        filterOpt = findViewById(R.id.filterOpt);

        noHistories = findViewById(R.id.noHistories);

        ALL = findViewById(R.id.ALL);
        REQUESTS = findViewById(R.id.REQUESTS);
        APPROVED = findViewById(R.id.APPROVED);
        DELETED = findViewById(R.id.DELETED);
        FIRED = findViewById(R.id.FIRED);

        ALL.setOnClickListener(this);
        REQUESTS.setOnClickListener(this);
        APPROVED.setOnClickListener(this);
        DELETED.setOnClickListener(this);
        FIRED.setOnClickListener(this);
        changeColor("#041DFF","#4E4C4C","#4E4C4C","#4E4C4C","#4E4C4C", R.string.all_history);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filterer:
                if (filterOpt.getVisibility()==View.VISIBLE){
                    closeFilterOptions();
                }else {
                    filterer.setText(R.string.close_filter_opt);
                    filterOpt.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.ALL:
                showProgress("Show All History", "fetching all history");
                changeColor("#041DFF","#4E4C4C","#4E4C4C","#4E4C4C","#4E4C4C", R.string.all_history);
                History_Type = "All";
                filterHistory();
                break;
            case R.id.REQUESTS:
                showProgress("Filter History", "fetching all requests history");
                changeColor("#4E4C4C","#041DFF","#4E4C4C","#4E4C4C","#4E4C4C", R.string.requests);
                History_Type = "Pending";
                filterHistory();
                break;
            case R.id.APPROVED:
                showProgress("Filter History", "fetching all approved requests history");
                changeColor("#4E4C4C","#4E4C4C","#041DFF","#4E4C4C","#4E4C4C", R.string.approved);
                History_Type = "Approved";
                filterHistory();
                break;
            case R.id.DELETED:
                showProgress("Filter History", "fetching all deleted requests history");
                changeColor("#4E4C4C","#4E4C4C","#4E4C4C","#041DFF","#4E4C4C", R.string.deleted);
                History_Type = "Deleted";
                filterHistory();
                break;
            case R.id.FIRED:
                showProgress("Filter History", "fetching all fired history");
                changeColor("#4E4C4C","#4E4C4C","#4E4C4C","#4E4C4C","#041DFF", R.string.fired);
                History_Type = "Fired";
                filterHistory();
                break;
        }

    }

    private void closeFilterOptions() {
        filterer.setText(R.string.filter_his);
        filterOpt.setVisibility(View.GONE);
    }

    private void showProgress(String pTitle, String pMsg) {
        pd.setTitle(pTitle);
        pd.setMessage(pMsg);
        pd.show();
    }

    private void settingRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        historyAdapter = new HistoryAdapter(allHistories, ORGANIZATION_ID, IS_THIS_ORGANIZATION, this);
    }
    public void refresh(View view) {
        Toast.makeText(this,
                getString(R.string.refreshing),
                Toast.LENGTH_SHORT)
                .show();

        filterHistory();
    }

    public void changeColor(String all, String request, String approved, String not_approved, String fired, int title){
        historyTitle.setText(title);
        ALL.setTextColor(Color.parseColor(all));
        REQUESTS.setTextColor(Color.parseColor(request));
        APPROVED.setTextColor(Color.parseColor(approved));
        DELETED.setTextColor(Color.parseColor(not_approved));
        FIRED.setTextColor(Color.parseColor(fired));
    }
    public void filterHistory(){
        closeFilterOptions();
        realtimeReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allHistories.clear();
                for (DataSnapshot historyDataContainer : snapshot.getChildren()) {

                    String staff_id = historyDataContainer.child("ID").getValue(String.class);

                    FirebaseDatabase.getInstance().getReference("ALL_STAFFS/"+staff_id)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    new HistorySetter(staff_id,
                                            snapshot.child("FullName").getValue(String.class),
                                            snapshot.child("Profile_Photo").getValue(String.class),
                                            historyDataContainer.child("Type").getValue(String.class),
                                            historyDataContainer.child("Time").getValue(String.class),
                                            History_Type).execute();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    someThingWentWrong();
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                someThingWentWrong();
            }
        });
        return;
    }

    @SuppressLint("StaticFieldLeak")
    public class HistorySetter extends AsyncTask<Void,Void,Void>{

        String StaffID, Name, Photo, History_Type, History_Time, Filter_History_Type;

        public HistorySetter(String staffID, String name, String photo, String history_type, String history_time, String filter_history_type){
            StaffID = staffID;
            Name = name;
            Photo = photo;
            History_Type = history_type;
            History_Time = history_time;
            Filter_History_Type = filter_history_type;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            HistoryModel eachHistory = new HistoryModel();

            eachHistory.setStaffID(StaffID);
            eachHistory.setPhoto(Photo);
            eachHistory.setTime(History_Time);
            eachHistory.setType(History_Type);

            if (Filter_History_Type.equals("All")){
                eachHistory.setContent(historyContentResolver(History_Type,Name));
                allHistories.add(eachHistory);
            }else {
                if (Filter_History_Type.equals(History_Type)){
                    eachHistory.setContent(historyContentResolver(History_Type,Name));
                    allHistories.add(eachHistory);
                }
            }
            return null;
        }
        private String historyContentResolver(String history_type, String name) {
            switch (history_type){
                case "Pending":
                    return "<b>"+name+" </b>"+getString(R.string.request_text);
                case "Deleted":
                    return "<b>"+name+"</b>"+getString(R.string.deleted_text);
                case "Approved":
                    return "<b>"+name+" </b>"+getString(R.string.approved_text);
                case "Fired":
                    return "<b>"+name+"</b>"+getString(R.string.fired_text);
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (allHistories.size() == 0) {
                showNoHistoryError();
            }else {
                noHistories.setVisibility(View.GONE);
                recyclerView.setAdapter(historyAdapter);
                historyAdapter.notifyDataSetChanged();
                pd.dismiss();
            }
        }
    }

    private void showNoHistoryError() {
        pd.dismiss();
        noHistories.setVisibility(View.VISIBLE);
        return;
    }
    public void closeHistory(View view) {
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        if (filterOpt.getVisibility()==View.VISIBLE){
            closeFilterOptions();
        }else {
            super.onBackPressed();
        }
    }
    private void someThingWentWrong() {
        pd.dismiss();
        Toast.makeText(this,
                getString(R.string.smth_wrong),
                Toast.LENGTH_SHORT)
                .show();
    }
}