package com.example.fevrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Spinner;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";

    BottomNavigationView setBottomNavigationView;

    Spinner spinnerCylinderQuantity;
    Spinner spinnerBlockSizeFFT;
    Spinner spinnerSamplingRate;

    Bundle settingInformationsFormSetting;

    int cylinderQuantity;
    int audioSampleRate;
    int fftBlockSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingInformationsFormSetting = new Bundle();
        cylinderQuantity = getIntent().getBundleExtra("settingInformations").getInt("cylinderQuantity");
        audioSampleRate = getIntent().getBundleExtra("settingInformations").getInt("audioSampleRate");
        fftBlockSize =getIntent().getBundleExtra("settingInformations").getInt("fftBlockSize");
//        Log.d(TAG, "cylinderQuantity: "+cylinderQuantity+"; audioSampleRate: "+audioSampleRate+"; fftBlockSize: " +fftBlockSize);

        spinnerCylinderQuantity = (Spinner) findViewById(R.id.sp_choose_cylinder_quantity);
        spinnerBlockSizeFFT = (Spinner) findViewById(R.id.sp_choose_block_size);
        spinnerSamplingRate = (Spinner) findViewById(R.id.sp_choose_sampling_rate);

        setBottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view_set);
        setBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Intent messageIntent = new Intent(SettingActivity.this,FunctionSelectionActivity.class);
            settingInformationsFormSetting.putInt("cylinderQuantity",cylinderQuantity);
            settingInformationsFormSetting.putInt("audioSampleRate",audioSampleRate);
            settingInformationsFormSetting.putInt("fftBlockSize",fftBlockSize);
            messageIntent.putExtra("settingInformationsFormSetting",settingInformationsFormSetting);
            startActivity(messageIntent);
            return true;
        }
    };
}
