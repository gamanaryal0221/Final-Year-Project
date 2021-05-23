package com.gaman_aryal.easyform.staff;

public class StaffModel {
    String Profile_Photo, OfficeID, FullName, Address, Gender, Post, ID;

    public StaffModel() {
    }

    public StaffModel(String profilePhoto, String officeID, String fullName, String address, String gender, String post, String ID) {
        Profile_Photo = profilePhoto;
        OfficeID = officeID;
        FullName = fullName;
        Address = address;
        Gender = gender;
        Post = post;
        this.ID = ID;
    }

    public String getProfile_Photo() {
        return Profile_Photo;
    }

    public void setProfile_Photo(String profile_Photo) {
        Profile_Photo = profile_Photo;
    }

    public String getOfficeID() {
        return OfficeID;
    }

    public void setOfficeID(String officeID) {
        OfficeID = officeID;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getPost() {
        return Post;
    }

    public void setPost(String post) {
        Post = post;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
