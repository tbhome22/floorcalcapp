package com.example.floorboardcalculator.core.constant;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class StateList {
    private static Map<String, String> State;

    private StateList() {
        State = new HashMap<String, String>();
        State.put("jhr", "Johor (JHR)");
        State.put("kdh", "Kedah (KDH)");
        State.put("ktn", "Kelantan (KTN)");
        State.put("kul", "Kuala Lumpur (KUL)");
        State.put("lbn", "Labuan (LBN)");
        State.put("mlk", "Malacca (MLK)");
        State.put("nsn", "Negeri Sembilan (NSN)");
        State.put("phg", "Pahang (PHG)");
        State.put("png", "Pulau Pinang (PNG)");
        State.put("prk", "Perak (PRK)");
        State.put("pls", "Perlis (PLS)");
        State.put("pjy", "Putrajaya (PJY)");
        State.put("sbh", "Sabah (SBH)");
        State.put("swk", "Sarawak (SWK)");
        State.put("sgr", "Selangor (SGR)");
        State.put("trg", "Terengganu (TRG)");
    }

    public static String getState(@NonNull String label){
        new StateList();
        return State.get(label);
    }

    public static Map<String, String> getFullList() {
        new StateList();
        return State;
    }
}
