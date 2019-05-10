package com.example.fevrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.media.AudioRecord;
import android.util.Log;
import android.view.SurfaceView;
import com.example.fevrec.fast_fourier_transformation.FFT;

import java.util.ArrayList;
import java.util.List;

public class AudioProcess {
	public static final float pi= (float) 3.1415926;
	private ArrayList<short[]> inBuf = new ArrayList<short[]>();
	private ArrayList<double[]> outBuf = new ArrayList<double[]>();

    private boolean isRecording = false;
    public boolean isRecording() {
        return isRecording;
    }
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

	Context mContext;
	private int shift = 30;
	public int frequence = 0;
	
	private int length = 256;
	public int rateY = 25;
	public int baseLine = 0;
	public void initDraw(int rateY, int baseLine,Context mContext, int frequence){
		this.mContext = mContext;
		this.rateY = rateY;
		this.baseLine = baseLine;
		this.frequence = frequence;
	}

	public void start(AudioRecord audioRecord, int minBufferSize, SurfaceView sfvSurfaceView) {
		isRecording = true;
		new RecordThread(audioRecord, minBufferSize).start();
		//new ProcessThread().start();
		new DrawThread(sfvSurfaceView).start();
	}

	public void stop(SurfaceView sfvSurfaceView){
		isRecording = false;
		inBuf.clear();
		//sfvSurfaceView;
		//drawBuf.clear();
		//outBuf.clear();
	}
	

	class RecordThread extends Thread{
		private AudioRecord audioRecord;
		private int minBufferSize;

		public RecordThread(AudioRecord audioRecord,int minBufferSize){
			this.audioRecord = audioRecord;
			this.minBufferSize = minBufferSize;
		}
		
		public void run(){
			try{
				short[] buffer = new short[minBufferSize];
				audioRecord.startRecording();

				while(isRecording){
					int res = audioRecord.read(buffer, 0, minBufferSize);
					synchronized (inBuf){
						inBuf.add(buffer);
					}
					length=up2int(res);
					short[]tmpBuf = new short[length];
					System.arraycopy(buffer, 0, tmpBuf, 0, length);

					float[] tmpBuf_float = new float[length];

					for(int i=0;i < length; i++){
						Short short1 = tmpBuf[i];
						tmpBuf_float[i] = short1.floatValue();
					}

					double[] outDouble = FastFourierTransform(tmpBuf_float,frequence);

					synchronized (outBuf) {
						outBuf.add(outDouble);
					}
				}
				audioRecord.stop();
			}catch (Exception e) {
				// TODO: handle exception
				Log.i("Rec E",e.toString());
			}
			
		}
	}

	class DrawThread extends Thread{

		private SurfaceView sfvSurfaceView;

		private Paint mPaint;
		private Paint tPaint;
		private Paint dashPaint;
		public DrawThread(SurfaceView sfvSurfaceView) {
			this.sfvSurfaceView = sfvSurfaceView;

			mPaint = new Paint();         //Spectrum painter
			mPaint.setColor(Color.BLUE);
			mPaint.setStrokeWidth(2);
			mPaint.setAntiAlias(true);
			
			tPaint = new Paint();        //Axes painter
			tPaint.setColor(Color.BLACK);
			tPaint.setStrokeWidth(1);
			tPaint.setAntiAlias(true);
			
			dashPaint = new Paint();     //dashed line painter
			dashPaint.setStyle(Style.STROKE);
			dashPaint.setColor(Color.GRAY);
			Path path = new Path();
	        path.moveTo(0, 10);
	        path.lineTo(480,10); 
	        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
	        dashPaint.setPathEffect(effects);
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			while (isRecording) {
				ArrayList<double[]>buf = new ArrayList<double[]>();
				synchronized (outBuf) {
					if (outBuf.size() == 0) {
						continue;
					}
					buf = (ArrayList<double[]>)outBuf.clone();
					outBuf.clear();
				}

				for(int i = 0; i < buf.size(); i++){
					double[]tmpBuf = buf.get(i);
					SimpleDraw(tmpBuf, rateY, baseLine);
				}
				
			}
		}


