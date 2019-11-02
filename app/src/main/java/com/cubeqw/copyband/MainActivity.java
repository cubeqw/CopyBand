package com.cubeqw.copyband;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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
    boolean connected=true;
    EditText text;
    boolean first=true;
    boolean bt_diag=false;
    SharedPreferences sPref;
    int miband;
    TextView empty;
    TinyDB tb;
    AlertDialog.Builder bluetooth_dialog;
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
        empty=findViewById(R.id.empty);
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
        if(connected){
            recyclerViewAdapter.add();
            quotes.add(full);
            dates.add(date);
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
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
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
        }}}
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
            default:return super.onOptionsItemSelected(item);}}

    public void clearData() {
        try{
        dates.clear();
        quotes.clear();
        tb.clear();
        recyclerViewAdapter.notifyDataSetChanged();}catch (IndexOutOfBoundsException e){}
    }

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
                        full = quotes.get(position);
                        date = new SimpleDateFormat("dd.MM HH:mm").format(Calendar.getInstance().getTime());
                        for (int i = 0; i < bc; i++) {
                            if (first) {
                                message = quotes.get(position);
                                first = false;
                            }
                            createNotification();
                            count++;
                        }
                        if(connected) {
                        quotes.remove(position);
                        dates.remove(position);
                        dates.add(date);
                        count = 0;
                        first = true;
                        recyclerViewAdapter.add();
                        notifyDataSetChanged();
                        text.setText("");
                        toEnd();
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
            dates.add(date);
            quotes.add(full);
            notifyDataSetChanged();
        }
    }
}


