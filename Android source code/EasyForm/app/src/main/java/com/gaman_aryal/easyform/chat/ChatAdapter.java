package com.gaman_aryal.easyform.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.staff.My_Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_MIDDLE = 2;

    private final Context context;
    private final List<ChatModel> chats;
    private final String OrganizationID;
    private final DatabaseReference databaseReference;
    private final String myID;

    private DisplayMetrics displayMetrics;
    int widthOfTheScreen;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public ChatAdapter(Context context, List<ChatModel> chats, String OrganizationID) {
        this.context = context;
        this.chats = chats;
        this.OrganizationID = OrganizationID;

        myID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        displayMetrics = this.context.getApplicationContext().getResources().getDisplayMetrics();
        widthOfTheScreen = displayMetrics.widthPixels;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = this.context.getSharedPreferences("EASYFORM", MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.new_staff_added, parent, false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setDataOnChats(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    private void setMessage(String id, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("SenderID", id);
        hashMap.put("Message", message);
        hashMap.put("Time", DateFormat
                .getTimeInstance()
                .format(new Date()));

        databaseReference.child("ALL_ORGANIZATIONS/" + OrganizationID + "/CHATS").push().setValue(hashMap);
        setLastMessage(id,message);
    }
    private void setLastMessage(String realtimeID, String Message) {
        databaseReference.child("ALL_STAFFS/" + realtimeID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String SEEN_CODE = String.valueOf(UUID.randomUUID());
                        HashMap<String, Object> lastMsg = new HashMap<>();
                        if (snapshot.child("FullName").exists()){
                            lastMsg.put("Sender",snapshot.child("FullName").getValue(String.class));
                        }else {
                            lastMsg.put("Sender","Admin");
                        }
                        lastMsg.put("Message",Message);
                        lastMsg.put("Seen-Code",SEEN_CODE);
                        lastMsg.put("Photo",snapshot.child("Profile_Photo").getValue(String.class));

                        editor = sharedPreferences.edit();
                        editor.putString("SEEN_CODE",SEEN_CODE);
                        editor.apply();

                        FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(OrganizationID+"/LAST_MESSAGE")
                                .setValue(lastMsg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView chatWriterName, showChat, chatTime;
        public CircleImageView chatWritererPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            showChat = itemView.findViewById(R.id.showChat);
            chatWriterName = itemView.findViewById(R.id.chatWriterName);
            chatWritererPhoto = itemView.findViewById(R.id.chatWritererPhoto);
            chatTime = itemView.findViewById(R.id.chatTime);

            showChat.setMaxWidth((int) (0.7*widthOfTheScreen));
        }

        @SuppressLint("SetTextI18n")
        public void setDataOnChats(ChatModel eachChat) {

            if (!eachChat.getSenderID().equals(OrganizationID)){
                databaseReference.child("ALL_STAFFS/"+eachChat.getSenderID()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        chatWritererPhoto.setOnClickListener(v -> {
                            Intent profileIntent = new Intent(context, My_Profile.class);
                            profileIntent.putExtra("STAFF_ID", eachChat.getSenderID());
                            context.startActivity(profileIntent);
                        });

                        if (eachChat.getMessage().equals("NEW@#$STAFF@#$ADDED")){
                            if (eachChat.getSenderID().equals(myID)){

                                chatWriterName.setText(Html.fromHtml("You are newly added in this organization"));
                                showChat.setText(Html.fromHtml("say <b><span style=\"color: blue\">hello!!</span></b> to everyone"));

                            }else {
                                chatWriterName.setText(Html.fromHtml("<b>"+snapshot.child("FullName").getValue(String.class)+"</b> is newly added in this organization"));
                                if (Objects.equals(snapshot.child("Gender").getValue(String.class), "Male")){
                                    showChat.setText(Html.fromHtml("say <b><span style=\"color: blue\">hello!!</span></b> to him"));
                                }else if (Objects.equals(snapshot.child("Gender").getValue(String.class), "Female")){
                                    showChat.setText(Html.fromHtml("say <b><span style=\"color: blue\">hello!!</span> to her"));
                                }else {
                                    showChat.setText(Html.fromHtml("say <b><span style=\"color: blue\">hello!!</span> to "+snapshot.child("FullName").getValue(String.class)));
                                }
                            }

                            showChat.setOnClickListener(v -> {
                                if (eachChat.getSenderID().equals(myID)){
                                    setMessage(myID,"Hello everyone");
                                }else {
                                    setMessage(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),"Hello, "+snapshot.child("FullName").getValue(String.class));
                                }
                            });

                        }else {
                            showChat.setText(eachChat.getMessage());

                            if (eachChat.getSenderID().equals(myID)){
                                chatWriterName.setText("Me");
                            }else {
                                chatWriterName.setText(snapshot.child("FullName").getValue(String.class));
                            }
                            chatTime.setText(eachChat.getTime());

                            showChat.setOnClickListener(v -> {
                                makeTimeVisible();
                            });
                        }

                        Picasso.get()
                                .load(snapshot.child("Profile_Photo").getValue(String.class))
                                .into(chatWritererPhoto);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else {
                chatWriterName.setText("Admin");
                showChat.setText(eachChat.getMessage());
                Picasso.get()
                        .load(R.drawable.admin)
                        .into(chatWritererPhoto);
                chatTime.setText(eachChat.getTime());

                showChat.setOnClickListener(v -> {
                    makeTimeVisible();
                });
            }


        }

        private void makeTimeVisible() {
            if (chatTime.getVisibility()==View.VISIBLE){
                makeTimeInvisible();
            }else {
                chatTime.setVisibility(View.VISIBLE);
                new Handler().postDelayed(this::makeTimeInvisible,3500);
                return;
            }
        }

        private void makeTimeInvisible() {
            chatTime.setVisibility(View.GONE);
            return;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chats.get(position).getMessage().equals("NEW@#$STAFF@#$ADDED")){
            return MSG_TYPE_MIDDLE;
        }else {
            if (chats.get(position).getSenderID().equals(myID)) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
    }
}
