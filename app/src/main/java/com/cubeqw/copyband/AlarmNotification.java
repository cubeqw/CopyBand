/**************************************************************************
 *
 * Copyright (C) 2012-2015 Alex Taradov <alex@taradov.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *************************************************************************/

package com.cubeqw.copyband;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.net.Uri;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.util.Log;
import android.view.WindowManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmNotification extends Activity
{
  private final String TAG = "AlarmMe";
  private static String CHANNEL_ID = "0";
  private static final String CHANNEL_NAME = "CopyBand";
  private static final String CHANNEL_DESC = "CopyBand Service";
  private Timer mTimer = null;
  private Alarm mAlarm;
  private DateTime mDateTime;
  private TextView mTextView;
  private PlayTimerTask mTimerTask;
  LinearLayout clock;
  SharedPreferences sPref;
  int bc;
  int tc;
  String message;
  @Override
  protected void onCreate(Bundle bundle)
  {
    super.onCreate(bundle);
    setContentView(R.layout.notification);
    getWindow().addFlags(
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
      WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    mDateTime = new DateTime(this);
    mTextView=findViewById(R.id.reminder_title);
    readPreferences();
    clock=findViewById(R.id.clock);
    start(getIntent());
    sPref=getSharedPreferences("setup",MODE_PRIVATE);
    int miband = sPref.getInt("miband", -1);
    final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
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
    clock.startAnimation(animShake);
    createNotification();
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    Log.i(TAG, "AlarmNotification.onDestroy()");

    stop();
  }

  @Override
  protected void onNewIntent(Intent intent)
  {
    super.onNewIntent(intent);
    Log.i(TAG, "AlarmNotification.onNewIntent()");

    addNotification(mAlarm);

    stop();
    start(intent);
  }

  private void start(Intent intent)
  {
    mAlarm = new Alarm(this);
    mAlarm.fromIntent(intent);
    message=mAlarm.getTitle();
    mTextView.setText(message);
    Log.i(TAG, "AlarmNotification.start('" + mAlarm.getTitle() + "')");
    mTimerTask = new PlayTimerTask();
    mTimer = new Timer();
  }
  public void createNotification(){
    Log.d("CopyBand", "Notification created");
    for (int i = 0; i < bc; i++) {
      BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (!mBluetoothAdapter.isEnabled()) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_on), Toast.LENGTH_SHORT).show();
      }
      else {
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_MIN).setSound(null).setAutoCancel(true).setVibrate(new long[]{0L}).setSmallIcon(R.drawable.send_button);
        ;
        if (message.length() <= bc * tc) {
          try {
            if (message.length() > tc - tc * i) {
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
          }}}}}
  private void stop()
  {
    Log.i(TAG, "AlarmNotification.stop()");
    mTimer.cancel();
  }

  public void onDismissClick(View view)
  {
    finish();
  }

  private void readPreferences()
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
  }

  private void addNotification(Alarm alarm)
  {
    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification;
    PendingIntent activity;
    Intent intent;

    Log.i(TAG, "AlarmNotification.addNotification(" + alarm.getId() + ", '" + alarm.getTitle() + "', '" + mDateTime.formatDetails(alarm) + "')");

    intent = new Intent(this.getApplicationContext(), MainActivity.class);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    activity = PendingIntent.getActivity(this, (int)alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

   /* NotificationChannel channel = new NotificationChannel("alarmme_01", "AlarmMe Notifications",
        NotificationManager.IMPORTANCE_DEFAULT);

    notification = new Builder(this)
        .setContentIntent(activity)
        .setAutoCancel(true)
        .setContentTitle("Missed alarm: " + alarm.getTitle())
        .setContentText(mDateTime.formatDetails(alarm))
        .setChannelId("alarmme_01")
        .build();

    notificationManager.createNotificationChannel(channel);

    notificationManager.notify((int)alarm.getId(), notification);*/
  }

  @Override
  public void onBackPressed()
  {
    finish();
  }

  private class PlayTimerTask extends TimerTask
  {
    @Override
    public void run()
    {
      Log.i(TAG, "AlarmNotification.PalyTimerTask.run()");
      addNotification(mAlarm);
      finish();
    }
  }
}

