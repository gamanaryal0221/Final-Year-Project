package com.gaman_aryal.easyform.form_field_record;

public class Form_Field_Record_Model {
    String Title, CreatedBy, Date, Description, ID;

    public Form_Field_Record_Model() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String name) {
        Title = name;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.CreatedBy = createdBy;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
