package com.ams.utils;

public class RegistrationUtils {
    private static String biometricData;
    private static String facePhotoPath;

    public static void setBiometricData(String data) {
        biometricData = data;
    }

    public static String getBiometricData() {
        return biometricData;
    }

    public static void setFacePhotoPath(String path) {
        facePhotoPath = path;
    }

    public static String getFacePhotoPath() {
        return facePhotoPath;
    }
}

