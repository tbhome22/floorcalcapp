package com.example.floorboardcalculator.core.config;

import java.io.Serializable;

public class PreferenceItem implements Serializable {
    public boolean unit;
    public boolean doubleUnit;
    public int unitSelect;
    public boolean priceExp;

    public boolean isUnit() {
        return unit;
    }

    public void setUnit(boolean unit) {
        this.unit = unit;
    }

    public boolean isDoubleUnit() {
        return doubleUnit;
    }

    public void setDoubleUnit(boolean doubleUnit) {
        this.doubleUnit = doubleUnit;
    }

    public int getUnitSelect() {
        return unitSelect;
    }

    public void setUnitSelect(int unitSelect) {
        this.unitSelect = unitSelect;
    }

    public boolean isPriceExp() {
        return priceExp;
    }

    public void setPriceExp(boolean priceExp) {
        this.priceExp = priceExp;
    }
}
