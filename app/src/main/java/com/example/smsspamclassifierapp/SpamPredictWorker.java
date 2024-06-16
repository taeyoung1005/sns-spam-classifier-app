package com.example.smsspamclassifierapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpamPredictWorker extends Worker {

    private static final String TAG = "SpamPredictWorker";
    private static final String API_URL = "http://203.252.230.245:5432/predict"; // 실제 API 서버 주소로 변경
    private static final String CHANNEL_ID = "spam_notification_channel";

    public SpamPredictWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String phoneNumber = getInputData().getString("phone_number");
        String messageContent = getInputData().getString("content");

        Log.d(TAG, "Starting work for phone number: " + phoneNumber + " with content: " + messageContent);

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("phone_number", phoneNumber);
            jsonParam.put("content", messageContent);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonParam.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d(TAG, "Response Body: " + response.toString());

            conn.disconnect();

            // 서버 응답 처리
            JSONObject responseObject = new JSONObject(response.toString());
            String label = responseObject.getString("label");
            Log.d(TAG, "Message labeled as: " + label);

            if ("spam".equals(label)) {
                // 스팸으로 판단되면 알림 띄우기
                showNotification(getApplicationContext(), phoneNumber, messageContent);

                // MainActivity에 스팸 메시지를 전달하기 위해 LocalBroadcastManager 사용
                Intent localIntent = new Intent("com.example.smsspamclassifierapp.SPAM_MESSAGE");
                localIntent.putExtra("phone_number", phoneNumber);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS to API", e);
            return Result.failure();
        }
    }

    private void showNotification(Context context, String phoneNumber, String messageContent) {
        Log.d(TAG, "Preparing to show notification");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 안드로이드 오레오(API 26) 이상에서는 알림 채널이 필요합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Spam Notification";
            String description = "Notification for spam messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_spam) // 알림 아이콘 설정
                .setContentTitle("Spam Alert")
                .setContentText("Spam message detected from: " + phoneNumber)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // 알림 클릭 시 자동으로 삭제

        // 고유한 알림 ID를 사용
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification shown with ID: " + notificationId);
    }
}
