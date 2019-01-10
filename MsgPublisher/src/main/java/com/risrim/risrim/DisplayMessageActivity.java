package com.risrim.risrim;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.util.concurrent.CountDownLatch;

public class DisplayMessageActivity extends AppCompatActivity {

    private static final String LOG_TAG = "RisRimMsgPublisher";
    private static final String NEWS_TICKER_IOT_ENDPOINT = "a3q6yyaptqkmga-ats.iot.us-east-1.amazonaws.com/mqtt";

    AWSIotMqttManager mqttManager;
    String clientId = "22ffu9tf5mtb6p4caj7duuu3sv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);


        // Initialize the credentials provider
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        Log.e(LOG_TAG, "onError: ", e);
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, NEWS_TICKER_IOT_ENDPOINT);

        try {
            connect();
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error: ", e);
        }
        try {
            publish();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error: ", e);
        }

        try {
            disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error: ", e);
        }

    }

    public void connect() {
        Log.d(LOG_TAG, "clientId = " + clientId);

        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
    }

    public void publish() {
        final String topic = "newstickerTopic";
        final String msg = "Hello from Android App";

        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }

    public void disconnect() {
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }

}
