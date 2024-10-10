package com.ams.fragments;

import com.ams.models.Subject;

public interface GPSStatusListener {
    void onCountdownComplete(Subject subject, double latitude, double longitude);
}