package com.example.smsspamclassifierapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.HashMap;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static long lastTimestamp = 0;
    private static String lastMessageBody = "";
    private static HashMap<String, StringBuilder> messageMap = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();

                        // 메시지 조각을 결합
                        if (!messageMap.containsKey(sender)) {
                            messageMap.put(sender, new StringBuilder());
                        }
                        messageMap.get(sender).append(messageBody);
                    }

                    for (String sender : messageMap.keySet()) {
                        // 개행 문자를 공백으로 대체
                        String messageBodySingleLine = messageMap.get(sender).toString().replace("\n", " ").replace("\r", " ");

                        // 중복 메시지 확인
                        if (System.currentTimeMillis() - lastTimestamp < 3000 && messageBodySingleLine.equals(lastMessageBody)) {
                            Log.d(TAG, "Duplicate message detected, skipping...");
                            continue;
                        }

                        lastTimestamp = System.currentTimeMillis();
                        lastMessageBody = messageBodySingleLine;

                        // 로그 메시지 출력
                        Log.d(TAG, "Sender: " + sender + ", Message: " + messageBodySingleLine);

                        // API 요청을 보내기 위해 WorkManager 실행
                        Data data = new Data.Builder()
                                .putString("phone_number", sender)
                                .putString("content", messageBodySingleLine)
                                .build();

                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SpamPredictWorker.class)
                                .setInputData(data)
                                .build();

                        WorkManager.getInstance(context).enqueue(workRequest);

                        // 메시지 맵에서 제거
                        messageMap.remove(sender);
                    }
                }
            }
        }
    }
}
