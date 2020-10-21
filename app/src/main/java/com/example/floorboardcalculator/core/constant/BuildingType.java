package com.example.floorboardcalculator.core.constant;

import java.util.ArrayList;
import java.util.List;

public class BuildingType {
    private static List<String> buildType;

    private BuildingType() {
        buildType = new ArrayList<String>();

        buildType.add("Flat");
        buildType.add("Condominium");
        buildType.add("Terrace House");
        buildType.add("Semi-D");
        buildType.add("Bungalow");
        buildType.add("Apartment");
    }

    public static String getType(int type) {
        new BuildingType();
        return buildType.get(type);
    }
}
