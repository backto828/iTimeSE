package com.chen.practice.itimese.model;

import java.io.Serializable;

public class Date implements Serializable {

    private static final long serialVersionUID = 1115047980946822546L;

    public int year, month, day;

    public Date(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Date() {
        this.year = 0;
        this.month = 0;
        this.day = 0;
    }
}