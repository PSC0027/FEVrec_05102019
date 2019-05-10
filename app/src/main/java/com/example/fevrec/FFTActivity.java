package com.example.fevrec;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.fevrec.fast_fourier_transformation.FFT;

public class FFTActivity extends AppCompatActivity {

    private static final String TAG = "FFTActivity";

    private int FFT_BLOCK_SIZE;

    private int  AUDIO_SAMPLE_RATE;
    private final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private final static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private final static int AUDIO_ENCODEFORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int BufferSize;

    private int rateY;

    private Context context;
    private AudioRecord audioRecord;
    private AudioProcess audioProcess;

    BottomNavigationView fftNavView;
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fft);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getBundleExtra("settingInformations");
        AUDIO_SAMPLE_RATE = (int) bundle.getInt("audioSampleRate");
        FFT_BLOCK_SIZE = (int) bundle.getInt("fftBlockSize");

        Log.d(TAG, "AUDIO_SAMPLE_RATE: " + AUDIO_SAMPLE_RATE + "; FFT_BLOCK_SIZE: " + FFT_BLOCK_SIZE +";");

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        fftNavView = (BottomNavigationView) findViewById(R.id.nav_view_fft);

        BufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,CHANNEL_CONFIGURATION,AUDIO_ENCODEFORMAT);
        Log.d(TAG, "bufferSize :" + BufferSize);

        context = getApplicationContext();
        rateY = 50;
        audioProcess = new AudioProcess();
        audioProcess.initDraw(rateY,surfaceView.getHeight(),context,AUDIO_SAMPLE_RATE);

        fftNavView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_record:
                    if(!audioProcess.isRecording()){
                        audioRecord = new AudioRecord(AUDIO_SOURCE,AUDIO_SAMPLE_RATE,CHANNEL_CONFIGURATION,AUDIO_ENCODEFORMAT,BufferSize);

                        audioProcess.baseLine = surfaceView.getHeight()-100;
                        audioProcess.frequence = AUDIO_SAMPLE_RATE;
                        audioProcess.start(audioRecord,BufferSize,surfaceView);
                    }
                    break;
                case R.id.navigation_pause:
                    if(audioProcess.isRecording()){
                        audioProcess.stop(surfaceView);
                    }
                    break;
                case R.id.navigation_back:
//                    if(audioProcess.isRecording())
//                        Toast.makeText(getApplicationContext(), "Please pause the record first", Toast.LENGTH_SHORT).show();
//                    else
//                        finish();
                    finish();
                    break;
            }
            return true;
        }
    };
}
