package com.cubeqw.copyband;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class About extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setTitle(getResources().getString(R.string.about));
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_about);
            setTitle(getResources().getString(R.string.about));
        }

        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.about_menu, menu);
            return true;
        }
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.cubeqw:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/cubeqw"));
                    startActivity(browserIntent);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
