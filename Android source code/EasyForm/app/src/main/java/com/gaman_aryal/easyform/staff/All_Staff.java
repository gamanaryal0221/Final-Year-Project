package com.gaman_aryal.easyform.staff;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.gaman_aryal.easyform.home.Home.displayToast;

public class All_Staff extends AppCompatActivity implements View.OnClickListener,
        StaffAdapter.OnStaffListClickedListener {

    String ORGANIZATION_ID, TYPE_OF_USER, TITLE;
    boolean IS_THIS_ORGANIZATION = false;

    ProgressDialog progressDialog;

    DatabaseReference databaseReference;

    TextView staffPageTitle, countOfStaff, showOtherStaffList, errorTextOnListOfStaffs;
    LinearLayout anotherShower;
    RecyclerView recyclerViewOfStaff;

    StaffAdapter staffAdapter;
    List<StaffModel> allStaffs;

    private String LISTENED_ID_OF_STAFF, LISTENED_NAME_OF_STAFF;

    View bottomSheetView;
    BottomSheetBehavior bottomSheetBehavior;

    LinearLayout greenOpt, redOpt, blackOpt;
    CircleImageView drawer_photo;
    ImageView cancel, redOptIcon, blackOptIcon, greenOptIcon;
    TextView purpose, below_purpose, dates, greenOptText, redOptText, blackOptText;

    SharedPreferences SP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_staffs);

        progressDialog = new ProgressDialog(this);
        allStaffs = new ArrayList<>();

        getFromIntent();
        init();
        loadAllList();

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP = getSharedPreferences("EASYFORM", Context.MODE_PRIVATE);
    }

    private void getFromIntent() {

        if (getIntent().getExtras() != null) {
            ORGANIZATION_ID = getIntent().getExtras().getString("ORGANIZATION_ID");
            IS_THIS_ORGANIZATION = getIntent().getExtras().getBoolean("IS_THIS_ORGANIZATION");
            TYPE_OF_USER = getIntent().getExtras().getString("TYPE_OF_USER");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(ORGANIZATION_ID);

                // Special case //
        showOtherStaffList = findViewById(R.id.showOtherStaffList);
        anotherShower = findViewById(R.id.anotherShower);
        switch (TYPE_OF_USER){
            case "STAFFS":
                TITLE = "All Staffs";
                showOtherStaffList.setText(R.string.view_fired_staffs);
                anotherShower.setVisibility(View.VISIBLE);
                break;
            case "FIRED_STAFFS":
                TITLE = "Fired Staffs";
                showOtherStaffList.setText("");
                anotherShower.setVisibility(View.GONE);
                break;
            case "DELETED_REQUESTS":
                TITLE = "Deleted Requests";
                showOtherStaffList.setText("");
                anotherShower.setVisibility(View.GONE);
                break;
            case "REQUESTS":
                TITLE = "All Requests";
                showOtherStaffList.setText(R.string.view_deleted_requests);
                anotherShower.setVisibility(View.VISIBLE);
                break;
        }
        // till this //

        showProgressDialog();
    }
    private void showProgressDialog() {
        progressDialog.setTitle(TITLE);
        progressDialog.setMessage("listing all users ...");
        progressDialog.show();
    }

    private void init() {
        staffPageTitle = findViewById(R.id.staffPageTitle);
        staffPageTitle.setText(TITLE);
        staffPageTitle.setOnClickListener(this);

        countOfStaff = findViewById(R.id.countOfStaff);

        showOtherStaffList.setOnClickListener(this);

        errorTextOnListOfStaffs = findViewById(R.id.errorTextOnListOfStaffs);

        setRecyclerView();
        referencingBottomSheet();
    }
    private void setRecyclerView() {
        recyclerViewOfStaff = findViewById(R.id.recyclerViewOfStaff);
        recyclerViewOfStaff.setHasFixedSize(true);

        LinearLayoutManager stafflist = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerViewOfStaff.setLayoutManager(stafflist);

        staffAdapter = new StaffAdapter(allStaffs, this, this);
        recyclerViewOfStaff.setAdapter(staffAdapter);
    }
    private void referencingBottomSheet() {
        bottomSheetView = findViewById(R.id.bottom_sheet_for_staff);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        cancel = findViewById(R.id.cancel);

        drawer_photo = findViewById(R.id.drawer_photo);
        Picasso.get()
                .load(R.drawable.default_photo)
                .into(drawer_photo);

        purpose = findViewById(R.id.purpose);
        below_purpose = findViewById(R.id.below_purpose);
        dates = findViewById(R.id.dates);

        greenOpt = findViewById(R.id.greenOpt);
        greenOptText = findViewById(R.id.greenOptText);
        greenOptIcon = findViewById(R.id.greenOptIcon);

        redOpt = findViewById(R.id.redOpt);
        redOptText = findViewById(R.id.redOptText);
        redOptIcon = findViewById(R.id.redOptIcon);

        blackOpt = findViewById(R.id.blackOpt);
        blackOptText = findViewById(R.id.blackOptText);
        blackOptIcon = findViewById(R.id.blackOptIcon);

        cancel.setOnClickListener(this);
        drawer_photo.setOnClickListener(this);
        dates.setOnClickListener(this);
        greenOpt.setOnClickListener(this);
        redOpt.setOnClickListener(this);
        blackOpt.setOnClickListener(this);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState==BottomSheetBehavior.STATE_COLLAPSED){

                    Picasso.get()
                            .load(R.drawable.default_photo)
                            .into(drawer_photo);
                    purpose.setText("");
                    below_purpose.setText("");
                    dates.setText("");
                    dates.setVisibility(View.GONE);

                    if (blackOpt.getVisibility()==View.VISIBLE){
                        blackOpt.setVisibility(View.GONE);
                        blackOptText.setText("");
                        blackOptIcon.setImageBitmap(null);
                    }

                    if (greenOpt.getVisibility()==View.VISIBLE){
                        greenOpt.setVisibility(View.GONE);
                        greenOptText.setText("");
                        greenOptIcon.setImageBitmap(null);
                    }

                    if (redOpt.getVisibility()==View.VISIBLE){
                        redOpt.setVisibility(View.GONE);
                        redOptText.setText("");
                        redOptIcon.setImageBitmap(null);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        return;
    }

    private void loadAllList() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(TYPE_OF_USER).exists()){

                    allStaffs.clear();
                    countOfStaff.setText("");
                    errorTextOnListOfStaffs.setText("");

                    for (DataSnapshot dataSnapshot : snapshot.child(TYPE_OF_USER).getChildren()) {
                        IDHolder eachStaffID = dataSnapshot.getValue(IDHolder.class);
                        if (eachStaffID != null) {
                            FirebaseDatabase.getInstance().getReference("ALL_STAFFS")
                                    .child(eachStaffID.getID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    new All_Staff.SetListOfUser(snapshot).execute();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    showErrorText();
                                }
                            });
                        }else {
                            showNullText();
                        }
                    }

                }else {
                    showNullText();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorText();
            }
        });

        if (!isNetworkConnected()){
            if (allStaffs.size()==0) errorTextOnListOfStaffs.setText(R.string.no_network);
            displayToast(getString(R.string.no_network), All_Staff.this);
        }
        return;
    }

    public class SetListOfUser extends AsyncTask<Void, Integer, Void> {

        DataSnapshot snapshot;

        public SetListOfUser (DataSnapshot dataSnapshot){
            snapshot = dataSnapshot;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            StaffModel eachStaff = snapshot.getValue(StaffModel.class);
            allStaffs.add(eachStaff);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            staffAdapter.notifyDataSetChanged();

            if (allStaffs.size() == 0) {
                showNullText();
            }else {
                progressDialog.dismiss();
                errorTextOnListOfStaffs.setText("");
            }
        }
    }

    @Override
    public void onStaffOptClicked(String staff_id, String staff_name, String staff_photo_url) {
        LISTENED_ID_OF_STAFF = staff_id;
        LISTENED_NAME_OF_STAFF = staff_name;
        showUsersOptionsInBottomSheet(staff_photo_url);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    @SuppressLint("ResourceAsColor")
    private void showUsersOptionsInBottomSheet(String staff_photo_url) {

        Picasso.get()
                .load(staff_photo_url)
                .into(drawer_photo);

        String purpose_text = "";
        dates.setText(R.string.view_dates);
        dates.setVisibility(View.VISIBLE);

        blackOptText.setText(R.string.open_profile);
        blackOptIcon.setImageResource(R.drawable.open);
        blackOpt.setVisibility(View.VISIBLE);

        switch (TYPE_OF_USER){
            case "STAFFS":
                purpose_text = "<b>"+LISTENED_NAME_OF_STAFF+"</b> is former staff of "+SP.getString("ORG_NAME", "")+"</p>";
                if(IS_THIS_ORGANIZATION){
                    below_purpose.setText(Html.fromHtml("You can <span style=\"color: red\">fire</span> the staff in case if he/she has done something unethical according to your organization policy"));
                    redOptText.setText(R.string.fire_staff);
                    redOptIcon.setImageResource(R.drawable.fire);
                    redOpt.setVisibility(View.VISIBLE);
                }
                break;
            case "FIRED_STAFFS":
                purpose_text = "<font size=\"-1\"><b>"+LISTENED_NAME_OF_STAFF+"</b> "+getString(R.string.fired_text)+"from "+SP.getString("ORG_NAME", "")+"</font>";
                break;
            case "REQUESTS":
                purpose_text = "<font size=\"-1\"><b>"+LISTENED_NAME_OF_STAFF+"</b> "+getString(R.string.request_text)+" of "+SP.getString("ORG_NAME", "")+"</font>";
                if(IS_THIS_ORGANIZATION){

                    below_purpose.setText(Html.fromHtml("<span style=\"color:Olive\">Approve</span> the request if he/she is genuine" +
                            "<br><span style=\"color: red\">Delete</span> the request if he/she seems to be disingenuous"));

                    redOptText.setText(R.string.delete_req);
                    redOptIcon.setImageResource(R.drawable.delete);
                    redOpt.setVisibility(View.VISIBLE);

                    greenOptText.setText(R.string.approve_req);
                    greenOptIcon.setImageResource(R.drawable.approve);
                    greenOpt.setVisibility(View.VISIBLE);
                }
                break;
            case "DELETED_REQUESTS":
                purpose_text = "<font size=\"-1\"><b>"+LISTENED_NAME_OF_STAFF+"</b> "+getString(R.string.deleted_text)+" of "+SP.getString("ORG_NAME", "")+"</font>";
                if(IS_THIS_ORGANIZATION){
                    below_purpose.setText(Html.fromHtml("Ask "+LISTENED_NAME_OF_STAFF+" to send the request again if he/she is genuine and you mistakenly deleted his/her request"));
                }
                break;
        }
        purpose.setText(Html.fromHtml(purpose_text));
        return;
    }

    private void showDatesOfStaff(){
        FirebaseDatabase.getInstance().getReference("ALL_STAFFS").child(LISTENED_ID_OF_STAFF+"/TIMES-"+ORGANIZATION_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String date = "";
                        if (snapshot.child("Pending/Time").exists())
                            date = "<b><span style=\"color:MediumSeaGreen\">Request received on:</b> "+snapshot.child("Pending/Time").getValue(String.class)+"</span>";

                        if (snapshot.child("Approved/Time").exists())
                            date = date + "<br><b><span style=\"color:Olive\">Request accepted on:</b> "+snapshot.child("Approved/Time").getValue(String.class)+"</span>";

                        if (snapshot.child("Fired/Time").exists())
                            date = date + "<br><b><span style=\"color:Red\">Fired on:<b> "+snapshot.child("Fired/Time").getValue(String.class)+"</span>";

                        dates.setText(Html.fromHtml(date));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void showAlertDialog(String title, String message, String pt_btn_text, String ng_btn_text, final String fromChild, final String toChild,final String history_type){
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(All_Staff.this, android.R.style.Theme_Material_Light_Dialog_Alert)).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(Html.fromHtml("Do you really want to "+message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, Html.fromHtml("<i>"+pt_btn_text+"</i>"), (dialog, which) -> {
            alertDialog.dismiss();
            if (ng_btn_text.equals(getString(R.string.cancel))) moveStaffsID(fromChild, toChild, history_type);
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, Html.fromHtml("<span style=\"color: red\"><i>"+ng_btn_text+"</i></span>"), (dialog, which) -> {
            alertDialog.dismiss();
            if (pt_btn_text.equals(getString(R.string.cancel))) moveStaffsID(fromChild, toChild, history_type);
        });

        alertDialog.show();
    }

//    private void moveStaffsID(final String fromChild, final String toChild,final String history_type) {
//
//        databaseReference.child(fromChild).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                databaseReference.child(toChild+"/"+LISTENED_ID_OF_STAFF).setValue(snapshot.child(LISTENED_ID_OF_STAFF).getValue())
//                        .addOnCompleteListener(task1 -> {
//                            if (task1.isSuccessful()){
//                                databaseReference.child(fromChild+"/"+LISTENED_ID_OF_STAFF).removeValue()
//                                        .addOnCompleteListener(task2 -> {
//                                            if (task2.isSuccessful()){
//                                                refreshPage();
//                                                updateOnHistory(history_type);
//                                                setNotification(history_type);
//                                                if (toChild.equals("STAFFS")) addMessage();
//                                            }else {
//                                                displayToast(getString(R.string.smth_wrong), All_Staff.this);
//                                            }
//                                        });
//                            }else {
//                                displayToast(getString(R.string.smth_wrong), All_Staff.this);
//                            }
//                        });
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                displayToast(getString(R.string.smth_wrong), All_Staff.this);
//            }
//        });
//        return;
//    }

    private void setNotification(String history_type) {

        HashMap<Object, String> notification = new HashMap<>();
        notification.put("TYPE", history_type);
        notification.put("ORGANIZATION_NAME",SP.getString("ORG_NAME", ""));

        FirebaseDatabase.getInstance().getReference("ALL_STAFFS")
                .child(LISTENED_ID_OF_STAFF)
                .child("NOTIFICATION")
                .child(String.valueOf(UUID.randomUUID()))
                .setValue(notification);
    }

    private void addMessage() {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("SenderID", LISTENED_ID_OF_STAFF);
        hashMap.put("Message", "NEW@#$STAFF@#$ADDED");

        databaseReference.child("CHATS")
                .push().setValue(hashMap);
    }
    public void updateOnHistory(String history_type) {

        String time = DateFormat
                .getDateTimeInstance()
                .format(new Date());


        HashMap<Object, String> history = new HashMap<>();
        history.put("ID",LISTENED_ID_OF_STAFF);
        history.put("Type",history_type);
        history.put("Time", time);

        DatabaseReference tempReference = databaseReference.child("HISTORY");

        tempReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int historyCount = (int) snapshot.getChildrenCount();
                tempReference.child(String.valueOf(historyCount+1)).setValue(history)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                if (history_type.equals("Pending") || history_type.equals("Approved") || history_type.equals("Fired")){

                                    HashMap<Object, String> date = new HashMap<>();
                                    date.put("Time", time);
                                    FirebaseDatabase.getInstance().getReference("ALL_STAFFS").child(LISTENED_ID_OF_STAFF+"/TIMES-"+ORGANIZATION_ID+"/"+ history_type)
                                            .setValue(date)
                                            .addOnSuccessListener(aVoid -> displayToast("done", All_Staff.this));
                                }
                                return;
                            }else {
                                displayToast(getString(R.string.smth_wrong), All_Staff.this);
                            }
                        });
                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayToast(getString(R.string.smth_wrong), All_Staff.this);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.staffPageTitle:
                Toast.makeText(this, getString(R.string.refreshing), Toast.LENGTH_SHORT).show();
                refreshPage();
                break;
            case R.id.showOtherStaffList:
                Intent staffIntent = new Intent(this, All_Staff.class);
                staffIntent.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
                staffIntent.putExtra("IS_THIS_ORGANIZATION",IS_THIS_ORGANIZATION);
                if(showOtherStaffList.getText().equals(getString(R.string.view_fired_staffs))){
                    staffIntent.putExtra("TYPE_OF_USER","FIRED_STAFFS");
                }else if (showOtherStaffList.getText().equals(getString(R.string.view_deleted_requests))){
                    staffIntent.putExtra("TYPE_OF_USER","DELETED_REQUESTS");
                }
                startActivity(staffIntent);
                break;
            case R.id.cancel:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.dates:
                showDatesOfStaff();
                break;
            case R.id.greenOpt:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAlertDialog(getString(R.string.approve_req),
                        "<b>approve</b> request of <b>"+LISTENED_NAME_OF_STAFF+"</b>?",
                        "Approve",
                        getString(R.string.cancel),
                        "REQUESTS",
                        "STAFFS",
                        "Approved");
                break;
            case R.id.redOpt:
                if (redOptText.getText().equals(getString(R.string.delete_req))){

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showAlertDialog(getString(R.string.delete_req),
                            "<b>delete</b> request of <b>"+LISTENED_NAME_OF_STAFF+"</b>?",
                            getString(R.string.cancel),
                            "Delete",
                            "REQUESTS",
                            "DELETED_REQUESTS",
                            "Deleted");

                }else if (redOptText.getText().equals(getString(R.string.fire_staff))){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    showAlertDialog(getString(R.string.fire_staff),
                            "<b>fire <u>"+LISTENED_NAME_OF_STAFF+"</u></b> from <b>"+SP.getString("ORG_NAME", "")+"</b>?",
                            getString(R.string.cancel),
                            "Fire",
                            "STAFFS",
                            "FIRED_STAFFS",
                            "Fired");
                }
                break;
            case R.id.blackOpt:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Intent profileIntent = new Intent(All_Staff.this, My_Profile.class);
                profileIntent.putExtra("STAFF_ID", LISTENED_ID_OF_STAFF);
                startActivity(profileIntent);
                break;
            case R.id.drawer_photo:
                Intent intent = new Intent(this, My_Profile.class);
                intent.putExtra("STAFF_ID", LISTENED_ID_OF_STAFF);
                startActivity(intent);
                break;
        }
    }

    private void refreshPage() {
        allStaffs.clear();
        loadAllList();
    }

    private void showErrorText() {
        progressDialog.dismiss();
        errorTextOnListOfStaffs.setText(R.string.smth_wrong);
    }
    @SuppressLint("SetTextI18n")
    private void showNullText() {
        progressDialog.dismiss();
        switch (TYPE_OF_USER){
            case "STAFFS":
                errorTextOnListOfStaffs.setText("List of all the former staffs will be displayed here");
                break;
            case "FIRED_STAFFS":
                errorTextOnListOfStaffs.setText("List of all the fired staffs will be displayed here");
                break;
            case "DELETED_REQUESTS":
                errorTextOnListOfStaffs.setText("List of all the deleted requests of the users will be displayed here");
                break;
            case "REQUESTS":
                errorTextOnListOfStaffs.setText("List of all the requests come from different users will be displayed here");
                break;
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void moveBack(View view) {
        super.onBackPressed();
    }
    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else {
            super.onBackPressed();
        }
    }

    String  myID = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private void moveStaffsID(final String fromChild, final String toChild,final String history_type) {

        databaseReference.child(fromChild).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(toChild+"/"+LISTENED_ID_OF_STAFF).setValue(snapshot.child(LISTENED_ID_OF_STAFF).getValue())
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                databaseReference.child(fromChild+"/"+LISTENED_ID_OF_STAFF).removeValue()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()){
                                                refreshPage();
                                                updateOnHistory(history_type);
                                                setNotification(history_type);
                                                if (toChild.equals("STAFFS")) addMessage();
                                            }else {
                                                displayToast(getString(R.string.smth_wrong), All_Staff.this);
                                            }
                                        });
                            }else {
                                displayToast(getString(R.string.smth_wrong), All_Staff.this);
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayToast(getString(R.string.smth_wrong), All_Staff.this);
            }
        });
        return;
    }
}