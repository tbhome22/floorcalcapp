package com.example.floorboardcalculator.core.datamodel;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Customer implements Serializable {
    public ObjectId _id;
    public String CustName;
    public Date AddDate;
    public int BuildingType;
    public String Address;
    public String PostalCode;
    public String City;
    public String State;
    public String ContactNo;
    public String Referral;
    public double Lat;
    public double Lng;
    public ArrayList<FloorPlan> FloorPlan;
    public FloorInf FloorInf;

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return Lng;
    }

    public void setLng(double lng) {
        Lng = lng;
    }

    public FloorInf getFloorInf() {
        return FloorInf;
    }

    public void setFloorInf(FloorInf floorInf) {
        FloorInf = floorInf;
    }

    public String getReferral() {
        return Referral;
    }

    public void setReferral(String referral) {
        Referral = referral;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCustName() {
        return CustName;
    }

    public void setCustName(String custName) {
        CustName = custName;
    }

    public Date getAddDate() {
        return AddDate;
    }

    public void setAddDate(Date addDate) {
        AddDate = addDate;
    }

    public int getBuildingType() {
        return BuildingType;
    }

    public void setBuildingType(int buildingType) {
        BuildingType = buildingType;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getContactNo() {
        return ContactNo;
    }

    public void setContactNo(String contactNo) {
        ContactNo = contactNo;
    }

    public ArrayList<com.example.floorboardcalculator.core.datamodel.FloorPlan> getFloorPlan() {
        return FloorPlan;
    }

    public void setFloorPlan(ArrayList<com.example.floorboardcalculator.core.datamodel.FloorPlan> floorPlan) {
        FloorPlan = floorPlan;
    }
}
