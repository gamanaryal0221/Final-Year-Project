package com.gaman_aryal.easyform.registration_login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.home.Home;
import com.gaman_aryal.easyform.organization.OrganizationModel;
import com.gaman_aryal.easyform.organization.OrganizationAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Character.isUpperCase;

public class Registration_Login extends AppCompatActivity implements View.OnClickListener, OrganizationAdapter.OnOrganizationClickedListener {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseAuth auth;
    FirebaseUser me;
    DatabaseReference realtimeReference;
    StorageReference storageReference;
    ProgressDialog progressDialog;

    DisplayMetrics displayMetrics;
    int widthOfTheScreen, heightOfTheScreen;

    CircleImageView photo;
    LinearLayout registrationLayout, registrationSectionChooser;
    TextView title, layoutSwiper, Organization_not_available, staffSection, organizationSection, secretIDHolder;
    EditText email_or_id, name, address, post_or_pan, pass, confirmPass;
    RadioGroup genderRadioGroup;
    RadioButton isMale, isFemale, isOthers;

    Button organizationChooser, loginOrRegister;

    String EMAIL_OR_ID, NAME, ADDRESS, GENDER, POST_OR_PAN, PASSWORD, CONFIRM_PASSWORD;
    Uri IMAGE_URI;
    int Number_Of_History;

    DrawerLayout drawerLayout;
    NavigationView organizationListNav;
    View drawerHeader;

    RecyclerView organizationsListRecyclerView;
    List<OrganizationModel> allOrganizationList;
    OrganizationAdapter organizationAdapter;

    View focusedView;
    InputMethodManager imm;

