package com.example.floorboardcalculator.core.datamodel;

import java.io.Serializable;

public class FloorInf implements Serializable {
    public float skirtingLen;
    public boolean curvedArea;

    public float getSkirtingLen() {
        return skirtingLen;
    }

    public void setSkirtingLen(float skirtingLen) {
        this.skirtingLen = skirtingLen;
    }

    public boolean isCurvedArea() {
        return curvedArea;
    }

    public void setCurvedArea(boolean curvedArea) {
        this.curvedArea = curvedArea;
    }
}