		private void SimpleDraw(double[] buffer, int rate, int baseLine){
			Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
					new Rect(0, 0, buffer.length,sfvSurfaceView.getHeight()));
			canvas.drawColor(Color.WHITE);
			canvas.drawText("decibelֵ", 0, 3, 2, 15, tPaint);
			canvas.drawText("original point", 0, 7, 5, baseLine + 15, tPaint);
			canvas.drawText("frequency (Hz)", 0, 6, sfvSurfaceView.getWidth() - 50, baseLine + 30, tPaint);
			canvas.drawLine(shift, 20, shift, baseLine, tPaint);
			canvas.drawLine(shift, baseLine, sfvSurfaceView.getWidth(), baseLine, tPaint);
			canvas.save();
			canvas.rotate(30, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint);
			canvas.rotate(-60, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint);
			canvas.rotate(30, shift, 20);
			canvas.rotate(30, sfvSurfaceView.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceView.getWidth() - 1, baseLine, sfvSurfaceView.getWidth() - 11, baseLine, tPaint);
			canvas.rotate(-60, sfvSurfaceView.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceView.getWidth() - 1, baseLine, sfvSurfaceView.getWidth() - 11, baseLine, tPaint);
			canvas.restore();
			//tPaint.setStyle(Style.STROKE);
			for(int index = 64; index <= 1024; index = index + 64){
				canvas.drawLine(shift + index, baseLine, shift + index, 40, dashPaint);
				String str = String.valueOf(Math.round(length *5.4/ 1024 * index));
				canvas.drawText( str, 0, str.length(), shift + index - 15, baseLine + 15, tPaint);
			}
			int y;
			for(int i = 0; i < buffer.length; i = i + 1){
				y =(int) Math.round(baseLine - 100*buffer[i] / rateY);
				System.out.println(y);
				canvas.drawLine(2*i + shift, baseLine, 2*i +shift, y, mPaint);
			}
			sfvSurfaceView.getHolder().unlockCanvasAndPost(canvas);
		}
	}
	

	private int up2int(int iint) {
		int ret = 1;
		while (ret<=iint) {
			ret = ret << 1;
		}
		return ret>>1;
	}

//	public void fft(Complex[] xin,int N)
//	{
//	    int f,m,N2,nm,i,k,j,L;
//	    float p;
//	    int e2,le,B,ip;
//	    Complex w = new Complex();
//	    Complex t = new Complex();
//	    N2 = N / 2;
//	    f = N;
//	    for(m = 1; (f = f / 2) != 1; m++);
//	    nm = N - 2;
//	    j = N2;
//
//	    for(i = 1; i <= nm; i++)
//	    {
//	        if(i < j)
//	        {
//	            t = xin[j];
//	            xin[j] = xin[i];
//	            xin[i] = t;
//	        }
//	        k = N2;
//	        while(j >= k)
//	        {
//	            j = j - k;
//	            k = k / 2;
//				System.out.println("j =" + j + "; k = " + k);
//	        }
//	        j = j + k;
//	    }
//
//	    for(L=1; L<=m; L++)
//	    {
//	    	e2 = (int) Math.pow(2, L);
//
//	        le=e2+1;
//	        B=e2/2;
//	        for(j=0;j<B;j++)
//	        {
//	            p=2*pi/e2;
//	            w.real = Math.cos(p * j);
//
//	            w.image = Math.sin(p*j) * -1;
//
//	            for(i=j;i<N;i=i+e2)
//	            {
//	                ip=i+B;
//	                t=xin[ip].cc(w);
//	                xin[ip] = xin[i].cut(t);
//	                xin[i] = xin[i].sum(t);
//	            }
//	        }
//	    }
//	}

	private double[] FastFourierTransform(float[] floats,int samp_rate){
		FFT fft = new FFT(floats.length, samp_rate);
		fft.forward(floats);

		float[] test = fft.getSpectrumReal();

		List<Double> output = new ArrayList<>();
		double[] XMag = new double[test.length];
		for (int i = 0; i < fft.specSize(); i++) // i++ == i = i+1
		{
			double re = fft.getSpectrumReal()[i];
			double im = fft.getSpectrumImaginary()[i];

			double sq = (double) Math.sqrt(re*re + im*im);
			output.add(sq);
			// Block-Korrektur & Halbseitiges Spektrum(?!)
			XMag[i] = Math.round(sq/test.length*2);
		}

		return XMag;
	}
}
