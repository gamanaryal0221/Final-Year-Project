package com.gaman_aryal.easyform.staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaman_aryal.easyform.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.gaman_aryal.easyform.home.Home.displayToast;

public class My_Profile extends AppCompatActivity {

    SharedPreferences SP;

    String STAFF_ID, MY_PHOTO_LINK = null;
    DatabaseReference databaseReference;
    DataSnapshot snapshot;

    CircleImageView myImage;
    ImageView bigPhoto;
    TextView myName, myGender, myOfficeID, myPost, myAdd, myOrgName, myOrgAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP = getSharedPreferences("EASYFORM", Context.MODE_PRIVATE);

        getFromIntent();
        init();
        extractData();
    }
    private void getFromIntent() {

        if (getIntent().getExtras() != null) {
            STAFF_ID = getIntent().getExtras().getString("STAFF_ID");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("ALL_STAFFS").child(STAFF_ID);
    }

    private void init() {
        myImage = findViewById(R.id.myImage);
        Picasso.get()
                .load(R.drawable.default_photo)
                .into(myImage);
        myImage.setOnClickListener(v -> enlargeMyImage());

        myName = findViewById(R.id.myName);
        myGender = findViewById(R.id.myGender);
        myOfficeID = findViewById(R.id.myOfficeID);
        myPost = findViewById(R.id.myPost);
        myAdd = findViewById(R.id.myAdd);

        myOrgName = findViewById(R.id.myOrgName);
        myOrgAdd = findViewById(R.id.myOrgAdd);

        bigPhoto = findViewById(R.id.bigPhoto);
        bigPhoto.setOnClickListener(this::closeStaffProfile);
    }

    private void enlargeMyImage() {
        if (MY_PHOTO_LINK != null){
            bigPhoto.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(MY_PHOTO_LINK)
                    .into(bigPhoto);
        }else {
            displayToast(getString(R.string.smth_wrong),this);
        }
    }

    private void extractData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot;
                new ExtractStaffDetail().execute(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayToast(error.getMessage(), My_Profile.this);
            }
        });
    }
    public class ExtractStaffDetail extends AsyncTask<DataSnapshot,Void,String[]> {

        @Override
        protected String[] doInBackground(DataSnapshot... snapshot) {

            String[] data = {"","","","","",""};

            data[0] = MY_PHOTO_LINK = snapshot[0].child("Profile_Photo").getValue(String.class);
            data[1] = snapshot[0].child("FullName").getValue(String.class);
            data[2] = snapshot[0].child("Gender").getValue(String.class);
            data[3] = snapshot[0].child("OfficeID").getValue(String.class);
            data[4] = snapshot[0].child("Post").getValue(String.class);
            data[5] = snapshot[0].child("Address").getValue(String.class);

            return data;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            putDataOnMyProfile(strings);
        }
    }
    @SuppressLint("SetTextI18n")
    private void putDataOnMyProfile(String[] data) {

        Picasso.get()
                .load(data[0])
                .into(myImage);

        if (STAFF_ID.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            myName.setText(data[1]+" (Me)");
        }else {
            myName.setText(data[1]);
        }

        myGender.setText(data[2]);
        myOfficeID.setText(data[3]);
        myPost.setText(data[4]);
        myAdd.setText(data[5]);

        myOrgName.setText(SP.getString("ORG_NAME", ""));
        myOrgAdd.setText(SP.getString("ORG_ADD", ""));
    }

    public void closeStaffProfile(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (bigPhoto.getVisibility()==View.VISIBLE){
            bigPhoto.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }
}