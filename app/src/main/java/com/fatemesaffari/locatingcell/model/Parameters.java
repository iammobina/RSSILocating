package com.fatemesaffari.locatingcell.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Parameter")
public class Parameters {


    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "cellID")
    public int cellID;

    @ColumnInfo(name = "Longitude")
    public double Longitude;

    @ColumnInfo(name = "Latitude")
    public double Latitude;

    @ColumnInfo(name = "signal_strength")
    public int signal_strength;

    public Parameters(int cellID, double Longitude, double  Latitude, int signal_strength) {
        this.cellID = cellID;
        this.Longitude = Longitude;
        this.Latitude = Latitude;
        this.signal_strength = signal_strength;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCellID() {
        return cellID;
    }

    public Parameters setCellID(int cellID) {
        this.cellID = cellID;
        return this;
    }

    public double getLongitude() {
        return Longitude;
    }

    public Parameters setLongitude(double longitude) {
        Longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return Latitude;
    }

    public Parameters setLatitude(double latitude) {
        Latitude = latitude;
        return this;
    }

    public int getSignal_strength() {
        return signal_strength;
    }

    public Parameters setSignal_strength(int signal_strength) {
        this.signal_strength = signal_strength;
        return this;
    }

    public int getSignalStrength() {
        return signal_strength;
    }
}