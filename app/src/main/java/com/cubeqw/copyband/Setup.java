package com.cubeqw.copyband;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Setup extends AppCompatActivity {

    SharedPreferences sPref;
    private CheckBox mBoldCheckBox;
    SharedPreferences.Editor ed;
    int miband;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setTitle("Chose your Mi Band");
        sPref=getSharedPreferences("setup",MODE_PRIVATE);
        ed=sPref.edit();
        mBoldCheckBox =findViewById(R.id.checkBoxBold);
        RadioGroup radioGroup =findViewById(R.id.radioGroup);
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case -1:
                        break;
                    case R.id.radioButtonHusband:
                        miband=1;
                        mBoldCheckBox.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radioButtonKitten:
                        miband=2;
                        mBoldCheckBox.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }
    public void onClick(View v){
       if(mBoldCheckBox.isChecked())
           miband=0;
       ed.putInt("miband", miband);
       ed.commit();
       Toast.makeText(getApplicationContext(), miband+"", Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(Setup.this, MainActivity.class);
        intent.putExtra("setup_end", miband);
        startActivity(intent);
        this.finish();    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
