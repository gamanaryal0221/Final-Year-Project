package com.gaman_aryal.easyform.form_field_record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.gaman_aryal.easyform.home.Home.displayToast;

public class Form_Field_RecordAdapter extends RecyclerView.Adapter<Form_Field_RecordAdapter.Form_Field_RecordViewHolder> {

    public static final int FORM_VIEW = 0;
    public static final int FIELD_VIEW = 1;
    public static final int RECORD_VIEW = 2;

    private final List<Form_Field_Record_Model> allForms;
    private final Context context;
    private int Count;
    private final boolean isThisForm, isThisOrg, isThisRecord;
    private final DatabaseReference databaseReference;
    private final OnFormOrFieldClickedListener onFormOrFieldClickedListener;
    private final String organization_id;

    public Form_Field_RecordAdapter(List<Form_Field_Record_Model> allForms, String organization_id, boolean isThisOrg,
                                    boolean isThisForm, boolean isThisRecord, OnFormOrFieldClickedListener onFormOrFieldClickedListener, Context context) {
        this.allForms = allForms;
        this.isThisOrg = isThisOrg;
        this.context = context;
        this.isThisForm = isThisForm;
        this.isThisRecord = isThisRecord;
        this.onFormOrFieldClickedListener = onFormOrFieldClickedListener;
        this.organization_id = organization_id;
        databaseReference = FirebaseDatabase.getInstance().getReference("ALL_STAFFS");
    }

    @NonNull
    @Override
    public Form_Field_RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == FORM_VIEW){
            view = LayoutInflater.from(context).inflate(R.layout.single_form, parent, false);
        }else if (viewType == FIELD_VIEW){
            view = LayoutInflater.from(context).inflate(R.layout.single_field, parent, false);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.single_record, parent, false);
        }
        return new Form_Field_RecordViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Form_Field_RecordViewHolder holder, int position) {
        holder.setDataOnForm(allForms.get(position), onFormOrFieldClickedListener);
    }

    @Override
    public int getItemCount() {
        Count = allForms.size();
        return Count;
    }

    public class Form_Field_RecordViewHolder extends RecyclerView.ViewHolder {

        TextView fTitle, fDate, fDesc, fCreatedBy, count, byText, timeText;
        CircleImageView fCreatedByPhoto;
        ImageView fOpt;
        RelativeLayout bottomSheetOpener;
        OnFormOrFieldClickedListener onFormOrFieldClickedListener;

        @SuppressLint("SetTextI18n")
        public Form_Field_RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            count = ((Activity)context).findViewById(R.id.countOfForm);
            count.setText("("+Count+")");
            fOpt = itemView.findViewById(R.id.fOpt);

            bottomSheetOpener = ((Activity)context).findViewById(R.id.bottomSheetOpener);

            fTitle = itemView.findViewById(R.id.fTitle);
            fCreatedBy = itemView.findViewById(R.id.fCreatedBy);
            fCreatedByPhoto = itemView.findViewById(R.id.fCreatedByPhoto);
            fDate = itemView.findViewById(R.id.fDate);
            fDesc = itemView.findViewById(R.id.fDesc);
            byText = itemView.findViewById(R.id.byText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        @SuppressLint("SetTextI18n")
        public void setDataOnForm(final Form_Field_Record_Model eachForm_Field_Record,
                                  OnFormOrFieldClickedListener onFormOrFieldClickedListener) {

            this.onFormOrFieldClickedListener = onFormOrFieldClickedListener;

            if (isThisRecord){
                fTitle.setText(eachForm_Field_Record.getID());
                fDesc.setText(Html.fromHtml("<b>Data : </b><br>"+eachForm_Field_Record.getDescription()));
                byText.setText(R.string.recorded_by);
                timeText.setText(R.string.recorded_on);
                fOpt.setVisibility(View.GONE);
            }else {
                fTitle.setText(eachForm_Field_Record.getTitle());
                fDesc.setText(Html.fromHtml("<b>Description : </b>"+eachForm_Field_Record.getDescription()));
                byText.setText(R.string.created_by);
                timeText.setText(R.string.created_on);

                if(isThisOrg){
                    fOpt.setVisibility(View.VISIBLE);
                }else {
                    if (isThisForm){
                        fOpt.setVisibility(View.VISIBLE);
                    }else {
                        fOpt.setVisibility(View.GONE);
                    }
                }
            }
            fDate.setText(eachForm_Field_Record.getDate());

            databaseReference.child(eachForm_Field_Record.getCreatedBy())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        if (eachForm_Field_Record.getCreatedBy()
                                .equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                            fCreatedBy.setText(snapshot.child("FullName").getValue(String.class)+" (Me)");
                        }else {
                            fCreatedBy.setText(snapshot.child("FullName").getValue(String.class));
                        }

                        Picasso.get()
                                .load(snapshot.child("Profile_Photo").getValue(String.class))
                                .into(fCreatedByPhoto);
                    }else {
                        fCreatedBy.setText("Admin");
                        fCreatedByPhoto.setImageResource(R.drawable.admin);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            itemView.setOnClickListener(v -> {
                if (isThisForm){
                    Intent formIntent = new Intent(context, All_Forms_Fields_Records.class);
                    formIntent.putExtra("ORGANIZATION_ID",organization_id);
                    formIntent.putExtra("IS_THIS_ORGANIZATION", isThisOrg);
                    formIntent.putExtra("IS_THIS_FORM", false);
                    formIntent.putExtra("IS_THIS_RECORD", false);
                    formIntent.putExtra("FORM_ID", eachForm_Field_Record.getID());
                    formIntent.putExtra("FORM_NAME", eachForm_Field_Record.getTitle());
                    context.startActivity(formIntent);
                }
            });
            fOpt.setOnClickListener(v -> {
                if (isThisForm){
                    onFormOrFieldClickedListener.onFormOptionClicked(eachForm_Field_Record.getID(),
                            eachForm_Field_Record.getTitle());
                }else {
                    onFormOrFieldClickedListener.onDeleteFieldClicked(eachForm_Field_Record.getID(),
                            eachForm_Field_Record.getTitle());
                }
            });

            fCreatedBy.setOnClickListener(v -> moveToProfile(eachForm_Field_Record.getCreatedBy(), fCreatedBy.getText().toString()));
            fCreatedByPhoto.setOnClickListener(v -> moveToProfile(eachForm_Field_Record.getCreatedBy(), fCreatedBy.getText().toString()));

            fTitle.setOnClickListener(v -> {
                if (isThisRecord){
                    copyRecordID("Record ID -> '"+eachForm_Field_Record.getID()+"'");
                }
            });
        }

        private void copyRecordID(String id) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("RECORD ID", id);
            clipboard.setPrimaryClip(clip);
            displayToast(id+" copied",context);
        }
    }

    private void moveToProfile(String staff_id, String creator_name) {
        if (!creator_name.equals("Admin")){
            Intent profileIntent = new Intent(context, My_Profile.class);
            profileIntent.putExtra("STAFF_ID", staff_id);
            context.startActivity(profileIntent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isThisForm){
            return FORM_VIEW;
        }else {
            if (isThisRecord){
                return RECORD_VIEW;
            }else {
                return FIELD_VIEW;
            }
        }
    }

    public interface OnFormOrFieldClickedListener {
        void onFormOptionClicked(String form_id, String form_title);
        void onDeleteFieldClicked(String field_id, String field_title);
    }
}
