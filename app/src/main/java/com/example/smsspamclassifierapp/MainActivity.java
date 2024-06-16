package com.example.smsspamclassifierapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_CODE = 2;
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private SpamMessageAdapter adapter;

    private BroadcastReceiver spamMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fetchSpamMessages(); // 새로운 스팸 메시지가 있을 때마다 목록 갱신
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 권한이 부여되지 않았을 경우 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        } else {
            Log.d(TAG, "SMS permission already granted");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        } else {
            Log.d(TAG, "Notification permission already granted");
        }

        // BroadcastReceiver 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(spamMessageReceiver, new IntentFilter("com.example.smsspamclassifierapp.SPAM_MESSAGE"));

        fetchSpamMessages();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SMS permission granted");
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "SMS permission denied");
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Notification permission denied");
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // BroadcastReceiver 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(spamMessageReceiver);
    }

    private void fetchSpamMessages() {
        ApiService apiService = RetrofitClient.getClient("http://203.252.230.245:5432").create(ApiService.class);
        apiService.getSpamMessages().enqueue(new Callback<List<SpamMessage>>() {
            @Override
            public void onResponse(Call<List<SpamMessage>> call, Response<List<SpamMessage>> response) {
                if (response.isSuccessful()) {
                    List<SpamMessage> spamMessages = response.body();
                    adapter = new SpamMessageAdapter(spamMessages);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Response not successful");
                    Toast.makeText(MainActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SpamMessage>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
