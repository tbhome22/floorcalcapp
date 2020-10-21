package com.example.floorboardcalculator.core.datamodel;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class FloorType implements Serializable {
    public ObjectId _id;
    public String abbr;
    public String full;
    public double base;
    public double base_8;
    public double base_15;
    public boolean checked = false;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public double getBase_8() {
        return base_8;
    }

    public void setBase_8(double base_8) {
        this.base_8 = base_8;
    }

    public double getBase_15() {
        return base_15;
    }

    public void setBase_15(double base_15) {
        this.base_15 = base_15;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
