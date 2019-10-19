package com.cubeqw.copyband;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
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
    int miband;
    int bc;
    int tc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text =findViewById(R.id.text);
        sPref=getSharedPreferences("setup",MODE_PRIVATE);
        miband=sPref.getInt("miband", -1);
        if(miband==-1){
            Intent intent=new Intent(MainActivity.this, Setup.class);
            startActivity(intent);
            this.finish();
        }
        if (miband==2){
            bc=10;
            tc=80;
        }
        if (miband==1){
            bc=5;
            tc=123;
        }
        if (miband==0){
            bc=16;
            tc=123;
        }

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
        for (int i = 0; i < bc; i++) {
            if (first) {
                message = text.getText().toString();
                first = false;
            }
            createNotification();
            count++;
        }
        count=0;
        first=true;
       if(first){ NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.cancelAll();}
    }

    public void createNotification(){
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if(message.length()<=bc*tc) {
                try {
                    if (message.length() > tc - tc * count) {
                        String numbers = message.substring(message.length() - tc);
                        message = message.substring(0, message.length() - tc);
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
            }else {
        }}

}
