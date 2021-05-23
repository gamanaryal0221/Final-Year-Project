package com.gaman_aryal.easyform.home;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.chat.Chat;
import com.gaman_aryal.easyform.form_field_record.All_Forms_Fields_Records;
import com.gaman_aryal.easyform.history.History;
import com.gaman_aryal.easyform.registration_login.Registration_Login;
import com.gaman_aryal.easyform.staff.All_Staff;
import com.gaman_aryal.easyform.staff.My_Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.gaman_aryal.easyform.notification.NotificationHelper.CHANNEL_1_ID;
import static com.gaman_aryal.easyform.notification.NotificationHelper.CHANNEL_2_ID;

public class Home extends AppCompatActivity implements View.OnClickListener{

    SharedPreferences SP;
    SharedPreferences.Editor editor;
    NotificationManagerCompat notificationManager;

    boolean isThisOrganization, isCurrentStaffValid;
    String ORGANIZATION_ID;

    ScrollView homePageScrollView;

    DisplayMetrics displayMetrics;
    int widthOfTheScreen, SquareBoxLength;

    LinearLayout homeBack;
    RelativeLayout staffBox, requestOrMyProfileBox, historyBox, formFieldBox, chatRoomBox;
    TextView noOfStaffs, requestOrMyProfileText, noOfRequests, noOfHistories, noOfForms;
    ImageView requestOrMyProfileIcon;

    CircleImageView photoOfLastMessageSender;
    TextView lastMessageSender, lastMessage;

    DatabaseReference databaseReference;

    TextView myOrganizationName, myOrganizationAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP = getSharedPreferences("EASYFORM", Context.MODE_PRIVATE);
        calculateScreenSize();
        init();