    private static final int CAMERA_CODE = 100;
    private static final int STORAGE_CODE = 101;
    private static final int ALL_PERMISSIONS_CODE = 111;
    String[] ALL_NEEDED_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    String LISTENED_ORG_ID = "", LISTENED_ORG_NAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(Registration_Login.this);
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getSharedPreferences("EASYFORM", Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        me = auth.getCurrentUser();

        if (me != null) {

            Intent redirect = new Intent(Registration_Login.this, Home.class);
            redirect.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(redirect);

        } else {
            setContentView(R.layout.registration_login);

            realtimeReference = FirebaseDatabase.getInstance().getReference();
            storageReference = FirebaseStorage.getInstance().getReference("Profile Pictures");
            allOrganizationList = new ArrayList<>();

            calculatemobileSize();
            init();
            askAllPermissions();
        }
    }
    private void calculatemobileSize() {
        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        widthOfTheScreen = displayMetrics.widthPixels;
        heightOfTheScreen = displayMetrics.heightPixels;
    }
    private void askAllPermissions() {
        if (checkPermission(Manifest.permission.CAMERA) +
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) +
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Grant those permissions");
                builder.setMessage("Camera and Storage(read and write) permissions");
                builder.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(
                        Registration_Login.this,
                        ALL_NEEDED_PERMISSIONS,
                        ALL_PERMISSIONS_CODE
                ));
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        ALL_NEEDED_PERMISSIONS,
                        ALL_PERMISSIONS_CODE
                );
            }

        }

        return;
    }
    public int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }
    private void init() {
        title = findViewById(R.id.titleText);
        layoutSwiper = findViewById(R.id.layoutSwiper);
        registrationLayout = findViewById(R.id.registrationLayout);
        registrationSectionChooser = findViewById(R.id.registrationSectionChooser);

        staffSection = findViewById(R.id.staffSection);
        organizationSection = findViewById(R.id.organizationSection);

        photo = findViewById(R.id.photo);
        email_or_id = findViewById(R.id.email_or_id);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        genderRadioGroup = findViewById(R.id.radioGroup);
        isMale = findViewById(R.id.isMale);
        isFemale = findViewById(R.id.isFemale);
        isOthers = findViewById(R.id.isOthers);
        post_or_pan = findViewById(R.id.postOrPAN);
        organizationChooser = findViewById(R.id.organizationChooser);
        secretIDHolder = findViewById(R.id.secretIDHolder);
        pass = findViewById(R.id.pass);
        confirmPass = findViewById(R.id.confirmPass);
        loginOrRegister = findViewById(R.id.loginOrRegister);

        staffSection.setLayoutParams(new LinearLayout.LayoutParams((int) (widthOfTheScreen / 2.3), (int) (widthOfTheScreen / 9)));
        organizationSection.setLayoutParams(new LinearLayout.LayoutParams((int) (widthOfTheScreen / 2.3), (int) (widthOfTheScreen / 9)));

        drawerLayout = findViewById(R.id.drawerLayout);
        organizationListNav = findViewById(R.id.organizationListNav);
        drawerHeader = organizationListNav.getHeaderView(0);
        organizationsListRecyclerView = drawerHeader.findViewById(R.id.organizationsListRecyclerView);
        Organization_not_available = drawerHeader.findViewById(R.id.noOrgaizations);

        layoutSwiper.setOnClickListener(this);
        loginOrRegister.setOnClickListener(this);
        photo.setOnClickListener(this);
        staffSection.setOnClickListener(this);
        organizationSection.setOnClickListener(this);
        organizationChooser.setOnClickListener(this);

        isMale.setOnCheckedChangeListener((compoundButton, b) -> {
            isFemale.setChecked(false);
            isOthers.setChecked(false);
        });
        isFemale.setOnCheckedChangeListener((compoundButton, b) -> {
            isMale.setChecked(false);
            isOthers.setChecked(false);
        });
        isOthers.setOnCheckedChangeListener((compoundButton, b) -> {
            isMale.setChecked(false);
            isFemale.setChecked(false);
        });
        openLoginSection();
        return;
    }

    private void openLoginSection() {
        setTexts(R.string.title_signin,R.string.signin,"<font color=#ffffff>New here ? </font> <font color=#4FB829>Register</font>");
        closeKeyboard();
        registrationSectionChooser.setVisibility(View.GONE);
        email_or_id.setHint(R.string.email_or_id);
        pass.setHint(R.string.pass);
        email_or_id.setText("");
        pass.setText("");
        confirmPass.setVisibility(View.GONE);
        registrationLayout.setVisibility(View.GONE);
    }
    private void openRegisterSection() {
        setTexts(R.string.title_create_acc,R.string.register,"<font color=#ffffff>Existing user ? </font> <font color=#4FB829>Sign In</font>");
        confirmPass.setVisibility(View.VISIBLE);
        registrationLayout.setVisibility(View.VISIBLE);
        registrationSectionChooser.setVisibility(View.VISIBLE);
        pass.setHint(R.string.creaate_pass);
        confirmPass.setHint(R.string.confirm_pass);
        openStaffSection();
        return;
    }
    private void setTexts(int title, int buttonText, String bottom) {
        this.title.setText(title);
        loginOrRegister.setText(buttonText);
        layoutSwiper.setText(Html.fromHtml(bottom));
        return;
    }

    private void openStaffSection() {
        unselectAll();
        closeKeyboard();
        photo.setImageResource(R.drawable.default_photo);
        IMAGE_URI = Uri.parse("");
        organizationChooser.setText(R.string.select_organization);
        organizationChooser.setVisibility(View.VISIBLE);
        staffSection.setTextColor(Color.BLUE);
        organizationSection.setTextColor(Color.WHITE);
        photo.setVisibility(View.VISIBLE);
        email_or_id.setHint(R.string.s_id);
        name.setHint(R.string.s_name);
        genderRadioGroup.setVisibility(View.VISIBLE);
        address.setHint(R.string.s_address);
        post_or_pan.setHint(R.string.post);
        loadOrganizationList();
        return;
    }
    private void openOrganizationSection() {
        unselectAll();
        closeKeyboard();
        organizationSection.setTextColor(Color.BLUE);
        staffSection.setTextColor(Color.WHITE);
        photo.setVisibility(View.GONE);
        organizationChooser.setVisibility(View.GONE);
        email_or_id.setHint(R.string.o_email);
        name.setHint(R.string.o_name);
        genderRadioGroup.setVisibility(View.GONE);
        address.setHint(R.string.o_address);
        post_or_pan.setHint(R.string.o_pan);
        return;
    }
    private void unselectAll(){
        email_or_id.setText("");
        name.setText("");
        address.setText("");
        post_or_pan.setText("");
        pass.setText("");
        confirmPass.setText("");
    }
    private void loadOrganizationList() {
        organizationListNav.getLayoutParams().height = heightOfTheScreen;
        organizationListNav.getLayoutParams().width = widthOfTheScreen;

        organizationsListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager organizationLayoutManager = new LinearLayoutManager(Registration_Login.this, LinearLayoutManager.VERTICAL, true);
        organizationsListRecyclerView.setLayoutManager(organizationLayoutManager);
        organizationAdapter = new OrganizationAdapter(allOrganizationList, Registration_Login.this, Registration_Login.this);

        realtimeReference.child("ALL_ORGANIZATIONS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrganizationList.clear();
                new LoadOrganizationList().execute(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return ;
    }

    @Override
    public void onOrganizationSelected(String org_id, String org_name) {
        displayToast(org_name+" selected");
        LISTENED_ORG_ID = org_id;
        LISTENED_ORG_NAME = org_name;
        organizationChooser.setText(org_name);
        drawerLayout.closeDrawer(organizationListNav);

    }

    @SuppressLint("StaticFieldLeak")
    private class LoadOrganizationList extends AsyncTask<DataSnapshot,Integer,Void>{

        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            for (DataSnapshot dataSnapshot : dataSnapshots[0].getChildren()) {
                OrganizationModel eachOrganization = dataSnapshot.getValue(OrganizationModel.class);
                if (eachOrganization != null) {
                    allOrganizationList.add(eachOrganization);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (allOrganizationList.size() == 0) {
                Organization_not_available.setVisibility(View.VISIBLE);
            } else {
                Organization_not_available.setVisibility(View.GONE);
                organizationsListRecyclerView.setAdapter(organizationAdapter);
                organizationAdapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layoutSwiper:
                if (loginOrRegister.getText().equals(getString(R.string.signin))){
                    openRegisterSection();
                }else {
                    openLoginSection();
                }
                break;
            case R.id.staffSection:
                closeKeyboard();
                openStaffSection();
                break;
            case R.id.organizationSection:
                closeKeyboard();
                openOrganizationSection();
                break;
            case R.id.loginOrRegister:
                closeKeyboard();
                if (isNetworkConnected()){
                    if (loginOrRegister.getText().equals(getString(R.string.signin))){
                        moveForLogin();
                    }else {
                        moveForRegistration();
                    }
                }else {
                    displayToast(getString(R.string.no_network));
                }
                break;
            case R.id.organizationChooser:
                closeKeyboard();
                drawerLayout.openDrawer(organizationListNav);
                break;
            case R.id.photo:
                closeKeyboard();
                showOptions(v);
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void showOptions(View view) {
        PopupMenu popup = new PopupMenu(Registration_Login.this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.photo_opt, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.camera:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (isPermissionDenied(Manifest.permission.CAMERA, Registration_Login.this) || isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, Registration_Login.this)){
                            displayToast(getString(R.string.permission_denied));
                        }else {
                            openCamera();
                        }
                    }else {
                        openCamera();
                    }
                    break;
                case R.id.gallery:
                    if (isPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE, Registration_Login.this)){
                        displayToast(getString(R.string.permission_denied));
                    }else {
                        openGallery();
                    }
                    break;
            }
            return true;
        });
        popup.show();
    }
    public static boolean isPermissionDenied(String permission, Context context) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_DENIED;
    }

    private void moveForLogin() {
        EMAIL_OR_ID = Objects.requireNonNull(email_or_id.getText()).toString().trim();
        PASSWORD = Objects.requireNonNull(pass.getText()).toString().trim();
        if (!EMAIL_OR_ID.isEmpty()){
            if (!PASSWORD.isEmpty()){
                progressDialog.setTitle("Authenticating");
                progressDialog.setMessage("Searching your account ...");
                progressDialog.show();

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(EMAIL_OR_ID).matches()) {
                    login(false);
                } else {
                    EMAIL_OR_ID = EMAIL_OR_ID + "@g.com";
                    login(true);
                }
            }else {
                displayToast(getString(R.string.no_login_pass));
            }
        }else {
            displayToast(getString(R.string.no_id_or_email));
        }
        return;
    }

    private void login(boolean isStaff) {
        auth.signInWithEmailAndPassword(EMAIL_OR_ID, PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String path = "";
                        if (isStaff){
                            path = "ALL_STAFFS/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+"/OrganizationID";
                            FirebaseDatabase.getInstance().getReference("ALL_STAFFS").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    moveToHomepage(snapshot.child("OrganizationID").getValue(String.class),true,false);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(Registration_Login.this, Registration_Login.class));
                                    progressDialog.dismiss();
                                    displayToast(getString(R.string.smth_wrong));
                                }
                            });
                        }else {
                            moveToHomepage("",false,false);
                        }
                    } else {
                        progressDialog.dismiss();
                        displayToast(getString(R.string.invalid_email_pass));
                    }
                });
    }

    private void moveForRegistration() {
        if (genderRadioGroup.getVisibility()==View.VISIBLE) {
            if (IMAGE_URI != null) {
                checkFilledData(true);
            } else {
                displayToast("Please select image");
            }
        } else {
            checkFilledData(false);
        }
        return;
    }
    private void checkFilledData(boolean isAStaff) {

        EMAIL_OR_ID = Objects.requireNonNull(email_or_id.getText()).toString().trim();
        NAME = Objects.requireNonNull(name.getText()).toString().trim();
        POST_OR_PAN = Objects.requireNonNull(post_or_pan.getText()).toString().trim();
        ADDRESS = Objects.requireNonNull(address.getText()).toString().trim();
        PASSWORD = Objects.requireNonNull(pass.getText()).toString().trim();
        CONFIRM_PASSWORD = Objects.requireNonNull(confirmPass.getText()).toString().trim();

        if (isAStaff){
            if (isMale.isChecked()) {
                GENDER = "Male";
            } else if (isFemale.isChecked()) {
                GENDER = "Female";
            } else if (isOthers.isChecked()) {
                GENDER = "Others";
            } else {
                GENDER = "null";
            }
        }

        if (EMAIL_OR_ID.isEmpty() || NAME.isEmpty() || ADDRESS.isEmpty() || POST_OR_PAN.isEmpty() || PASSWORD.isEmpty()){
            displayToast(getString(R.string.smth_missing));
        }else {
            if (isPasswordValid(PASSWORD)){
                if (PASSWORD.equals(CONFIRM_PASSWORD)){
                    if (isAStaff){
                        if(android.util.Patterns.EMAIL_ADDRESS.matcher(EMAIL_OR_ID).matches()){
                            displayToast(getString(R.string.email_entered_than_id));
                        }else {
                            if (!GENDER.isEmpty()){
                                if (!LISTENED_ORG_ID.isEmpty()){
                                    if (!IMAGE_URI.toString().isEmpty()){
                                        progressDialog.setTitle("Adding new Staff");
                                        progressDialog.setMessage("Creating account ...");
                                        progressDialog.show();
                                        countHistory();
                                        register(true);
                                    }else {
                                        displayToast(getString(R.string.no_img));
                                    }
                                }else {
                                    displayToast(getString(R.string.organization_not_selected));
                                }
                            }else {
                                displayToast(getString(R.string.gender_missing));
                            }
                        }
                    }else {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(EMAIL_OR_ID).matches()){
                            progressDialog.setTitle("Adding new Organization");
                            progressDialog.setMessage("Creating account ...");
                            progressDialog.show();
                            register(false);
                        }else {
                            displayToast(getString(R.string.invalid_email));
                        }
                    }
                }else {
                    displayToast(getString(R.string.pass_not_match));
                }
            }else {
                displayToast(getString(R.string.pass_not_valid));
            }
        }
    }

    private void register(boolean isStaff) {
        String EMAIL = EMAIL_OR_ID;
        if (isStaff) EMAIL = EMAIL_OR_ID + "@g.com";

        auth.createUserWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        me = auth.getCurrentUser();
                        assert me != null;
                        if (isStaff) {
                            progressDialog.setMessage("Storing image ...");
                            storeImage(me.getUid());
                        } else {
                            saveOnRealTime(me.getUid(),"",false);
                        }
                    } else {
                        progressDialog.dismiss();
                        displayToast(Objects.requireNonNull(task.getException()).toString());
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
            displayToast(e.getMessage());
        });
    }

    private void storeImage(String firebaseID) {
        storageReference.child(firebaseID)
                .putFile(IMAGE_URI).addOnSuccessListener(taskSnapshot ->
                storageReference.child(firebaseID).getDownloadUrl()
                        .addOnSuccessListener(uri -> saveOnRealTime(firebaseID,String.valueOf(uri),true)))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    displayToast(e.getMessage());
                });
    }
    private void saveOnRealTime(String firebaseID, String imagePath, boolean isAStaff) {
        progressDialog.setMessage("Saving data ...");
        HashMap<Object, String> myData = new HashMap<>();
        String time = DateFormat.getDateTimeInstance().format(new Date());
        myData.put("ID", firebaseID);
        if (isAStaff) {
            myData.put("FullName", NAME);
            myData.put("OfficeID", EMAIL_OR_ID);
            myData.put("Profile_Photo", imagePath);
            myData.put("Gender", GENDER);
            myData.put("Post", POST_OR_PAN);
            myData.put("Address", ADDRESS);
            myData.put("OrganizationID", LISTENED_ORG_ID);

            HashMap<Object, String> request = new HashMap<>();
            request.put("ID", firebaseID);
            realtimeReference.child("ALL_ORGANIZATIONS/" + LISTENED_ORG_ID + "/REQUESTS/" + firebaseID)
                    .setValue(request);

            HashMap<Object, String> history = new HashMap<>();
            history.put("ID", firebaseID);
            history.put("Type", "Pending");
            history.put("Time", time);

            realtimeReference.child("ALL_ORGANIZATIONS/" + LISTENED_ORG_ID + "/HISTORY/").child(String.valueOf(Number_Of_History))
                    .setValue(history);

            realtimeReference = realtimeReference.child("ALL_STAFFS/" + firebaseID);
        } else {
            myData.put("PANNumber", POST_OR_PAN);
            myData.put("OrganizationName", NAME);
            myData.put("Address", ADDRESS);

            realtimeReference = realtimeReference.child("ALL_ORGANIZATIONS/" + firebaseID);
        }

        realtimeReference.setValue(myData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isAStaff){
                            setDatesOnStaff(time);
                            setNotification(NAME,imagePath);
                        }else {
                            moveToHomepage(LISTENED_ORG_ID, false,true);
                        }
                    } else {
                        progressDialog.dismiss();
                        displayToast(Objects.requireNonNull(task.getException()).getMessage());
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    displayToast(e.getMessage());
                });
    }

    private void setNotification(String staff_name, String staff_photo) {

        HashMap<Object, String> notification = new HashMap<>();
        notification.put("SENT_BY_NAME", staff_name);
        notification.put("SENT_BY_PHOTO", staff_photo);

        FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS")
                .child(LISTENED_ORG_ID)
                .child("NOTIFICATION")
                .child(String.valueOf(UUID.randomUUID()))
                .setValue(notification);
    }

    private void setDatesOnStaff(String time) {
        HashMap<Object, String> date = new HashMap<>();
        date.put("Time", time);
        realtimeReference.child("TIMES-"+LISTENED_ORG_ID+"/Pending").setValue(date)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        moveToHomepage(LISTENED_ORG_ID,true,true);
                    }else {
                        progressDialog.dismiss();
                        displayToast(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    private void moveToHomepage(String org_id, boolean isAStaff, boolean from_registration) {
        editor = sharedPreferences.edit();
        Intent register  = new Intent(Registration_Login.this, Home.class);
        if (isAStaff){
            editor.putString("ACCOUNT_TYPE", "STAFF");
            editor.putString("MY_ORG_ID", org_id);
        }else {
            editor.putString("ACCOUNT_TYPE", "ORGANIZATION");
            editor.putString("MY_ORG_ID", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        }
        if (editor.commit()){
            register.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            progressDialog.dismiss();
            if (from_registration){
                displayToast("Successfully Created");
            }else {
                displayToast("Authenticated");
            }
            startActivity(register);
        }else {
            progressDialog.dismiss();
            displayToast(getString(R.string.smth_wrong));
        }
        return;
    }

    private void countHistory() {
        FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS").child(LISTENED_ORG_ID + "/HISTORY")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Number_Of_History = (int) snapshot.getChildrenCount();
                        Number_Of_History = Number_Of_History+1;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return;
    }
    public boolean isPasswordValid(String str) {
        boolean checked;

        int NumberOfUpperCase = 0;
        int NumberOfSpecialCharacter = 0;
        int NumberOfnumbers = 0;
        int NumberOfdigit = 0;

        for (int i = 0; i < str.length(); i++) {

            int ascii = str.charAt(i);

            if (str.charAt(i) == '.' || str.charAt(i) == '!' || str.charAt(i) == '@' || str.charAt(i) == '#' || str.charAt(i) == '$' || str.charAt(i) == '%' || str.charAt(i) == '^' || str.charAt(i) == '&' || str.charAt(i) == '*') {
                NumberOfSpecialCharacter++;

            } else if (isUpperCase(str.charAt(i))) {
                NumberOfUpperCase++;

            } else if ((ascii >= 48 && ascii <= 57)) {
                NumberOfnumbers++;
            }
            NumberOfdigit++;
        }

        checked = (NumberOfSpecialCharacter > 0) && (NumberOfUpperCase > 0) && (NumberOfnumbers > 0) && ((NumberOfdigit > 6) && (NumberOfdigit < 13));
        return checked;
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, STORAGE_CODE);
        return;
    }
    private void openCamera() {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_CODE);

        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {

                assert data != null;
                Bitmap image = (Bitmap) data.getExtras().get("data");
                photo.setImageBitmap(image);
                IMAGE_URI = getImageURI(getApplicationContext(), image);

            } else if (requestCode == STORAGE_CODE && data != null && data.getData() != null) {
                IMAGE_URI = data.getData();
                photo.setImageURI(IMAGE_URI);
            }
        }
    }
    public Uri getImageURI(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(organizationListNav)) {
            drawerLayout.closeDrawer(organizationListNav);
        }else if (organizationChooser.getVisibility()==View.GONE) {
            openStaffSection();
        } else if (organizationChooser.getVisibility()==View.VISIBLE) {
            openLoginSection();
        } else {
            super.onBackPressed();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void closeKeyboard() {
        focusedView = this.getCurrentFocus();
        if (focusedView != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    private void displayToast(String toastMessage) {
        Toast.makeText(Registration_Login.this,
                toastMessage,
                Toast.LENGTH_SHORT)
                .show();
    }
}