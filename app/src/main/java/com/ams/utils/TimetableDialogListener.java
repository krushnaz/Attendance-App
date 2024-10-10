package com.ams.utils;

import com.ams.models.Subject;

// TimetableDialogListener.java
public interface TimetableDialogListener {
    void onTimetableDialogSaveClick(int rowId, String day, Subject subject);
}