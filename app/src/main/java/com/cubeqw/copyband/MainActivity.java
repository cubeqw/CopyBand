package com.cubeqw.copyband;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

    private static String CHANNEL_ID = "0";
    private static final String CHANNEL_NAME = "CopyBand";
    private static final String CHANNEL_DESC = "CopyBand Service";
    String message;
    int count=0;
    EditText text;
    boolean first=true;
    SharedPreferences sPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text =findViewById(R.id.text);
        sPref=getPreferences(MODE_PRIVATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            createNotification();
            return true;
        }
        return false;
    }
    public void onClick(View v){
        for (int i = 0; i < 16; i++) {
            if(first){
                message=text.getText().toString();
                first=false;
            }
            createNotification();
            count++;
        }
        count=0;
        first=true;
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.cancelAll();
    }

    public void createNotification(){
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if(message.length()<=123*16) {
                try {
                    if (message.length() > 123 - 123 * count) {
                        String numbers = message.substring(message.length() - 123);
                        message = message.substring(0, message.length() - 123);
                        mBuilder.setContentText(numbers);
                        mNotificationMgr.notify(1, mBuilder.build());
                    } else {
                        mBuilder.setContentText(message);
                        mNotificationMgr.notify(1, mBuilder.build());
                    }
                } catch (IndexOutOfBoundsException e) {
                    mBuilder.setContentText(message);
                    mNotificationMgr.notify(1, mBuilder.build());
                }

            }}
}