        notificationManager = NotificationManagerCompat.from(this);
        getFromSharedPreference();
    }

    private void calculateScreenSize() {
        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        widthOfTheScreen = displayMetrics.widthPixels;
        SquareBoxLength = (int) (widthOfTheScreen / 2.2);
        return;
    }
    private void init() {
        homeBack = findViewById(R.id.homeBack);
        homeBack.getLayoutParams().height = (int) (widthOfTheScreen / 1.5);

        homePageScrollView = findViewById(R.id.homePageScrollView);

        myOrganizationName = findViewById(R.id.myOrganizationName);
        myOrganizationAdd = findViewById(R.id.myOrganizationAdd);

        staffBox = findViewById(R.id.approvedStaffBox);
        staffBox.getLayoutParams().height = SquareBoxLength;
        staffBox.getLayoutParams().width = SquareBoxLength;
        noOfStaffs = findViewById(R.id.noOfStaffs);

        requestOrMyProfileBox = findViewById(R.id.requestOrMyProfileBox);
        requestOrMyProfileBox.getLayoutParams().height = SquareBoxLength;
        requestOrMyProfileBox.getLayoutParams().width = SquareBoxLength;
        requestOrMyProfileText = findViewById(R.id.requestOrMyProfileText);
        requestOrMyProfileIcon = findViewById(R.id.requestOrMyProfileIcon);
        noOfRequests = findViewById(R.id.noOfRequests);

        historyBox = findViewById(R.id.historyBox);
        historyBox.getLayoutParams().height = SquareBoxLength;
        historyBox.getLayoutParams().width = SquareBoxLength;
        noOfHistories = findViewById(R.id.noOfHistories);

        formFieldBox = findViewById(R.id.formFieldBox);
        formFieldBox.getLayoutParams().height = SquareBoxLength;
        formFieldBox.getLayoutParams().width = SquareBoxLength;
        noOfForms = findViewById(R.id.noOfForms);

        chatRoomBox = findViewById(R.id.chatRoomBox);
        photoOfLastMessageSender = findViewById(R.id.photoOfLastMessageSender);
        lastMessageSender = findViewById(R.id.lastMessageSender);
        lastMessage = findViewById(R.id.lastMessage);

        staffBox.setOnClickListener(this);
        requestOrMyProfileBox.setOnClickListener(this);
        historyBox.setOnClickListener(this);
        formFieldBox.setOnClickListener(this);
        chatRoomBox.setOnClickListener(this);

        staffBox.setBackgroundResource(R.drawable.homepage_black_box);
        requestOrMyProfileBox.setBackgroundResource(R.drawable.homepage_black_box);
        historyBox.setBackgroundResource(R.drawable.homepage_black_box);
        formFieldBox.setBackgroundResource(R.drawable.homepage_black_box);
        chatRoomBox.setBackgroundResource(R.drawable.homepage_black_box);

        putFrontData();
    }
    private void getFromSharedPreference() {
        ORGANIZATION_ID = SP.getString("MY_ORG_ID","");
        databaseReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(ORGANIZATION_ID);

        if (SP.getString("ACCOUNT_TYPE","").equals("STAFF")){
            isThisOrganization = false;
            requestOrMyProfileBox.setBackgroundResource(R.drawable.homepage_white_box);
            listenForNotification(false);

        }else if (SP.getString("ACCOUNT_TYPE","").equals("ORGANIZATION")){
            isThisOrganization = true;
            makeBoxesWhite();
            listenForNotification(true);
        }

        putFrontData();
        designHome();
        linkWithDatabase();
    }
    private void listenForNotification(boolean isThisOrg) {
        DatabaseReference notificationReference;
        if (isThisOrg){
            notificationReference = databaseReference.child("NOTIFICATION");
        }else {
            notificationReference = FirebaseDatabase.getInstance()
                    .getReference("ALL_STAFFS")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("NOTIFICATION");
        }

        notificationReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int number_of_notifications = (int) snapshot.getChildrenCount(),
                                count = 1;

                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                         if (isThisOrg){
                            new PushNotificationToAdmin(snapshot1.child("SENT_BY_NAME").getValue(String.class),
                                    snapshot1.child("SENT_BY_PHOTO").getValue(String.class))
                                    .execute();
                         }else {
                             new PushNotificationToStaff(snapshot1.child("TYPE").getValue(String.class),
                                     snapshot1.child("ORGANIZATION_NAME").getValue(String.class))
                                     .execute();
                         }

                         if (count == number_of_notifications){
                             new Handler().postDelayed(() -> cleanNotification(notificationReference),
                                     20000);
                         }

                         count++;

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private class PushNotificationToAdmin extends AsyncTask<Void, Void, Bitmap> {

        String staff_name, staff_photo;
        public PushNotificationToAdmin(String staff_name, String staff_photo){
            this.staff_name = staff_name;
            this.staff_photo = staff_photo;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                return Picasso.get()
                        .load(staff_photo)
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            Intent adminView = new Intent(Home.this, All_Staff.class);
            adminView.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
            adminView.putExtra("IS_THIS_ORGANIZATION",isThisOrganization);
            adminView.putExtra("TYPE_OF_USER","REQUESTS");
            PendingIntent adminIntentToOrder = PendingIntent.getActivity(Home.this,
                    0, adminView, 0);

            Notification notification = new NotificationCompat.Builder(Home.this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(Html.fromHtml("<b>!!! New Request !!!</b>"))
                    .setContentText("enlarge to see detail")
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(Html.fromHtml("<b>"+staff_name+"</b>" +
                                    " sent a request to be staff of " +
                                    "<b>"+ SP.getString("ORG_NAME", "") +"</b>"))
                            .setBigContentTitle(Html.fromHtml("<b>!!! New Request !!!</b>"))
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(adminIntentToOrder)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(new Random().nextInt((1000 - 1) + 1) + 1, notification);
        }
    }

    private class PushNotificationToStaff extends AsyncTask<Void, Void, String[]> {

        String type, org_name;
        public PushNotificationToStaff(String type, String org_name){
            this.type = type;
            this.org_name = org_name;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            String[] data = new String[2];

            switch (type){
                case "Pending":
                    data[0] = " REQUEST SENT ";
                    data[1] = "Your request is sent to ";
                    return data;
                case "Deleted":
                    data[0] = "<span style=\"color: red\"> REQUEST DELETED </span>";
                    data[1] = "Your request is deleted by ";
                    return data;
                case "Approved":
                    data[0] = "<span style=\"color: blue\"> REQUEST APPROVED </span>";
                    data[1] = "Your request is approved by ";
                    return data;
                case "Fired":
                    data[0] = "<span style=\"color: red\"> FIRED </span>";
                    data[1] = "You are fired from ";
                    return data;
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            Notification notification = new NotificationCompat.Builder(Home.this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(Html.fromHtml("<b>!!! "+strings[0]+" !!!</b>"))
                    .setContentText("enlarge to see detail")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(Html.fromHtml(strings[1]+
                                    "<b>"+ org_name +"</b>"))
                            .setBigContentTitle(Html.fromHtml("<b>!!! "+strings[0]+" !!!</b>"))
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(new Random().nextInt((1000 - 1) + 1) + 1, notification);

        }
    }

    private void cleanNotification(DatabaseReference notification) {
        notification.removeValue();
    }

    private void linkWithDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isThisOrganization){
                    isCurrentStaffValid = snapshot.child("STAFFS/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists();
                }
                new ExtractFrontData().execute(snapshot);
                designHome();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void designHome() {
        if (isThisOrganization){
            requestOrMyProfileIcon.setImageResource(R.drawable.request);
            requestOrMyProfileText.setText(R.string.requests);
        }else {
            if (isCurrentStaffValid) makeBoxesWhite();
            requestOrMyProfileIcon.setImageResource(R.drawable.my_profile);
            requestOrMyProfileText.setText(R.string.my_profile);
        }
    }
    private void makeBoxesWhite() {
        requestOrMyProfileBox.setBackgroundResource(R.drawable.homepage_white_box);
        staffBox.setBackgroundResource(R.drawable.homepage_white_box);
        historyBox.setBackgroundResource(R.drawable.homepage_white_box);
        formFieldBox.setBackgroundResource(R.drawable.homepage_white_box);
        chatRoomBox.setBackgroundResource(R.drawable.homepage_white_box);
    }


    @SuppressLint("StaticFieldLeak")
    private class ExtractFrontData extends AsyncTask<DataSnapshot,Integer,Boolean>{

        @Override
        protected Boolean doInBackground(DataSnapshot... snapshot) {
            editor = SP.edit();

            editor.putString("ORG_NAME", snapshot[0].child("OrganizationName").getValue(String.class));
            editor.putString("ORG_ADD", snapshot[0].child("Address").getValue(String.class));
            editor.putString("STAFF_COUNT", String.valueOf(snapshot[0].child("STAFFS").getChildrenCount()));
            editor.putString("FIRED_COUNT", String.valueOf(snapshot[0].child("FIRED_STAFFS").getChildrenCount()));
            editor.putString("REQUEST_COUNT", String.valueOf(snapshot[0].child("REQUESTS").getChildrenCount()));
            editor.putString("DELETED_COUNT", String.valueOf(snapshot[0].child("DELETED_REQUESTS").getChildrenCount()));
            editor.putString("HISTORY_COUNT", String.valueOf(snapshot[0].child("HISTORY").getChildrenCount()));
            editor.putString("FORMS_COUNT", String.valueOf(snapshot[0].child("FORMS").getChildrenCount()));
            editor.putString("STORAGE_COUNT", String.valueOf(snapshot[0].child("STORAGE").getChildrenCount()));

            if (snapshot[0].child("LAST_MESSAGE").exists()){

                editor.putString("LAST_MSG/Sender", snapshot[0].child("LAST_MESSAGE/Sender").getValue(String.class));
                editor.putString("LAST_MSG/Message", snapshot[0].child("LAST_MESSAGE/Message").getValue(String.class));

                if (Objects.equals(snapshot[0].child("LAST_MESSAGE/Sender").getValue(String.class), "Admin")){
                    editor.putString("LAST_MSG/Photo", "admin");
                }else {
                    editor.putString("LAST_MSG/Photo", snapshot[0].child("LAST_MESSAGE/Photo").getValue(String.class));
                }
            }else {
                editor.putString("LAST_MSG/Sender", "Chat Room is empty");
            }
            return editor.commit();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) putFrontData();
        }
    }

    @SuppressLint("SetTextI18n")
    private void putFrontData() {

        if (isNetworkConnected()){
            myOrganizationName.setText(SP.getString("ORG_NAME", ""));
            myOrganizationAdd.setText(SP.getString("ORG_ADD", ""));

            myOrganizationName.setTextColor(Color.WHITE);
        }else {
            myOrganizationName.setText("No internet Connection !!");
            myOrganizationName.setTextColor(Color.RED);

            myOrganizationAdd.setText(Html.fromHtml(SP.getString("ORG_NAME", "")
                    +"<br><br>"+
                    SP.getString("ORG_ADD", "")));
        }

        if (isThisOrganization || isCurrentStaffValid){

            noOfStaffs.setText(Html.fromHtml(""+ SP.getString("STAFF_COUNT", "")+".<span style=\"color: red\">"+ SP.getString("FIRED_COUNT", "")+"</span>"));
            if (isThisOrganization) noOfRequests.setText(Html.fromHtml(""+ SP.getString("REQUEST_COUNT", "")+".<span style=\"color: red\">"+ SP.getString("DELETED_COUNT", "")+"</span>"));

            noOfHistories.setText(SP.getString("HISTORY_COUNT", ""));
            noOfForms.setText(SP.getString("FORMS_COUNT", ""));

            lastMessageSender.setText(makeTextShort(SP.getString("LAST_MSG/Sender", ""),25));
            lastMessage.setText(makeTextShort(SP.getString("LAST_MSG/Message", ""),130));
            if (Objects.equals(SP.getString("LAST_MSG/Sender", ""), "Admin")){
                photoOfLastMessageSender.setImageResource(R.drawable.admin);
            }else {
                Picasso.get()
                        .load(Uri.parse(SP.getString("LAST_MSG/Photo", "")))
                        .into(photoOfLastMessageSender);
            }
        }

        return;
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.approvedStaffBox:
                if (isThisOrganization){
                    openStaffListActivity("STAFFS");
                }else {
                    if (isCurrentStaffValid) openStaffListActivity("STAFFS");
                }
                break;
            case R.id.requestOrMyProfileBox:
                if (isThisOrganization){
                    openStaffListActivity("REQUESTS");
                }else {
                    openMyProfile();
                }
                break;
            case R.id.historyBox:
                if (isThisOrganization){
                    openHistoryActivity();
                }else {
                    if (isCurrentStaffValid) openHistoryActivity();
                }
                break;
            case R.id.formFieldBox:
                if (isThisOrganization){
                    openFormActivity();
                }else {
                    if (isCurrentStaffValid) openFormActivity();
                }
                break;
            case R.id.chatRoomBox:
                if (isThisOrganization){
                    openChatActivity();
                }else {
                    if (isCurrentStaffValid) openChatActivity();
                }
                break;

        }
    }

    private void openMyProfile() {
        Intent profileIntent = new Intent(Home.this, My_Profile.class);
        profileIntent.putExtra("STAFF_ID",FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(profileIntent);
    }
    private void openStaffListActivity(String type_of_user) {
        Intent staffIntent = new Intent(Home.this, All_Staff.class);
        staffIntent.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
        staffIntent.putExtra("IS_THIS_ORGANIZATION",isThisOrganization);
        staffIntent.putExtra("TYPE_OF_USER",type_of_user);
        startActivity(staffIntent);
    }
    private void openHistoryActivity() {
        Intent history = new Intent(this, History.class);
        history.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
        history.putExtra("IS_THIS_ORGANIZATION",isThisOrganization);
        startActivity(history);
        return;
    }
    private void openFormActivity() {
        Intent formIntent = new Intent(Home.this, All_Forms_Fields_Records.class);
        formIntent.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
        formIntent.putExtra("IS_THIS_ORGANIZATION",isThisOrganization);
        formIntent.putExtra("IS_THIS_FORM",true);
        formIntent.putExtra("IS_THIS_RECORD",false);
        startActivity(formIntent);
    }
    private void openChatActivity() {
        Intent chatRoom = new Intent(this, Chat.class);
        chatRoom.putExtra("ORG_ID",SP.getString("MY_ORG_ID",""));
        chatRoom.putExtra("IS_ADMIN", isThisOrganization);
        startActivity(chatRoom);
        return;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public static void displayToast(String message, Context context) {
        Toast.makeText(context,
                message,
                Toast.LENGTH_SHORT)
                .show();
    }
    public String makeTextShort(String text, int limit) {
        if (text.length() < limit) {
            return text;
        } else {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                str.append(text.charAt(i));
            }
            return str.append("...").toString();
        }
    }

    public void logOut(View view) {
        signingOut();
    }
    private void signingOut() {
        editor = SP.edit();
        editor.clear();
        editor.apply();
        if (editor.commit()){
            FirebaseAuth.getInstance().signOut();
            Intent logout = new Intent(Home.this, Registration_Login.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logout);
        }
    }
}