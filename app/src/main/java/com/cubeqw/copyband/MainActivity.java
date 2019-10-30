package com.cubeqw.copyband;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static String CHANNEL_ID = "0";
    private static final String CHANNEL_NAME = "CopyBand";
    private static final String CHANNEL_DESC = "CopyBand Service";
    String message;
    String full;
    ArrayList <String>quotes=new ArrayList<>();
    ArrayList <String>dates=new ArrayList<>();
    int count=0;
    boolean connected=false;
    EditText text;
    boolean first=true;
    SharedPreferences sPref;
    int miband;
    TinyDB tb;
    int bc;
    int tc;
    String date;
    private CustomAdapter recyclerViewAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text =findViewById(R.id.text);
        tb=new TinyDB(getApplicationContext());
        quotes=tb.getListString("history");
        dates=tb.getListString("dates");
        recyclerViewAdapter = new CustomAdapter();
        recyclerView =findViewById(R.id.recyclerview_quotes);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);
        toEnd();
        sPref=getSharedPreferences("setup",MODE_PRIVATE);
        miband=sPref.getInt("miband", -1);
        for (int i = 0; i < quotes.size(); i++) {
            full=quotes.get(i);
            date=dates.get(i);
            recyclerViewAdapter.add();
        }
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        toEnd();
    }
    public void onClick(View v){
        date=new SimpleDateFormat("dd.MM HH:mm").format(Calendar.getInstance().getTime());
        if(!text.getText().toString().equals("")){
        full= text.getText().toString();
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
        recyclerViewAdapter.add();
        quotes.add(full);
        dates.add(date);
        text.setText("");
        toEnd();
        tb.putListString("history", quotes);
        tb.putListString("dates",dates);
    }}
    public void createNotification(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Turn on Bluetooth!", Toast.LENGTH_SHORT).show();
            connected=false;
        }
        else{
            connected=true;
        }
        if(connected){
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
                toEnd();
          Finish finish=new Finish(30000, 1000);
          finish.start();}
            }else {
        }}
    public void toEnd(){
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    public void end_action(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.cancelAll();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            connected=false;
        }
        Toast.makeText(getApplicationContext(), "Finish", Toast.LENGTH_SHORT).show();
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




    private class CustomAdapter extends RecyclerView.Adapter<QuoteViewHolder>
    {
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
            holder.itemView.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    full=quotes.get(position);
                    date=new SimpleDateFormat("dd.MM HH:mm").format(Calendar.getInstance().getTime());
                    for (int i = 0; i < bc; i++) {
                        if (first) {
                            message=quotes.get(position);
                            first = false;
                        }
                        createNotification();
                        count++;
                    }
                    quotes.remove(position);
                    dates.remove(position);
                    dates.add(date);
                    count=0;
                    first=true;
                    recyclerViewAdapter.add();
                    notifyDataSetChanged();
                    text.setText("");
                    toEnd();}
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
            dates.add(date);
            quotes.add(full);
            notifyDataSetChanged();
        }
    }

}


