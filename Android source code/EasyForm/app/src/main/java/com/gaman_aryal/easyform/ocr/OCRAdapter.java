package com.gaman_aryal.easyform.ocr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaman_aryal.easyform.R;
import com.gaman_aryal.easyform.form_field_record.Form_Field_Record_Model;

import java.util.List;

public class OCRAdapter extends RecyclerView.Adapter<OCRAdapter.OCRViewHolder> {

    private final Context context;
    private final List<Form_Field_Record_Model> allExtractedData;
    private final OnDataEditListener onDataEditListener;
    private final String formTitle;

    public OCRAdapter(List<Form_Field_Record_Model> allExtractedData, Context context, OnDataEditListener onDataEditListener, String formTitle) {
        this.context = context;
        this.allExtractedData = allExtractedData;
        this.onDataEditListener = onDataEditListener;
        this.formTitle = formTitle;
    }

    @NonNull
    @Override
    public OCRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_data_receiver, parent, false);
        return new OCRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OCRViewHolder holder, int position) {
        holder.setData(allExtractedData.get(position), onDataEditListener);
    }

    @Override
    public int getItemCount() {
        return allExtractedData.size();
    }

    public class OCRViewHolder extends RecyclerView.ViewHolder{

        TextView ocrTitle, ocrDesc, ocrError;
        EditText ocrData;
        OnDataEditListener onDataEditListener;

        public OCRViewHolder(@NonNull View itemView) {
            super(itemView);

            ocrTitle = itemView.findViewById(R.id.ocrTitle);
            ocrDesc = itemView.findViewById(R.id.ocrDesc);
            ocrData = itemView.findViewById(R.id.ocrData);
            ocrError = itemView.findViewById(R.id.ocrError);
        }

        @SuppressLint("SetTextI18n")
        public void setData(Form_Field_Record_Model eachData, OnDataEditListener onDataEditListener) {
            this.onDataEditListener = onDataEditListener;
            final boolean[] isReasonSeen = {false};

            Log.i("TAG", "title:- "+eachData.getTitle());
            Log.i("TAG", "data:- "+eachData.getDate());
            Log.i("TAG", " ");

            try {
                ocrTitle.setText(eachData.getTitle());
            }catch (Exception ignored){

            }
            try {
                ocrDesc.setText(eachData.getDescription());
            }catch (Exception ignored){

            }
            try {
                ocrData.setText(eachData.getDate());
            }catch (Exception ignored){

            }
            try {
                if (eachData.getDate() == null){
                    setNoDataError(eachData.getTitle());
                    ocrError.setVisibility(View.VISIBLE);
                }else {
                    ocrError.setVisibility(View.GONE);
                    ocrError.setText("");
                }
            }catch (Exception e){
                setNoDataError(eachData.getTitle());
                ocrError.setVisibility(View.VISIBLE);
            }


            ocrData.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onDataEditListener.onDataEdited(eachData.getTitle(),ocrData.getText().toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            ocrError.setOnClickListener(v -> {
                if (isReasonSeen[0]){
                    setNoDataError(eachData.getTitle());
                    isReasonSeen[0] = false;
                }else {
                    ocrError.setText(Html.fromHtml("<br><i>This might be because of following reason :-<br> " +
                            " - Not having "+eachData.getTitle()+" in <b>"+formTitle+"</b>'s sample form in your database library.<br>" +
                            " - The spelling of "+eachData.getTitle()+" might be different than in <b>"+formTitle+"</b>'s the sample form in your database library.<br>" +
                            " - The entered data was so messy in "+eachData.getTitle()+" due to which system couldn't detect it.<br>" +
                            " <b>[Hide reasons]</b></i>"));
                    isReasonSeen[0] = true;
                }

            });
        }

        private void setNoDataError(String data_title) {
            ocrError.setText(Html.fromHtml("<i><span style=\"color: red\">We could not present the data in <b>"+data_title +"</b>.<br>" +
                    "<b>[View the reason]</b><i>"));
        }
    }

    public interface OnDataEditListener{
        void onDataEdited(String title, String data);
    }
}
