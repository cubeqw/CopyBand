package com.cubeqw.copyband;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity{

    private static String CHANNEL_ID = "0";
    private static final String CHANNEL_NAME = "CopyBand";
    private static final String CHANNEL_DESC = "CopyBand Service";
    String message;
    String full;
    ArrayList <String>quotes=new ArrayList<>();
    ArrayList <String>dates=new ArrayList<>();
    int count=0;
    boolean connected=true;
    EditText text;
    boolean bt_diag=false;
    SharedPreferences sPref;
    int miband;
    TextView empty;
    TinyDB tb;
    AlertDialog.Builder bluetooth_dialog;
    AlertDialog.Builder long_dialog;
    int bc;
    int tc;
    String date;
    private CustomAdapter recyclerViewAdapter;
    RecyclerView recyclerView;
    TabHost mTabHost;
    private final String TAG = "AlarmMe";

    private ListView mAlarmList;
    private AlarmListAdapter mAlarmListAdapter;
    private Alarm mCurrentAlarm;

    private final int NEW_ALARM_ACTIVITY = 0;
    private final int EDIT_ALARM_ACTIVITY = 1;
    private final int PREFERENCES_ACTIVITY = 2;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTheme(R.style.AppTheme);
        mTabHost=findViewById(R.id.tabHost);
        mTabHost.setup();
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.main);
        tabSpec.setIndicator(getResources().getString(R.string.messages));
        mTabHost.addTab(tabSpec);
        tabSpec = mTabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.reminder_layout);
        tabSpec.setIndicator(getResources().getString(R.string.reminder));
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(0);
        text =findViewById(R.id.text);
        tb=new TinyDB(getApplicationContext());
        quotes=tb.getListString("history");
        dates=tb.getListString("dates");
        recyclerViewAdapter = new CustomAdapter();
        recyclerView =findViewById(R.id.recyclerview_quotes);
        empty=findViewById(R.id.empty);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);
        toEnd();
        sPref=getSharedPreferences("setup",MODE_PRIVATE);
        Log.i(TAG, "AlarmMe.onCreate()");

        mAlarmList = (ListView)findViewById(R.id.alarm_list);

        mAlarmListAdapter = new AlarmListAdapter(this);
        mAlarmList.setAdapter(mAlarmListAdapter);
        mAlarmList.setOnItemClickListener(mListOnItemClickListener);
        registerForContextMenu(mAlarmList);

        mCurrentAlarm = null;
        miband=sPref.getInt("miband", -1);
        for (int i = 0; i < quotes.size(); i++) {
            full=quotes.get(i);
            date=dates.get(i);
            recyclerViewAdapter.add();
        }
        if(quotes.size()==0){
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        if(miband==-1){
            Intent intent=new Intent(MainActivity.this, Tutorial.class);
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        toEnd();
    }
    public void onClick(View v){
        message = text.getText().toString();
        date=new SimpleDateFormat("dd.MM HH:mm").format(Calendar.getInstance().getTime());
        if(!text.getText().toString().equals("")){
            createNotification();
        full= text.getText().toString();
        count=0;
            dates.add(date);
            quotes.add(full);
        if(connected){
            recyclerViewAdapter.add();
            text.setText("");
            toEnd();
            tb.putListString("history", quotes);
            tb.putListString("dates",dates);
            String title=getResources().getString(R.string.title_bd);
            String msg=getResources().getString(R.string.bt_diag);
            String off=getResources().getString(R.string.off);
            String cancel=getResources().getString(R.string.cancel);
            bluetooth_dialog = new AlertDialog.Builder(MainActivity.this);
            bluetooth_dialog.setTitle(title);
            bluetooth_dialog.setMessage(msg);
            bluetooth_dialog.setPositiveButton(off, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    bt_diag = true;
                }
            });
            bluetooth_dialog.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    bt_diag = false;
                }
            });
            bluetooth_dialog.show();
            Finish finish = new Finish(30000, 1000);
            finish.start();
        }
    }}
    public void createNotification(){
        for (int i = 0; i < bc; i++) {
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_on), Toast.LENGTH_SHORT).show();
                connected=false;
            }
            else {
                connected=true;
                NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setPriority(NotificationCompat.PRIORITY_HIGH).setSound(null).setAutoCancel(true).setVibrate(new long[]{0L}).setSmallIcon(R.drawable.send_button);
                ;
                if (message.length() <= bc * tc) {
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
                    toEnd();
                }
                else{
                    long_dialog= new AlertDialog.Builder(MainActivity.this);
                    String title=getResources().getString(R.string.long_title);
                    String msg=getResources().getString(R.string.long_message);
                    String cancel=getResources().getString(R.string.cancel);
                    String contine=getResources().getString(R.string.contine);
                    long_dialog.setTitle(title);
                    long_dialog.setMessage(msg);
                    long_dialog.setPositiveButton(contine, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            message=message.substring(0,bc*tc);
                            createNotification();
                        }
                    });
                    long_dialog.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                        }
                    });
                    long_dialog.show();
                    break;
                }}            count++;
        }
        }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "AlarmMe.onDestroy()");
