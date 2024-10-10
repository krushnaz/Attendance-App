package com.ams.models;

public class Room {
    private String roomId;
    private String roomName;
    private double latitude;
    private double longitude;

    // Default constructor required for Firebase
    public Room() {
    }

    public Room(String roomId, String roomName, double latitude, double longitude) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
