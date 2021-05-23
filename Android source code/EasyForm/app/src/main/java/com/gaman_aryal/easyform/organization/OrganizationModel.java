package com.gaman_aryal.easyform.organization;

public class OrganizationModel {

    String OrganizationName, ID, Address;

    public OrganizationModel() {
    }

    public OrganizationModel(String organizationName, String ID, String address) {
        OrganizationName = organizationName;
        this.ID = ID;
        Address = address;
    }

    public String getOrganizationName() {
        return OrganizationName;
    }

    public void setOrganizationName(String organizationName) {
        OrganizationName = organizationName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}

