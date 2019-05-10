package com.example.fevrec;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class RpmActivity extends AppCompatActivity {

    private int cylinderQuantity;
    private int  AUDIO_SAMPLE_RATE;
    private final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private final static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private final static int AUDIO_ENCODEFORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int BufferSize;


    BottomNavigationView rpmNavView;

    private AudioRecord audioRecord;
    private RpmProzess rpmProzess;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpm);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getBundleExtra("settingInformations");
        AUDIO_SAMPLE_RATE = (int) bundle.getInt("audioSampleRate");
        cylinderQuantity = (int) bundle.getInt("cylinderQuantity");

        rpmProzess = new RpmProzess();
        textView = (TextView) findViewById(R.id.tv_rpm);

        BufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,CHANNEL_CONFIGURATION,AUDIO_ENCODEFORMAT);

        rpmNavView = findViewById(R.id.nav_view_rpm);
        rpmNavView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_record:
                    if(!rpmProzess.isRecording()){
                        audioRecord = new AudioRecord(AUDIO_SOURCE,AUDIO_SAMPLE_RATE,CHANNEL_CONFIGURATION,AUDIO_ENCODEFORMAT,BufferSize);
                        rpmProzess.start(audioRecord,BufferSize,textView,AUDIO_SAMPLE_RATE,cylinderQuantity);
                    }
                    break;
                case R.id.navigation_pause:
                    if(rpmProzess.isRecording()) {
                        rpmProzess.stop(textView);
                    }
                    break;
                case R.id.navigation_back:
                    finish();
                    break;
            }
            return true;
        }
    };

}
