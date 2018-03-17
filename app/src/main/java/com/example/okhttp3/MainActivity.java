package com.example.okhttp3;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView tvDisplay;
    Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDisplay = (TextView) findViewById(R.id.tv_display);
    }

    public void apiCall(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchCall();
            }
        }).start();
    }

    private void searchCall() {
        final String data = NetworkService.INSTANCE.search("hello");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tvDisplay.setText(data);
            }
        });
    }
}
