package com.ams.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.ams.R;
import com.ams.models.AttendanceRecord;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttendaceExcelReportGenerator {
    private static final String CHANNEL_ID = "report_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "AttendaceExcelReportGenerator";

    // Updated method to dynamically generate attendance report
    public static void generateAttendanceReport(Context context, List<AttendanceRecord> records, Map<String, String> lectureStatusMap, String subjectName, String lectureType) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // Create styles
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18); // Font size 18
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle subtitleStyle = workbook.createCellStyle();
        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 16); // Font size 16
        subtitleStyle.setFont(subtitleFont);
        subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle infoStyle = workbook.createCellStyle();
        Font infoFont = workbook.createFont();
        infoFont.setBold(true);
        infoFont.setFontHeightInPoints((short) 14); // Font size 14
        infoStyle.setFont(infoFont);
        infoStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle centerAlignStyle = workbook.createCellStyle();
        centerAlignStyle.setAlignment(HorizontalAlignment.CENTER); // Center-align the content

        // Create college name row
        Row collegeNameRow = sheet.createRow(0);
        collegeNameRow.createCell(0).setCellValue("Sinhgad Institute of Management and Computer Application, Pune");
        collegeNameRow.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5)); // Merge cells for the college name
        collegeNameRow.setHeightInPoints(30); // Set height for college name row

        // Create report title row
        Row reportTitleRow = sheet.createRow(1);
        reportTitleRow.createCell(0).setCellValue("Attendance Report");
        reportTitleRow.getCell(0).setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 5)); // Merge cells for the report title
        reportTitleRow.setHeightInPoints(25); // Set height for report title row

        // Create subject info row
        Row subjectInfoRow = sheet.createRow(2);
        String subjectInfo = String.format("Subject: %s | Lecture Type: %s", subjectName, lectureType);
        subjectInfoRow.createCell(0).setCellValue(subjectInfo);
        subjectInfoRow.getCell(0).setCellStyle(infoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 5)); // Merge cells for subject info
        subjectInfoRow.setHeightInPoints(20); // Set height for subject info row

        // Create header row with fixed columns for Sr No, Roll No, Student Name, Subject Name, and Lecture Type
        Row headerRow = sheet.createRow(3);
        String[] headers = {"Sr No", "Roll No", "Student Name", "Subject Name", "Lecture Type"};

        // Set headers with style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // Increase the width of specific columns (Student Name and Subject Name)
        sheet.setColumnWidth(2, 6000); // Increase width of "Student Name"
        sheet.setColumnWidth(3, 6000); // Increase width of "Subject Name"

        // Now, find all unique dates from the records and add them as header columns dynamically
        Set<String> uniqueDatesSet = new LinkedHashSet<>(); // Use LinkedHashSet to maintain order
        for (AttendanceRecord record : records) {
            uniqueDatesSet.add(record.getDate()); // Collect unique dates
        }

        List<String> uniqueDates = new ArrayList<>(uniqueDatesSet);
        Collections.sort(uniqueDates, (date1, date2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy"); // Assuming your date format is "dd-MM-yyyy"
                return sdf.parse(date1).compareTo(sdf.parse(date2));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        // Add date headers dynamically starting from column index 5
        int dateColumnIndex = 5;
        for (String date : uniqueDates) {
            headerRow.createCell(dateColumnIndex).setCellValue(date);
            headerRow.getCell(dateColumnIndex).setCellStyle(headerStyle);
            dateColumnIndex++;
        }

        // Add data rows for each student
        int rowNum = 4;
        Map<String, Integer> studentRowMapping = new HashMap<>();

        for (AttendanceRecord record : records) {
            int rowIndex;
            if (studentRowMapping.containsKey(record.getStudentRollNo())) {
                rowIndex = studentRowMapping.get(record.getStudentRollNo());
            } else {
                rowIndex = rowNum++;
                studentRowMapping.put(record.getStudentRollNo(), rowIndex);
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);

            // Set each cell with center-aligned content
            if (row.getCell(0) == null) {
                Cell cell = row.createCell(0);
                cell.setCellValue(rowIndex - 3); // Sr No
                cell.setCellStyle(centerAlignStyle);
            }

            if (row.getCell(1) == null) {
                Cell cell = row.createCell(1);
                cell.setCellValue(record.getStudentRollNo());
                cell.setCellStyle(centerAlignStyle);
            }

            if (row.getCell(2) == null) {
                Cell cell = row.createCell(2);
                cell.setCellValue(record.getStudentName());
                cell.setCellStyle(centerAlignStyle);
            }

            if (row.getCell(3) == null) {
                Cell cell = row.createCell(3);
                cell.setCellValue(subjectName);
                cell.setCellStyle(centerAlignStyle);
            }

            if (row.getCell(4) == null) {
                Cell cell = row.createCell(4);
                cell.setCellValue(lectureType);
                cell.setCellStyle(centerAlignStyle);
            }

            // Insert attendance status under the corresponding date column
            int dateColIndex = 5;
            for (String date : uniqueDates) {
                if (record.getDate().equals(date)) {
                    Cell cell = row.createCell(dateColIndex);
                    cell.setCellValue(record.getStatus()); // Present/Absent status under the correct date
                    cell.setCellStyle(centerAlignStyle); // Center align the status
                    break;
                }
                dateColIndex++;
            }
        }

        // Save the workbook to a file
        saveReportToFile(context, workbook, subjectName);
    }


    // Method to save the report to a file and notify the user
    private static void saveReportToFile(Context context, Workbook workbook, String subjectName) {
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

// No need to create a new folder, just reference the Downloads folder directly
        if (!downloadsFolder.exists()) {
            if (!downloadsFolder.mkdirs()) {
                Toast.makeText(context, "Failed to access Downloads folder", Toast.LENGTH_LONG).show();
                return;
            }
        }

        String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = String.format("AttendanceReport_%s_%s.xlsx", subjectName, dateString);
        File file = new File(downloadsFolder, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            workbook.close();

            Log.d(TAG, "Report saved to: " + file.getAbsolutePath());
            sendNotification(context, file);

        } catch (IOException e) {
            Toast.makeText(context, "Failed to generate report: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Send a notification when the report is downloaded
    private static void sendNotification(Context context, File file) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Report Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Report Downloaded")
                .setContentText("The attendance report has been downloaded successfully.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
