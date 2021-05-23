package com.gaman_aryal.easyform.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.form_field_record.Form_Field_Record_Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.gaman_aryal.easyform.registration_login.Registration_Login.isPermissionDenied;

public class OCR extends AppCompatActivity implements OCRAdapter.OnDataEditListener {

    ProgressDialog progressDialog;
    SharedPreferences SP;
    SharedPreferences.Editor editor;

    ScrollView beforeOCR;
    TextView title, description, imageInfo;
    String descriptionText;
    Button ocrButton;
    ImageView selectedImage;
    LinearLayout idSection;
    EditText serverID;
    RecyclerView ocrRecyclerView;

    DatabaseReference databaseReference;

    private static final int CAMERA_CODE = 100;
    private static final int STORAGE_CODE = 101;

    private Bitmap IMAGE_BITMAP;
    String organizationID,
            formID,
            requestUri,
            IMAGE_PATH = "";

    OkHttpClient okHttpClient;
    Request requestAPI;

    List<Form_Field_Record_Model> listOfData;
    OCRAdapter ocrAdapter;
    DataSnapshot snapshot;
    int Number_Of_Fields = 0;
    String[] titleArray, dataArray, descriptionArray;

    View focusedView;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.o_c_r);

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP = getSharedPreferences("EASYFORM", Context.MODE_PRIVATE);
        setRequestUri();

        progressDialog = new ProgressDialog(this);
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (getIntent().getExtras() != null) {
            organizationID = getIntent().getExtras().getString("ORG_ID");
            formID = getIntent().getExtras().getString("FORM_ID");

            databaseReference = FirebaseDatabase.getInstance().getReference("ALL_ORGANIZATIONS")
                    .child(organizationID + "/FORMS/" + formID);
        }

        init();
        listOfData = new ArrayList<>();
        okHttpClient = new OkHttpClient();
    }

    private void init() {
        beforeOCR = findViewById(R.id.beforeOCR);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);

        idSection = findViewById(R.id.idSection);
        serverID = findViewById(R.id.serverID);

        imageInfo = findViewById(R.id.imageInfo);
        selectedImage = findViewById(R.id.selectedImage);
        ocrButton = findViewById(R.id.ocrButton);
        ocrRecyclerView = findViewById(R.id.ocrRecyclerView);
        layoutSwapper(beforeOCR, ocrRecyclerView, R.string.detect_text);

        imageInfo.setText(R.string.before_image_selected);
        selectedImage.setVisibility(View.GONE);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot;
                Number_Of_Fields = (int) snapshot.child("FIELDS").getChildrenCount();
                title.setText(Html.fromHtml("<b>Title :<br></b>" + snapshot.child("Title").getValue(String.class)));
                descriptionText = "<b>Description :<br></b>" + snapshot.child("Description").getValue(String.class)
                        + "<br><br>It has " + Number_Of_Fields + " fields.<br>";

                description.setText(Html.fromHtml(descriptionText));
                defineArray();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (SP.getString("OCR_IP", "").isEmpty()) idSection.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void defineArray() {

        titleArray = new String[Number_Of_Fields];
        dataArray = new String[Number_Of_Fields];
        descriptionArray = new String[Number_Of_Fields];
        StringBuilder all_fields = new StringBuilder();

        int count = 0;
        for (DataSnapshot fields : snapshot.child("FIELDS").getChildren()) {

            titleArray[count] = fields.child("Title").getValue(String.class);
            descriptionArray[count] = fields.child("Description").getValue(String.class);
            dataArray[count] = null;

            all_fields.append("<b>[</b>").append(titleArray[count]).append("<b>]</b><br>");
            count = count + 1;
        }

        descriptionText = descriptionText + all_fields;

        description.setText(Html.fromHtml(descriptionText));
    }

    public void selectFromGallery(View view) {
        if (isPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE, OCR.this)) {
            displayToast(getString(R.string.permission_denied));
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, STORAGE_CODE);
        return;
    }

    public void clickUsingCamera(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionDenied(Manifest.permission.CAMERA, OCR.this) || isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, OCR.this)) {
                displayToast(getString(R.string.permission_denied));
            } else {
                openCamera();
            }
        } else {
            openCamera();
        }
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
                IMAGE_BITMAP = (Bitmap) data.getExtras().get("data");
                selectedImage.setImageBitmap(IMAGE_BITMAP);
                IMAGE_PATH = "";

            } else if (requestCode == STORAGE_CODE && data != null && data.getData() != null) {
                selectedImage.setImageURI(data.getData());
                IMAGE_PATH = getPath(getApplicationContext(), data.getData());
            }
            selectedImage.setVisibility(View.VISIBLE);
            imageInfo.setText(R.string.after_image_selected);
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void buttonClicked(View view) {
        if (ocrButton.getText().equals(getString(R.string.detect_text))) {

            if (Number_Of_Fields == 0) {
                displayToast("This form has no field to detect data");
            } else {
                if (SP.getString("OCR_IP", "").isEmpty()) {
                    displayToast("Please record your server id at first");
                } else {
                    if (selectedImage.getVisibility() == View.VISIBLE) {
                        progressDialog.setTitle(R.string.detect_text);
                        progressDialog.setMessage("analysing image");
                        progressDialog.show();
                        startTextDetection();
                    } else {
                        progressDialog.dismiss();
                        displayToast(getString(R.string.no_img));
                    }
                }
            }
        } else {
            closeKeyboard();
            progressDialog.setTitle(R.string.record_data);
            progressDialog.setMessage("storing current extracted data in database");
            progressDialog.show();

            saveDataOnRealtime();
        }
    }

    private void startTextDetection() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        if (!IMAGE_PATH.isEmpty()) IMAGE_BITMAP = BitmapFactory.decodeFile(IMAGE_PATH, options);
        IMAGE_BITMAP.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        sendRequest(stream.toByteArray());
    }

    private void sendRequest(byte[] image_in_byte) {

        RequestBody postRequest = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("form_image", formID, RequestBody.create(MediaType.parse("image/*jpg"), image_in_byte))
                .build();


        requestAPI = new Request.Builder().url(requestUri).post(postRequest).build();

        progressDialog.setMessage("detecting data from the form-image selected");
        okHttpClient.newCall(requestAPI).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    displayToast(e.getMessage());
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String response_data = Objects.requireNonNull(response.body().string());

                runOnUiThread(() -> {
                    if (response_data.equals("Sorry, This form format is not registered in your library")) {
                        progressDialog.dismiss();
                        displayToast(response_data);
                    } else {
                        progressDialog.setMessage("presenting detected data");
                        new SetExtractedData().execute(response_data);

                        ocrRecyclerView.setHasFixedSize(true);
                        ocrRecyclerView.setLayoutManager(new LinearLayoutManager(OCR.this, LinearLayoutManager.VERTICAL, false));
                        ocrAdapter = new OCRAdapter(listOfData, OCR.this, OCR.this, snapshot.child("Title").getValue(String.class));
                        ocrRecyclerView.setAdapter(ocrAdapter);

                        layoutSwapper(ocrRecyclerView, beforeOCR, R.string.record_data);

                    }
                });
            }
        });
    }

    public void saveServerPath(View view) {

        String SERVER_ID = serverID.getText().toString().trim();
        editor = SP.edit();
        editor.putString("OCR_IP", SERVER_ID);
        if (editor.commit()) {
            setRequestUri();
            closeKeyboard();
            displayToast("saved");
            idSection.setVisibility(View.GONE);
        } else {
            displayToast("could not record\ntry again");
        }
    }

    public void changeServerID(View view) {
        if (idSection.getVisibility() == View.VISIBLE) {
            idSection.setVisibility(View.GONE);
        } else {
            serverID.setText(SP.getString("OCR_IP", ""));
            idSection.setVisibility(View.VISIBLE);
        }
    }

    private void setRequestUri() {
        requestUri = "http://" + SP.getString("OCR_IP", "") + ":5000/POST";
    }

    public class SetExtractedData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            try {
                JSONObject jsonObject = new JSONObject(strings[0]);
                JSONArray jsonArray = jsonObject.getJSONArray("extracted_data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    for (int j = 0; j < Number_Of_Fields; j++) {
                        if (titleArray[j].equals(object.getString("FIELD"))) {
                            addData(dataArray[j] == null, object.getString("DATA"), j);
                            break;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void addData(boolean isNull, String extracted_data, int position) {
            if (!extracted_data.isEmpty()) {
                if (isNull) {
                    dataArray[position] = extracted_data;
                } else {
                    dataArray[position] = dataArray[position] + " " + extracted_data;
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            for (int i = 0; i < Number_Of_Fields; i++) {
                Form_Field_Record_Model eachData = new Form_Field_Record_Model();
                eachData.setTitle(titleArray[i]);
                eachData.setDescription(descriptionArray[i]);
                eachData.setDate(dataArray[i]);

                listOfData.add(eachData);
                ocrAdapter.notifyDataSetChanged();

                if (i == Number_Of_Fields - 1) {
                    progressDialog.dismiss();
                    if (listOfData.size() == 0) displayToast(getString(R.string.smth_wrong));
                }
            }

        }
    }

    @Override
    public void onDataEdited(String title, String data) {
        for (int i = 0; i < Number_Of_Fields; i++) {
            if (titleArray[i].equals(title)) {
                if (data.isEmpty()) {
                    dataArray[i] = null;
                } else {
                    dataArray[i] = data;
                }
            }
        }
    }

    private void saveDataOnRealtime() {

        int null_data_count = 0;
        String record_id = UUID.randomUUID().toString();

        HashMap<Object, String> record = new HashMap<>();

        record.put("ID", record_id);
        record.put("Date", DateFormat.getDateTimeInstance().format(new Date()));
        record.put("CreatedBy", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < Number_Of_Fields; i++) {
            if (dataArray[i] == null) {
                null_data_count++;
            } else {
                data.append("<br><b>").append(titleArray[i]).append(" :-</b><br><i>").append(dataArray[i]).append("</i><br>");
            }
            if (i == Number_Of_Fields - 1) {
                if (null_data_count == Number_Of_Fields) {
                    progressDialog.dismiss();
                    displayToast("no data to record");
                } else {
                    record.put("Description", data.toString());
                    saving(record, record_id);
                }
                return;
            }
        }
    }

    private void saving(HashMap<Object, String> record, String record_id) {
        databaseReference.child("RECORDS/" + record_id)
                .setValue(record)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {

                        selectedImage.setImageBitmap(null);
                        selectedImage.setVisibility(View.GONE);
                        imageInfo.setText(R.string.before_image_selected);

                        backPressHandler();
                        displayToast(getString(R.string.data_recorded));
                    } else {
                        displayToast(getString(R.string.smth_wrong));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (idSection.getVisibility() == View.VISIBLE) {
            idSection.setVisibility(View.GONE);
        } else {
            backPressHandler();
        }
    }

    public void close(View view) {
        closeKeyboard();
        backPressHandler();
    }

    public void backPressHandler() {
        if (ocrRecyclerView.getVisibility() == View.VISIBLE) {
            layoutSwapper(beforeOCR, ocrRecyclerView, R.string.detect_text);
            listOfData.clear();
            ocrAdapter.notifyDataSetChanged();
            Arrays.fill(dataArray, null);
        } else {
            super.onBackPressed();
        }
    }

    private void displayToast(String msg) {
        Toast.makeText(this,
                msg,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void layoutSwapper(View to_visible, View to_gone, int button_text_id) {
        to_gone.setVisibility(View.GONE);
        ocrButton.setText(button_text_id);
        to_visible.setVisibility(View.VISIBLE);
    }

    public void closeKeyboard() {
        focusedView = this.getCurrentFocus();
        if (focusedView != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}