package com.ams.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;

import com.ams.R;
import com.ams.models.AttendanceRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelReportGenerator {

    private static final String CHANNEL_ID = "report_channel";
    private static final int NOTIFICATION_ID = 1;

    public static void generateAttendanceReport(Context context, List<AttendanceRecord> records, String subjectName, String lectureType) {
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

        // Create college name row
        Row collegeNameRow = sheet.createRow(0);
        collegeNameRow.createCell(0).setCellValue("Sinhgad Institute of Management and Computer Application, Pune");
        collegeNameRow.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8)); // Merge cells for the college name
        collegeNameRow.setHeightInPoints(30); // Set height for college name row

// Create report title row
        Row reportTitleRow = sheet.createRow(1);
        reportTitleRow.createCell(0).setCellValue("Attendance Report");
        reportTitleRow.getCell(0).setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 8)); // Merge cells for the report title
        reportTitleRow.setHeightInPoints(25); // Set height for report title row

// Create subject info row
        Row subjectInfoRow = sheet.createRow(2);
        String subjectInfo = String.format("Subject: %s | Lecture Type: %s", subjectName, lectureType);
        subjectInfoRow.createCell(0).setCellValue(subjectInfo);
        subjectInfoRow.getCell(0).setCellStyle(infoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 8)); // Merge cells for subject info
        subjectInfoRow.setHeightInPoints(20); // Set height for subject info row

        // Create header row
        Row headerRow = sheet.createRow(3);
        String[] headers = {"Sr No", "Roll No", "Student Name", "Subject Name", "Lecture Type", "Date", "Time", "Status", "Room Name"};

// Create a CellStyle for the header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // Center align

// Create a CellStyle for data alignment
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER); // Center align

// Set the headers and apply the style
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
            headerRow.getCell(i).setCellStyle(headerStyle); // Apply style to header cell
            sheet.setColumnWidth(i, 20 * 256); // Set column width (20 characters wide)
        }

// Add data rows
        int rowNum = 4; // Start from row 4 for data
        for (AttendanceRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 4); // Adjusting index for serial number
            row.createCell(1).setCellValue(record.getStudentRollNo());
            row.createCell(2).setCellValue(record.getStudentName());
            row.createCell(3).setCellValue(subjectName); // Use passed subject name
            row.createCell(4).setCellValue(lectureType); // Use passed lecture type
            row.createCell(5).setCellValue(record.getDate());
            row.createCell(6).setCellValue(record.getTime());
            row.createCell(7).setCellValue(record.getStatus());
            row.createCell(8).setCellValue(record.getRoomName());

            // Apply the data style to each cell in the row
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle); // Apply style to data cell
            }
        }


        // Create directory in Downloads folder
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

// No need to create a new folder, just reference the Downloads folder directly
        if (!downloadsFolder.exists()) {
            if (!downloadsFolder.mkdirs()) {
                Toast.makeText(context, "Failed to access Downloads folder", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Write to file
        String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = String.format("AttendanceReport_%s_%s.xlsx", subjectName, dateString);
        File file = new File(downloadsFolder, fileName);
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            sendNotification(context, file);

        } catch (IOException e) {
            Toast.makeText(context, "Failed to generate report: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void sendNotification(Context context, File file) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Report Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Use FileProvider to get a content URI
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Use FLAG_IMMUTABLE for the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)  // Replace with your own notification icon
                .setContentTitle("Report Downloaded")
                .setContentText("The attendance report has been downloaded successfully.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
