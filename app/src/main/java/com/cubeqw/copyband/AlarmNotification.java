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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmNotification extends Activity
{
  private final String TAG = "AlarmMe";
  private static String CHANNEL_ID = "0";
  private Timer mTimer = null;
  private Alarm mAlarm;
    boolean end_action = false, bt_diag = false;
  private DateTime mDateTime;
    private TextView mTextView, status_tv, clock_tv, bt_st;
  private PlayTimerTask mTimerTask;
    LinearLayout clock, dialog;
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
      status_tv = findViewById(R.id.send_status);
      dialog = findViewById(R.id.dialog);
      bt_st = findViewById(R.id.bt_st);
    readPreferences();
    clock=findViewById(R.id.clock);
      clock_tv = findViewById(R.id.clock_string);
      clock_tv.setText(new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
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

    public void onClick_ok(View v) {
        bt_diag = true;
        bt_st.setText("Bluetooth будет отключён");
        dialog.setVisibility(View.GONE);
    }

    public void onClick_cancel(View v) {
        bt_st.setText("Bluetooth останется включенным");
        bt_diag = false;
        dialog.setVisibility(View.GONE);
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

    public void end_action() {
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.cancelAll();
        if (bt_diag) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_off), Toast.LENGTH_SHORT).show();
            }
        }
        this.finish();
    }
  public void createNotification(){
    Log.d("CopyBand", "Notification created");
    for (int i = 0; i < bc; i++) {
      BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (!mBluetoothAdapter.isEnabled()) {
        status_tv.setText(getResources().getString(R.string.bt_on));
          mBluetoothAdapter.enable();
          Finish finish = new Finish(20000, 1000);
          finish.start();
      }
      else {
          end_action = true;
          Finish finish = new Finish(20000, 1000);
          finish.start();
        status_tv.setText(getResources().getString(R.string.wait_send));
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_MIN).setSound(null).setAutoCancel(true).setVibrate(new long[]{0L}).setSmallIcon(R.drawable.send_button);
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

    public class Finish extends CountDownTimer {

        public Finish(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (end_action) {
                end_action();
            } else createNotification();
        }

        public void onTick(long millisUntilFinished) {
        }

    }
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

