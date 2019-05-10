package com.example.fevrec;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class FunctionSelectionActivity extends AppCompatActivity {
    private static final String TAG = "FunctionSelectionActivi";

    ImageButton setButton, rpmButton, fftButton;
    Bundle settingInformations;
    final static  int MIC_PERMISSION_CODE = 033;

    int CYLINDER_QUANTITY_INIT_VALUE = 4;
    int AUDIO_SAMPLING_RATE_INIT_VALUE = 44100;
    int FFT_BLOCK_SIZE_INIT_VALUE =44100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_selection);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION_CODE);

        try {
            settingInformations = (Bundle) getIntent().getBundleExtra("settingInformationsFormSetting").clone();
            Log.d(TAG, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD Setting informations come from: 1");
        }catch (NullPointerException e){
            if(savedInstanceState != null)
                settingInformations = (Bundle) savedInstanceState.clone();
            else
            {
                settingInformations = new Bundle();
                settingInformations.putInt("cylinderQuantity", CYLINDER_QUANTITY_INIT_VALUE);
                settingInformations.putInt("audioSampleRate", AUDIO_SAMPLING_RATE_INIT_VALUE);
                settingInformations.putInt("fftBlockSize",FFT_BLOCK_SIZE_INIT_VALUE);
                Log.d(TAG, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD Setting informations come from: 3");
            }

        }

        setButton = (ImageButton) findViewById(R.id.imageButton_set);
        setButton.setOnClickListener(mListener);
        rpmButton = (ImageButton) findViewById(R.id.imageButton_rpm);
        rpmButton.setOnClickListener(mListener);
        fftButton = (ImageButton) findViewById(R.id.imageButton_fft);
        fftButton.setOnClickListener(mListener);
    }

    private ImageButton.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent messageIntent = null;
            switch (v.getId()){
                case R.id.imageButton_rpm:
                    messageIntent = new Intent(FunctionSelectionActivity.this,RpmActivity.class);
                    messageIntent.putExtra("settingInformations",settingInformations);
                    startActivity(messageIntent);
                    break;
                case R.id.imageButton_fft:
                    messageIntent = new Intent(FunctionSelectionActivity.this,FFTActivity.class);
                    messageIntent.putExtra("settingInformations",settingInformations);
                    startActivity(messageIntent);
                    break;
                case R.id.imageButton_set:
                    messageIntent = new Intent(FunctionSelectionActivity.this,SettingActivity.class);
                    messageIntent.putExtra("settingInformations",settingInformations);
                    startActivity(messageIntent);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MIC_PERMISSION_CODE:
                {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();}
                else{Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState =(Bundle) settingInformations.clone();

    }
}
