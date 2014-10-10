package com.aki.glass.contact;

import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.aki.utils.MySalesforceException;
import com.aki.utils.OnSalesforceTaskCompleted;
import com.aki.utils.SObject;
import com.aki.utils.SalesforceCreateTask;
import com.aki.utils.SalesforceLoginTask;
import com.aki.utils.SalesforceQueryTask;
import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


public class MainActivity extends Activity implements OnSalesforceTaskCompleted {

	static String TAG = "MainActivity";
	private Card card;

	private GestureDetector mGestureDetector;
	private AudioManager maManager;
	
	private int LOGIN = 1;
	
	private AppBean bean = null;
	
	private Intent bleService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bean = (AppBean) this.getApplication();
		maManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
		mGestureDetector = createGestureDetector(this);
		
		SalesforceLoginTask task = new SalesforceLoginTask(this);
		task.setCode(LOGIN);
		task.execute();
		
		/* DO NOT DELETE
		  bleService = new Intent(getBaseContext(), BLEService.class);
		  startService(bleService);
		*/
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gdDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gdDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					// play the tap sound
					maManager.playSoundEffect(Sounds.TAP);
					moveToQRActivity();
					return true;
				}
				if(gesture == Gesture.TWO_TAP) {
					stopService(bleService);
					finish();
					Log.d(TAG, "program stop");
					return true;
				}
				return false;
			}
		});
		return gdDetector;
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null)
			return mGestureDetector.onMotionEvent(event);
		return false;
	}

	private void moveToQRActivity() {
		Intent myIntent = new Intent(this, ScannerActivity.class);
		startActivity(myIntent);		

	}

	@Override
	public void successSalesforceTask(int code, Map<String, Object> result) {
		if (LOGIN == code) {
			bean.token = (String) result.get("token");
			card = new Card(this);
			card.setImageLayout(Card.ImageLayout.FULL);			
			card.addImage(R.drawable.welcome);
			setContentView(card.getView());			
		}
	}

	@Override
	public void errorSalesforceTask(int code, MySalesforceException e) {
		// TODO Auto-generated method stub
		
	}

}