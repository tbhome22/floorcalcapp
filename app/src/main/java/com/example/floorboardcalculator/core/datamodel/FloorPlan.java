package com.example.floorboardcalculator.core.datamodel;

import java.io.Serializable;

public class FloorPlan implements Serializable {
    public String name;
    public double width;
    public double height;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