//    mAlarmListAdapter.save();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "AlarmMe.onResume()");
        mAlarmListAdapter.updateAlarms();
    }

    public void onAddAlarmClick(View view)
    {
        Intent intent = new Intent(getBaseContext(), EditAlarm.class);

        mCurrentAlarm = new Alarm(this);
        mCurrentAlarm.toIntent(intent);

        MainActivity.this.startActivityForResult(intent, NEW_ALARM_ACTIVITY);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_ALARM_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                mCurrentAlarm.fromIntent(data);
                mAlarmListAdapter.add(mCurrentAlarm);
            }
            mCurrentAlarm = null;
        } else if (requestCode == EDIT_ALARM_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                mCurrentAlarm.fromIntent(data);
                mAlarmListAdapter.update(mCurrentAlarm);
            }
            mCurrentAlarm = null;
        } else if (requestCode == PREFERENCES_ACTIVITY) {
            mAlarmListAdapter.onSettingsUpdated();
        }
    }
    private AdapterView.OnItemClickListener mListOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = new Intent(getBaseContext(), EditAlarm.class);

            mCurrentAlarm = mAlarmListAdapter.getItem(position);
            mCurrentAlarm.toIntent(intent);
            MainActivity.this.startActivityForResult(intent, EDIT_ALARM_ACTIVITY);
        }
    };
    public void toEnd(){
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.edit_model:
                Intent intent=new Intent(MainActivity.this, Setup.class);
                startActivity(intent);
                return true;
                case R.id.clean:
                clearData();
                    return true;
            case R.id.about:
                Intent intent1=new Intent(MainActivity.this, About.class);
                startActivity(intent1);
            default:return super.onOptionsItemSelected(item);}}

    public void clearData() {
        dates.clear();
        quotes.clear();
        tb.clear();
        recreate();}

    public void end_action(){
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.cancelAll();
       if (bt_diag){
           BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
           if (mBluetoothAdapter.isEnabled()) {
               mBluetoothAdapter.disable();
               connected=false;
           }
           Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_off), Toast.LENGTH_SHORT).show();
       }

    }
    public class Finish extends CountDownTimer
    {

        public Finish(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish()
        {
        end_action();
        }

        public void onTick(long millisUntilFinished)
        {
        }

    }
    private class CustomAdapter extends RecyclerView.Adapter<QuoteViewHolder>{
        private List<String> quotes = new ArrayList<>();
        public CustomAdapter()
        {
            super();
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position)
        {
            return quotes.get(position).hashCode();
        }

        @Override
        public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return QuoteViewHolder.make(parent);
        }

        @Override
        public void onBindViewHolder(QuoteViewHolder holder, final int position)
        {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message=quotes.get(position);
                        message=message.replaceAll("\n|\r\n", " ");
                        full = quotes.get(position);
                        date = new SimpleDateFormat("dd.MM HH:mm").format(Calendar.getInstance().getTime());
                        createNotification();
                        dates.add(date);
                        quotes.add(full);
                        if(connected) {
                        quotes.remove(position);
                        dates.remove(position);
                        count = 0;
                        notifyDataSetChanged();
                        text.setText("");
                        toEnd();
                                    String title=getResources().getString(R.string.title_bd);
                                    String msg=getResources().getString(R.string.bt_diag);
                                    String off=getResources().getString(R.string.off);
                                    String cancel=getResources().getString(R.string.cancel);
                                    bluetooth_dialog = new AlertDialog.Builder(MainActivity.this);
                                    bluetooth_dialog.setTitle(title);
                                    bluetooth_dialog.setMessage(msg);
                                    bluetooth_dialog.setPositiveButton(off, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            bt_diag = true;
                                        }
                                    });
                                    bluetooth_dialog.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            bt_diag = false;
                                        }
                                    });
                                    bluetooth_dialog.show();
                                    Finish finish = new Finish(30000, 1000);
                                    finish.start();
                    }}
                });

            holder.setModel(quotes.get(position));
            holder.setModel1(dates.get(position));
        }
        @Override
        public int getItemCount()
        {
            return quotes.size();
        }

        public void add()
        {
            quotes.add(full);
            notifyDataSetChanged();
        }
    }
}


