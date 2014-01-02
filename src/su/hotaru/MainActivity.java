package su.hotaru;

import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private final int NOTIFICATION_ID=0;
	private NotificationManager nm;
	private SeekBar redBar,greenBar,blueBar;
	private TextView redNum,greenNum,blueNum;
	protected Button colorBox;
	protected final float[] freqTable={0.1f,0.2f,0.3f,0.5f,1.f,2.f,3.f,4.f,5.f,8.f, 10.f,24.f,30.f,60.f};
	protected final float[] dutyTable={1.f,10.f,20.f,33.3333333f,50.f,66.6666666f,80.f,90.f,100.f};
	private SeekBar freqBar,dutyBar;
	private TextView freqNum,dutyNum;
	private ToggleButton toggleButton;
	private int onms, offms;
	private final int DEFAULT_INTENSITY_RED=0x00;
	private final int DEFAULT_INTENSITY_GREEN=0xff;
	private final int DEFAULT_INTENSITY_BLUE=0xff;
	private final int DEFAULT_FREQ_INDEX=5;
	private final int DEFAULT_DUTY_INDEX=3;
	private final String SAVELABEL_RED="red";
	private final String SAVELABEL_GREEN="green";
	private final String SAVELABEL_BLUE="blue";
	private final String SAVELABEL_FREQ="freq";
	private final String SAVELABEL_DUTY="duty";
	private final String SAVELABEL_TOGGLE="toggle";
	private SharedPreferences shrP;
	private abstract class EmptySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			refreshParams();
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) { }
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) { }
	}
	private class ColorComponentChangeListener extends EmptySeekBarChangeListener {
		TextView fld;
		protected ColorComponentChangeListener(TextView v) {
			fld=v;
		}
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			fld.setText(Integer.toHexString(progress));
			colorBox.setBackgroundColor(getARGB());
			super.onProgressChanged(seekBar, progress, fromUser);
		}
	}
	private class PseudoSeekBarListener extends EmptySeekBarChangeListener {
		private float [] tbl;
		private TextView numView;
		protected PseudoSeekBarListener(TextView numView,float[] tbl) {
			this.numView=numView;
			this.tbl=tbl;
		}
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			numView.setText(String.format("%.1f", tbl[progress]));
			super.onProgressChanged(seekBar, progress, fromUser);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		toggleButton=(ToggleButton)findViewById(R.id.toggleButton);
		colorBox=(Button)findViewById(R.id.colorBox);
		
		redBar=(SeekBar)findViewById(R.id.redBar);
		greenBar=(SeekBar)findViewById(R.id.greenBar);
		blueBar=(SeekBar)findViewById(R.id.blueBar);
		
		redNum=(TextView)findViewById(R.id.redNum);
		greenNum=(TextView)findViewById(R.id.greenNum);
		blueNum=(TextView)findViewById(R.id.blueNum);
		
		redBar.setOnSeekBarChangeListener(new ColorComponentChangeListener(redNum));
		greenBar.setOnSeekBarChangeListener(new ColorComponentChangeListener(greenNum));
		blueBar.setOnSeekBarChangeListener(new ColorComponentChangeListener(blueNum));
		
		freqNum=(TextView)findViewById(R.id.freqNum);
		freqBar=(SeekBar)findViewById(R.id.freqBar);
		freqBar.setOnSeekBarChangeListener(new PseudoSeekBarListener(freqNum,freqTable));
		
		dutyNum=(TextView)findViewById(R.id.dutyNum);
		dutyBar=(SeekBar)findViewById(R.id.dutyBar);
		dutyBar.setOnSeekBarChangeListener(new PseudoSeekBarListener(dutyNum,dutyTable));

		nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		shrP=getSharedPreferences(getPackageName(), android.content.Context.MODE_PRIVATE);
		if(savedInstanceState==null) {
			redBar.setProgress(shrP.getInt(SAVELABEL_RED,DEFAULT_INTENSITY_RED));
			greenBar.setProgress(shrP.getInt(SAVELABEL_GREEN,DEFAULT_INTENSITY_GREEN));
			blueBar.setProgress(shrP.getInt(SAVELABEL_BLUE, DEFAULT_INTENSITY_BLUE));
			freqBar.setProgress(shrP.getInt(SAVELABEL_FREQ,DEFAULT_FREQ_INDEX));
			dutyBar.setProgress(shrP.getInt(SAVELABEL_DUTY, DEFAULT_DUTY_INDEX));
		} else {
			redBar.setProgress(savedInstanceState.getInt(SAVELABEL_RED,DEFAULT_INTENSITY_RED));
			greenBar.setProgress(savedInstanceState.getInt(SAVELABEL_GREEN,DEFAULT_INTENSITY_GREEN));
			blueBar.setProgress(savedInstanceState.getInt(SAVELABEL_BLUE,DEFAULT_INTENSITY_BLUE));
			freqBar.setProgress(savedInstanceState.getInt(SAVELABEL_FREQ,DEFAULT_FREQ_INDEX));
			dutyBar.setProgress(savedInstanceState.getInt(SAVELABEL_DUTY,DEFAULT_DUTY_INDEX));
			toggleButton.setChecked(savedInstanceState.getBoolean(SAVELABEL_TOGGLE,false));
		}
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(SAVELABEL_RED, redBar.getProgress());
		savedInstanceState.putInt(SAVELABEL_GREEN, greenBar.getProgress());
		savedInstanceState.putInt(SAVELABEL_BLUE, blueBar.getProgress());
		savedInstanceState.putInt(SAVELABEL_FREQ, freqBar.getProgress());
		savedInstanceState.putInt(SAVELABEL_DUTY, dutyBar.getProgress());
		savedInstanceState.putBoolean(SAVELABEL_TOGGLE, toggleButton.isChecked());
		super.onSaveInstanceState(savedInstanceState);
	}
	@Override
	protected void onPause() {
		SharedPreferences.Editor e=shrP.edit();
		e.putInt(SAVELABEL_RED, redBar.getProgress());
		e.putInt(SAVELABEL_GREEN, greenBar.getProgress());
		e.putInt(SAVELABEL_BLUE, blueBar.getProgress());
		e.putInt(SAVELABEL_FREQ, freqBar.getProgress());
		e.putInt(SAVELABEL_DUTY, dutyBar.getProgress());
		e.commit();
		super.onPause();
	}

	private void flashLed(int argb) {
		Notification n=new Notification();
		n.flags = Notification.FLAG_SHOW_LIGHTS;
		n.ledARGB=argb;
		n.ledOnMS=onms;
		n.ledOffMS=offms;
		nm.notify(NOTIFICATION_ID,n);
	}
	protected int getARGB() {
		int red=redBar.getProgress();
		int green=greenBar.getProgress();
		int blue=blueBar.getProgress();
		int argb=0xff000000 | (red<<16) | (green<<8) | blue;
		return argb;
	}
	protected void calcOnOffMs() {
		int freqProgress=freqBar.getProgress();
		float freq=freqTable[freqProgress];
		int dutyProgress=dutyBar.getProgress();
		float duty=dutyTable[dutyProgress];
		float lambda=1000.f/freq;
		onms=(int) (lambda*duty/100.f);
		offms=(int) (lambda*(100.f-duty)/100.f);
	}
	public boolean onToggled(View v) {
		if(toggleButton.isChecked()) {
			calcOnOffMs();
			flashLed(getARGB());
		} else {
			nm.cancel(NOTIFICATION_ID);
		}
		return true;
	}
	protected void refreshParams() {
		if(toggleButton.isChecked()) {
			calcOnOffMs();
			nm.cancel(NOTIFICATION_ID);
			flashLed(getARGB());
		}
	}
}
