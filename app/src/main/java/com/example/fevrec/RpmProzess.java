package com.example.fevrec;

import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;
import java.util.ArrayList;

public class RpmProzess {
    private ArrayList<float[]> outBuf = new ArrayList<float[]>();

    private boolean isRecording = false;
    public boolean isRecording() {
        return isRecording;
    }
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public void start(AudioRecord audioRecord, int minBufferSize, TextView textView, int SAMPLE_RATE, int cylinderQuantity){
        isRecording = true;
        new RpmProzess.RecordThread(audioRecord, minBufferSize).start();
        new RpmProzess.AnalysProzess(textView,SAMPLE_RATE,cylinderQuantity,minBufferSize).start();
    }

    public void stop(TextView textView){
        isRecording = false;
    }

     private class RecordThread extends Thread{
        private AudioRecord audioRecord;
        private int minBufferSize;


        public RecordThread(AudioRecord audioRecord,int minBufferSize){
            this.audioRecord = audioRecord;
            this.minBufferSize = minBufferSize;

        }

        public void run() {
            try {
                short[] buffer = new short[minBufferSize];
                audioRecord.startRecording();

                while (isRecording) {
                    int res = audioRecord.read(buffer, 0, minBufferSize);

                    short[] tmpBuf = new short[buffer.length];
                    System.arraycopy(buffer, 0, tmpBuf, 0, buffer.length);

                    float[] tmpBuf_float = new float[buffer.length];

                    for (int i = 0; i < buffer.length; i++) {
                        Short short1 = tmpBuf[i];
                        tmpBuf_float[i] = short1.floatValue();
                    }

                    synchronized (outBuf) {
                        outBuf.add(tmpBuf_float);
                    }
                }
                audioRecord.stop();
            } catch (Exception e) {
                // TODO: handle exception
                Log.i("Rec E", e.toString());
            }
        }
    }

    private class AnalysProzess extends Thread{
        private TextView textView;
        private int SAMPLE_RATE;
        private int cylinderQuantity;
        private int minBufferSize;

        public AnalysProzess(TextView textView, int SAMPLE_RATE, int cylinderQuantity,int minBufferSize) {
            this.textView = textView;
            this.SAMPLE_RATE = SAMPLE_RATE;
            this.cylinderQuantity = cylinderQuantity;
            this.minBufferSize = minBufferSize;
        }

        public void run() {
            while (isRecording) {
                ArrayList<float[]>buf = new ArrayList<float[]>();
                synchronized (outBuf) {
                    if (outBuf.size()< Math.ceil((double)2*60*SAMPLE_RATE/(250*cylinderQuantity*minBufferSize))) {   //< 2*60*SAMPLE_RATE/(250*cylinderQuantity*minBufferSize)
                        continue;
                    }
                    buf = (ArrayList<float[]>)outBuf.clone();
                    outBuf.clear();
                }

                ArrayList<Float>tmpBuf = new ArrayList<Float>();

                for(int i=0;i<buf.size();i++){
                    for (int j=0;j<buf.get(i).length;j++){
                        tmpBuf.add(buf.get(i)[j]);
                    }
                }
                if (tmpBuf.size() < 2*60*44100/(250*cylinderQuantity)) {
                    continue;
                }

                float[] samples = new float[Math.round(44100*60*2/(250*cylinderQuantity))];
                for(int i = 0; i < samples.length; i++)
                    {
                        samples[i] = tmpBuf.get(i);
                    }


                float[] autocor = Autocorrelation(samples);
                float rpm = FindRPM(autocor);
                if(rpm > 500 && rpm < 1000)
                {
                    textView.setText(Float.toString(rpm));
                }

            }

        }

        private float[] Autocorrelation(float[] samples){
            float[] result = new float[samples.length];

            for(int i = 0; i < samples.length; i++){
                for(int j = 0; j < samples.length-i; j++){
                    result[i] += samples[j]*samples[i+j];
                }
            }
            return result;
        }

        private float FindRPM (float[] autocor_halb){
            Float max = 0f;
            int maxIndex = 0;

            for(int i = Math.round(60*44100/(2*7000)); i < autocor_halb.length; i++){
                if (autocor_halb[i]>max){
                    max = autocor_halb[i];
                    maxIndex = i;
                }
            }
            return 60f*SAMPLE_RATE/((cylinderQuantity/2)*maxIndex);
        }
    }
}
