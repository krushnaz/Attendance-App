package com.ams.dialogs;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.ams.R;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QrCodeDialog extends Dialog {

    private ImageView ivQrCode;
    private MaterialButton btnDownloadQr;
    private ImageButton btnCancel;
    private Bitmap qrBitmap;
    private static final String CHANNEL_ID = "qr_download_channel";
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;

    private String subjectName;
    private String division;
    private String courseName;
    private String day;
    private String startTime;
    private String endTime;
    private String lectureType;
    private String room;
    private String teacherName;
    private String semester;
    private int rowId;

    public QrCodeDialog(@NonNull Context context, String subjectName, String semester, String division, String courseName, String day, String startTime,String endTime, String lectureType, String room,String teacherName,int rowId) {
        super(context, R.style.CustomDialog);
        this.subjectName = subjectName;
        this.division = division;
        this.courseName = courseName;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lectureType = lectureType;
        this.room = room;
        this.teacherName = teacherName;
        this.semester = semester;
        this.rowId = rowId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_qr_code);

        ivQrCode = findViewById(R.id.ivQrCode);
        btnDownloadQr = findViewById(R.id.btnDownloadQr);
        btnCancel = findViewById(R.id.btnCancel);

        // Set cancel button click listener
        btnCancel.setOnClickListener(v -> dismiss());

        // Set download button click listener
        btnDownloadQr.setOnClickListener(v -> downloadQrCode());

        // Generate and set QR code image
        qrBitmap = generateQrCode(subjectName,semester, division, courseName, day, startTime,endTime, lectureType, room,teacherName,rowId);
        ivQrCode.setImageBitmap(qrBitmap);

        // Create the notification channel
        createNotificationChannel();
    }

    private Bitmap generateQrCode(String subjectName,String semester, String division, String courseName, String day, String startTime,String endTime, String lectureType, String room, String teacherName,int rowId) {
        QRCodeWriter writer = new QRCodeWriter();
        Bitmap qrBitmap = null;

        try {
            // Create the QR code data string
            String qrData = "Subject: " + subjectName + "\n" +
                    "Course: " + courseName + "\n" +
                    "Semester: " + semester + "\n" +
                    "Division: " + division + "\n" +
                    "Day: " + day + "\n" +
                    "Start Time: " + startTime + "\n" +
                    "End Time: " + endTime + "\n" +
                    "Lecture Type: " + lectureType + "\n" +
                    "Room: " + room + "\n" +
                    "Row Id: " + rowId + "\n" +
                    "Teacher: " + teacherName;

            // Generate QR code bitmap
            BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            String time = startTime+" - "+endTime;
            // Add header text to QR code image
            return addHeaderTextToQrCode(qrBitmap, subjectName, day, time, lectureType, teacherName);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap addHeaderTextToQrCode(Bitmap qrBitmap, String subjectName, String day, String time, String lectureType, String teacherName) {
        // Create a new bitmap with space for a larger header and divider
        int headerHeight = 150; // Increased height of the header in pixels
        int dividerHeight = 4;  // Increased height of the divider line in pixels
        Bitmap bitmapWithHeader = Bitmap.createBitmap(qrBitmap.getWidth(), qrBitmap.getHeight() + headerHeight + dividerHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithHeader);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw the header background
        Paint headerPaint = new Paint();
        headerPaint.setColor(0xFFE0E0E0); // Background color for the header
        canvas.drawRect(0, 0, bitmapWithHeader.getWidth(), headerHeight, headerPaint);

        // Draw the header text
        paint.setColor(0xFF000000); // Black color for the text
        paint.setTextSize(40); // Increased text size for better visibility
        paint.setTextAlign(Paint.Align.CENTER);

        // Split the header text into three lines
        String headerText1 = subjectName;
        String headerText2 = day + " | " + time;
        String headerText3 = "";

        // Calculate vertical positions for the text
        float lineHeight = paint.descent() - paint.ascent();
        float y = (headerHeight - (lineHeight * 3)) / 2 + -paint.ascent(); // Center text vertically

        canvas.drawText(headerText1, bitmapWithHeader.getWidth() / 2, y, paint);
        y += lineHeight + 10; // Move down for the next line with extra spacing
        canvas.drawText(headerText2, bitmapWithHeader.getWidth() / 2, y, paint);
        y += lineHeight + 10; // Move down for the next line with extra spacing
        canvas.drawText(headerText3, bitmapWithHeader.getWidth() / 2, y, paint);

        // Draw the divider line below the header
        Paint dividerPaint = new Paint();
        dividerPaint.setColor(0xFF000000); // Black color for the divider line
        canvas.drawRect(0, headerHeight, bitmapWithHeader.getWidth(), headerHeight + dividerHeight, dividerPaint);

        // Draw the QR code below the divider
        canvas.drawBitmap(qrBitmap, 0, headerHeight + dividerHeight, null);

        return bitmapWithHeader;
    }


    private void downloadQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(getContext(), "QR code is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        OutputStream outputStream;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "qr_code_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                outputStream = getContext().getContentResolver().openOutputStream(
                        getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));
            } else {
                // For Android 9 (Pie) and below, save to external storage
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File qrCodeFile = new File(picturesDir, "qr_code_" + System.currentTimeMillis() + ".png");
                outputStream = new FileOutputStream(qrCodeFile);
            }

            // Compress and save the bitmap
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(getContext(), "QR code downloaded successfully", Toast.LENGTH_SHORT).show();
            showDownloadNotification();  // Show notification after successful download
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to download QR code", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show notification after download
    private void showDownloadNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions((AppCompatActivity) getContext(),
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_NOTIFICATION_PERMISSION);
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)  // Set your own notification icon
                .setContentTitle("Download Complete")
                .setContentText("QR code has been downloaded successfully.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);  // Auto-dismiss the notification when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(1, builder.build());
    }

    // Create notification channel (Required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "QR Code Download";
            String description = "Notification for QR code download";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
