package com.gaman_aryal.easyform.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.home.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Chat extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser currentUser;
    String currentUserID, organizationID, SenderName, SenderPhotoLink;
    boolean isAdmin;
    DatabaseReference realtimeReference;

    EditText insertedMessage;
    String Message;

    RecyclerView chatSectionRecyclerView;
    List<ChatModel> allChats;
    ChatAdapter chatAdapter;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    View focusedView;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_chats);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getSharedPreferences("EASYFORM",MODE_PRIVATE);

        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();

        if (getIntent().getExtras() != null) {
            organizationID = getIntent().getExtras().getString("ORG_ID");
            realtimeReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(organizationID);
            isAdmin = getIntent().getExtras().getBoolean("IS_ADMIN");
            if (isAdmin) {
                SenderName = "Admin";
            }else {
                extractStaffDetail();
            }
        }

        allChats = new ArrayList<>();

        init();
    }

    private void extractStaffDetail() {
        FirebaseDatabase.getInstance().getReference("ALL_STAFFS")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SenderName = snapshot.child("FullName").getValue(String.class);
                        SenderPhotoLink = snapshot.child("Profile_Photo").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {

        chatSectionRecyclerView = findViewById(R.id.chatSectionRecyclerView);
        insertedMessage = findViewById(R.id.insertedMessage);

        chatSectionRecyclerView.setHasFixedSize(true);
        LinearLayoutManager cmntSectionLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        chatSectionRecyclerView.setLayoutManager(cmntSectionLayoutManager);
        chatAdapter = new ChatAdapter(this, allChats, organizationID);
        loadingAllChat();
        return;
    }
    private void loadingAllChat() {
        realtimeReference.child("CHATS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatModel chat = snapshot.getValue(ChatModel.class);

                    if (chat != null) allChats.add(chat);
                }
                chatSectionRecyclerView.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
                chatSectionRecyclerView.scrollToPosition(Objects.requireNonNull(chatSectionRecyclerView.getAdapter()).getItemCount()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return;
    }
    public void refreshApprovedStaffs(View view) {
        loadingAllChat();
    }

    public void sendMessage(View view) {
        Message = insertedMessage.getText().toString().trim();
        if (Message.isEmpty()) {
            Home.displayToast("write something first",this);
        } else {
            if (Message.length()>4000){
                Home.displayToast("Message limit is 4000",this);
            }else {
                addMessage();
                updateLastMessage();
            }
        }
        return;
    }
    private void addMessage() {

        insertedMessage.setText("");
        closeKeyboard();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("SenderID", currentUserID);
        hashMap.put("Message", Message);
        hashMap.put("Time", DateFormat
                .getTimeInstance()
                .format(new Date()));

        realtimeReference.child("CHATS")
                .push().setValue(hashMap);
        return;
    }
    private void updateLastMessage() {
        HashMap<String, Object> lastMsg = new HashMap<>();
        lastMsg.put("Sender",SenderName);
        lastMsg.put("Message",Message);
        lastMsg.put("Photo",SenderPhotoLink);

        realtimeReference.child("LAST_MESSAGE")
                .setValue(lastMsg);
    }

    public void closeRoom(View view) {
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public void closeKeyboard() {
        focusedView = this.getCurrentFocus();
        if (focusedView != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}