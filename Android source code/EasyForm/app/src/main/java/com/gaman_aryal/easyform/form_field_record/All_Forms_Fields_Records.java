package com.gaman_aryal.easyform.form_field_record;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.ocr.OCR;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.UUID;

import static com.gaman_aryal.easyform.home.Home.displayToast;

public class All_Forms_Fields_Records extends AppCompatActivity implements View.OnClickListener,
        Form_Field_RecordAdapter.OnFormOrFieldClickedListener {

    String ORGANIZATION_ID, FORM_ID, FORM_NAME;
    boolean IS_THIS_ORGANIZATION, IS_THIS_FORM, IS_THIS_RECORD;

    ProgressDialog progressDialog;

    TextView formPageTitle, countOfForm, errorTextOnListOfForms, fieldFilter, recordFilter, whatToAdd;
    RelativeLayout filter;
    RecyclerView recyclerViewOfForms;

    String REFERENCE_CHILD;
    DatabaseReference databaseReference;

    Form_Field_RecordAdapter form_fieldRecordAdapter;
    List<Form_Field_Record_Model> allForm_Field_Records;

    RelativeLayout bottomSheetOpener;
    View bottomSheetView;
    BottomSheetBehavior bottomSheetBehavior;
    View focusedView;
    InputMethodManager imm;

    LinearLayout greenOpt, redOpt, blackOpt, editTexts;
    Button add_Form_Field;
    EditText form_field_Title, form_field_Description;
    ImageView cancel, redOptIcon, blackOptIcon, greenOptIcon;
    TextView purpose, below_purpose, dates, greenOptText, redOptText, blackOptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_forms);

        progressDialog = new ProgressDialog(this);
        allForm_Field_Records = new ArrayList<>();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        getFromIntent();
        init();
        loadList();
    }
    private void getFromIntent() {

        if (getIntent().getExtras() != null) {
            ORGANIZATION_ID = getIntent().getExtras().getString("ORGANIZATION_ID");
            IS_THIS_ORGANIZATION = getIntent().getExtras().getBoolean("IS_THIS_ORGANIZATION");
            IS_THIS_FORM = getIntent().getExtras().getBoolean("IS_THIS_FORM");
            IS_THIS_RECORD = getIntent().getExtras().getBoolean("IS_THIS_RECORD");
            FORM_ID = getIntent().getExtras().getString("FORM_ID");
            FORM_NAME = getIntent().getExtras().getString("FORM_NAME");
        }

        manageReference();
    }

    private void manageReference() {
        showProgressDialog();
        if (IS_THIS_FORM){
            REFERENCE_CHILD = "FORMS";
        }else {
            if (IS_THIS_RECORD){
                REFERENCE_CHILD = "FORMS/"+ FORM_ID +"/RECORDS";
            }else {
                REFERENCE_CHILD = "FORMS/"+ FORM_ID +"/FIELDS";
            }
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS")
                .child(ORGANIZATION_ID)
                .child(REFERENCE_CHILD);

    }

    private void showProgressDialog() {
        if (IS_THIS_FORM){
            progressDialog.setTitle("Forms");
            progressDialog.setMessage("listing all forms ...");
        }else {
            if (IS_THIS_RECORD){
                progressDialog.setTitle("Show Records");
                progressDialog.setMessage("fetching all records ...");
            }else {
                progressDialog.setTitle("Show Records");
                progressDialog.setMessage("fetching all fields ...");
            }
        }
        progressDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        formPageTitle = findViewById(R.id.formPageTitle);
        formPageTitle.setOnClickListener(this);

        countOfForm = findViewById(R.id.countOfForm);

        filter = findViewById(R.id.filter);

        errorTextOnListOfForms = findViewById(R.id.errorTextOnListOfForms);

        bottomSheetOpener = findViewById(R.id.bottomSheetOpener);
        bottomSheetOpener.setOnClickListener(this);

        whatToAdd = findViewById(R.id.whatToAdd);

        if (IS_THIS_FORM){
            formPageTitle.setText("All Forms");
            filter.setVisibility(View.GONE);
            whatToAdd.setText(R.string.add_new_form);
        }else {
            formPageTitle.setText(FORM_NAME);
            whatToAdd.setText(R.string.add_new_field);

            filter.setVisibility(View.VISIBLE);

            fieldFilter = findViewById(R.id.fieldFilter);
            recordFilter = findViewById(R.id.recordFilter);

            fieldFilter.setOnClickListener(this);
            recordFilter.setOnClickListener(this);

            manageColor();
        }

        setRecyclerView();
        referencingBottomSheet();
    }

    private void setRecyclerView() {
        recyclerViewOfForms = findViewById(R.id.recyclerViewOfForms);
        recyclerViewOfForms.setHasFixedSize(true);

    }
    private void referencingBottomSheet() {
        bottomSheetView = findViewById(R.id.bottom_sheet_for_form);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        cancel = findViewById(R.id.cancel);
        purpose = findViewById(R.id.purpose);
        below_purpose = findViewById(R.id.below_purpose);

        editTexts = findViewById(R.id.editTexts);
        form_field_Title = findViewById(R.id.form_field_Title);
        form_field_Description = findViewById(R.id.form_field_Description);
        add_Form_Field = findViewById(R.id.add_Form_Field);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(30, 100, 30, (int) (getApplicationContext()
                .getResources()
                .getDisplayMetrics()
                .heightPixels*0.45));
        add_Form_Field.setLayoutParams(params);

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
        greenOpt.setOnClickListener(this);
        redOpt.setOnClickListener(this);
        blackOpt.setOnClickListener(this);
        add_Form_Field.setOnClickListener(this);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState==BottomSheetBehavior.STATE_COLLAPSED){

                    purpose.setText("");
                    below_purpose.setText("");
                    form_field_Title.setText("");
                    form_field_Description.setText("");

                    editTexts.setVisibility(View.GONE);

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

    @SuppressLint("SetTextI18n")
    private void loadList() {

        form_fieldRecordAdapter = new Form_Field_RecordAdapter(allForm_Field_Records,ORGANIZATION_ID, IS_THIS_ORGANIZATION, IS_THIS_FORM, IS_THIS_RECORD,this,this);
        recyclerViewOfForms.setAdapter(form_fieldRecordAdapter);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countOfForm.setText("");
                allForm_Field_Records.clear();
                if (snapshot.exists()){
                    int number_of_data = (int) snapshot.getChildrenCount();
                    int count = 1;
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Form_Field_Record_Model form_field_record_model = snapshot1.getValue(Form_Field_Record_Model.class);

                        if (form_field_record_model != null) allForm_Field_Records.add(form_field_record_model);
                        form_fieldRecordAdapter.notifyDataSetChanged();

                        if (count == number_of_data){
                            endSearching();
                            break;
                        }
                        count++;
                    }
                }else {
                    endSearching();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                errorTextOnListOfForms.setText(error.getMessage());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void endSearching() {
        if (allForm_Field_Records.size() == 0){
            countOfForm.setText("");
            if (IS_THIS_FORM){
                errorTextOnListOfForms.setText("Add new Form\nAll the available form registered in your organization will be displayed here");
            }else {
                if (IS_THIS_RECORD){
                    errorTextOnListOfForms.setText("All the recorded data in "+ FORM_NAME +" will be displayed here");
                }else {
                    errorTextOnListOfForms.setText("Add new Field in "+ FORM_NAME +"\nAll the added field in "+ FORM_NAME +" will be displayed here");
                }
            }
        }else {
            countOfForm.setText("("+allForm_Field_Records.size()+")");
            errorTextOnListOfForms.setText("");
        }
        progressDialog.dismiss();
    }

    @SuppressLint("SetTextI18n")
    private void openBottomSheetForAdding() {
        editTexts.setVisibility(View.VISIBLE);
        if (IS_THIS_FORM){
            purpose.setText("Create new Form");
            form_field_Title.setHint(getString(R.string.form_title));
            add_Form_Field.setText(R.string.create_form);
        }else {
            purpose.setText(Html.fromHtml("Add new field in<br><b>"+FORM_NAME+"</b>"));
            form_field_Title.setHint(getString(R.string.field_title));
            add_Form_Field.setText(R.string.create_field);
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        return;
    }
    private void firstCheckDataAndAdd() {
        String title = form_field_Title.getText().toString().trim(),
                desc = form_field_Description.getText().toString();

        if (!title.isEmpty()){
            if (title.length()<=100){
                if (!desc.isEmpty()){
                    if (desc.length()<=1000){
                        closeKeyboard();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        if (IS_THIS_FORM){
                            progressDialog.setTitle("Create Form");
                            progressDialog.setMessage("creating "+title+" ...");
                        }else {
                            progressDialog.setTitle("Create Field");
                            progressDialog.setMessage("adding "+title+" in "+ FORM_NAME +" ...");
                        }
                        progressDialog.show();

                        addFormOrField(title,desc);

                    }else {
                        displayToast(getString(R.string.long_desc), All_Forms_Fields_Records.this);
                    }
                }else {
                    displayToast(getString(R.string.desc_empty), All_Forms_Fields_Records.this);
                }
            }else {
                if (IS_THIS_FORM){
                    displayToast(getString(R.string.long_form_title), All_Forms_Fields_Records.this);
                }else {
                    displayToast(getString(R.string.long_field_title), All_Forms_Fields_Records.this);
                }
            }
        }else {
            if (IS_THIS_FORM){
                displayToast(getString(R.string.form_title_empty), All_Forms_Fields_Records.this);
            }else {
                displayToast(getString(R.string.field_title_empty), All_Forms_Fields_Records.this);
            }
        }
    }
    private void addFormOrField(String title, String desc) {
        String uniqueID = UUID.randomUUID().toString();

        HashMap<String ,Object> data = new HashMap<>();
        data.put("ID",uniqueID);
        data.put("CreatedBy", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        data.put("Date", DateFormat.getDateTimeInstance()
                .format(new Date()));
        data.put("Description", desc);
        data.put("Title",title);

        databaseReference.child(uniqueID).setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        refreshPage();
                        return;
                    }else {
                        progressDialog.dismiss();
                        displayToast(getString(R.string.smth_wrong), All_Forms_Fields_Records.this);
                    }
                });
        return;
    }
    @Override
    public void onFormOptionClicked(String form_id, String form_title) {
        FORM_ID = form_id;
        FORM_NAME = form_title;
        purpose.setText(Html.fromHtml("Choose an option for <br><b>"+FORM_NAME+"</b>"));

        blackOptText.setText(R.string.open_form);
        blackOptIcon.setImageResource(R.drawable.open);
        blackOpt.setVisibility(View.VISIBLE);

        greenOptText.setText(R.string.use_form);
        greenOptIcon.setImageResource(R.drawable.text);
        greenOpt.setVisibility(View.VISIBLE);

        if(IS_THIS_ORGANIZATION){
            redOptText.setText(R.string.delete_form);
            redOptIcon.setImageResource(R.drawable.delete);
            redOpt.setVisibility(View.VISIBLE);
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        return;
    }
    @Override
    public void onDeleteFieldClicked(String field_id, String field_title) {
        showAlertDialog("<b>"+field_title+"</b> from <b>"+FORM_NAME+"</b>", field_id);
    }

    public void showAlertDialog(String what_to_delete, String id){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete Alert");
        alertDialog.setMessage(Html.fromHtml("Do you really want to delete "+what_to_delete+" ?"));

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, Html.fromHtml("<span style=\"color: red\">Delete</span>"), (dialog, which) -> {
            alertDialog.dismiss();
            delete(id);
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel", (dialog, which) -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
    private void delete(String id) {
        databaseReference.child(id).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        allForm_Field_Records.clear();
                        refreshPage();
                        displayToast(getString(R.string.done), All_Forms_Fields_Records.this);
                    }else {
                        displayToast(getString(R.string.smth_wrong), All_Forms_Fields_Records.this);
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.formPageTitle:
                displayToast(getString(R.string.refreshing), All_Forms_Fields_Records.this);
                refreshPage();
                break;
            case R.id.fieldFilter:
                IS_THIS_FORM = false;
                IS_THIS_RECORD = false;
                manageColor();
                manageReference();
                loadList();
                break;
            case R.id.recordFilter:
                IS_THIS_FORM = false;
                IS_THIS_RECORD = true;
                manageColor();
                manageReference();
                loadList();
                break;
            case R.id.cancel:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.bottomSheetOpener:
                openBottomSheetForAdding();
                break;
            case R.id.add_Form_Field:
                firstCheckDataAndAdd();
                break;
            case R.id.greenOpt:
                openOCRActivity();
                break;
            case R.id.redOpt:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAlertDialog("<b>"+FORM_NAME+"</b>",FORM_ID);
                break;
            case R.id.blackOpt:
                showFieldAndRecord();
                break;
        }
    }

    private void showFieldAndRecord() {
        Intent formIntent = new Intent(this, All_Forms_Fields_Records.class);
        formIntent.putExtra("ORGANIZATION_ID",ORGANIZATION_ID);
        formIntent.putExtra("IS_THIS_ORGANIZATION", IS_THIS_ORGANIZATION);
        formIntent.putExtra("IS_THIS_FORM", false);
        formIntent.putExtra("IS_THIS_RECORD", false);
        formIntent.putExtra("FORM_ID", FORM_ID);
        formIntent.putExtra("FORM_NAME", FORM_NAME);
        startActivity(formIntent);
    }

    private void openOCRActivity() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Intent ocr = new Intent(this, OCR.class);
        ocr.putExtra("ORG_ID",ORGANIZATION_ID);
        ocr.putExtra("FORM_ID",FORM_ID);
        startActivity(ocr);
        return;
    }

    private void refreshPage() {
        allForm_Field_Records.clear();
        loadList();
    }

    private void manageColor() {
        if (IS_THIS_RECORD){
            recordFilter.setTextColor(Color.WHITE);
            recordFilter.setBackgroundResource(R.color.black_transparent);
            fieldFilter.setTextColor(Color.BLACK);
            fieldFilter.setBackgroundResource(R.color.white);
        }else {
            fieldFilter.setTextColor(Color.WHITE);
            fieldFilter.setBackgroundResource(R.color.black_transparent);
            recordFilter.setTextColor(Color.BLACK);
            recordFilter.setBackgroundResource(R.color.white);
        }
    }
    public void closeKeyboard() {
        focusedView = this.getCurrentFocus();
        if (focusedView != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
}