package com.example.floorboardcalculator;

import android.util.Log;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class ConversionTest {
    public List<Room> testData;
    double max = 600.0, min = 150.00;

    @Test
    public void centimetresToFeet() {
        makeData();

        for(Room i : testData) {
            assertEquals(i.length / 30.48, Math.ceil(i.length / 30.48), 1.0);
            assertEquals(i.width / 30.48, Math.ceil(i.width / 30.48), 1.0);
        }
    }

    @Test
    public void centimetresToMillimetres() {
        makeData();

        for(Room i : testData) {
            assertEquals(i.length * 10, Math.ceil(i.length * 10), 1.0);
            assertEquals(i.width * 10, Math.ceil(i.width * 10), 1.0);
        }
    }

    @Test
    public void centimetresToMetres() {
        makeData();

        for(Room i : testData) {
            assertEquals(i.length / 100, Math.ceil(i.length / 100), 1.0);
            assertEquals(i.width / 100, Math.ceil(i.width / 100), 1.0);
        }
    }

    @Test
    public void centimetresToFeetSquare() {
        makeData();

        for(Room i : testData) {
            assertEquals((i.length * i.width) / 929f, Math.ceil(i.length * i.width) / 929f, 5.0);
        }
    }

    @Test
    public void centimetresToMetreSquare() {
        makeData();

        for(Room i : testData) {
            assertEquals((i.length * i.width) / 10000f, Math.ceil(i.length * i.width) / 10000f, 2.0);
        }
    }



    public void makeData() {
        testData = new ArrayList<Room>();

        for(int i=0; i<10; i++) {
            Room n = new Room();
            double range = max - min + 1;
            n.length = (Math.random() * range) + min;
            n.width = (Math.random() * range) + min;

            testData.add(n);
        }
    }

    public static class Room {
        public double length;
        public double width;
    }
}
